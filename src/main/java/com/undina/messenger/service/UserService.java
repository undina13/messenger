package com.undina.messenger.service;


import com.undina.messenger.emailsender.EmailSender;
import com.undina.messenger.mapper.UserMapper;
import com.undina.messenger.model.JWTToken;
import com.undina.messenger.model.User;
import com.undina.messenger.model.dto.LoginRequest;
import com.undina.messenger.model.dto.RegisterUser;
import com.undina.messenger.model.dto.UpdateUser;
import com.undina.messenger.model.dto.UserTo;
import com.undina.messenger.repository.UserRepository;
import com.undina.messenger.security.JWTUtil;
import com.undina.messenger.validation.exceptions.ApplicationException;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final UserMapper userMapper;
    private final EmailSender emailSender;
    @Value("${admin.email.domain}")
    private String adminEmailDomain;
    @Value("${host}")
    private String host;


    public JWTToken login(LoginRequest loginRequest) {
        User user = getByEmail(loginRequest.getEmail());
        if (user.getBlocked()) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Account is blocked, contact support");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return new JWTToken(user.getId(), user.getRole(), token);
    }

    public JWTToken signup(RegisterUser registerUser) {
        User user = userMapper.toUser(registerUser);
        if (user.getEmail().endsWith(adminEmailDomain)) {
            user.setRole("ROLE_ADMIN");
        } else {
            user.setRole("ROLE_USER");
        }
        user.setVerifyCode(generateVerifyCode());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User registratedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(registratedUser.getId(), registratedUser.getRole());
        String activationLink = host + "/users/activate/" + registratedUser.getId() + "/" + registratedUser.getVerifyCode();

        emailSender.sendMessage(
                "Activate your email",
                activationLink,
                registratedUser.getEmail());

        return new JWTToken(registratedUser.getId(), registratedUser.getRole(), token);
    }


    public UserTo getUserInfo(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(HttpStatus.NOT_FOUND, "Not found"));
        return userMapper.toUserTo(user);
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new ApplicationException(HttpStatus.NOT_FOUND, "Not found"));
    }

    public JWTToken changeUserPassword(User user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return new JWTToken(user.getId(), user.getRole(), token);
    }

    public JWTToken changeUserInfo(String userId, UpdateUser updateUser) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(HttpStatus.NOT_FOUND, "Not found"));
        if (updateUser.getEmail() != null) {
            user.setVerifyCode(generateVerifyCode());
            String activationLink = host + "/users/email/" + updateUser.getEmail() + "/" + user.getId() + "/" + user.getVerifyCode();
            userRepository.save(user);
            emailSender.sendMessage(
                    "Change your email",
                    activationLink,
                    updateUser.getEmail());
        }
        if (updateUser.getLogin() != null) {
            user.setLogin(updateUser.getLogin());
        }
        if (updateUser.getFirstName() != null) {
            user.setFirstName(updateUser.getFirstName());
        }
        if (updateUser.getLastName() != null) {
            user.setLastName(updateUser.getLastName());
        }
        if (updateUser.getIsClosedFriends() != null) {
            user.setClosedFriends(updateUser.getIsClosedFriends());
        }
        if (updateUser.getIsClosedMessages() != null) {
            user.setClosedMessages(updateUser.getIsClosedMessages());
        }
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return new JWTToken(user.getId(), user.getRole(), token);
    }

    public boolean checkIfValidOldPassword(User user, String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    public JWTToken activateUserWithCode(UUID userId, String code) {
        User user = userRepository.findById(userId.toString()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found")
        );
        if (!code.equals(user.getVerifyCode())) {
            throw new ApplicationException(HttpStatus.NOT_ACCEPTABLE, "Code in not acceptable");
        }
        if (!user.getBlocked()) {
            throw new ApplicationException(HttpStatus.CONFLICT, "Account already activated");
        }
        user.setBlocked(false);
        user.setVerifyCode(generateVerifyCode());
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return new JWTToken(user.getId(), user.getRole(), token);
    }

    private String generateVerifyCode() {
        return RandomString.make(12);
    }

    public JWTToken changeUserEmail(String newEmail, UUID userId, String verifyCode) {
        User user = userRepository.findById(userId.toString()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found")
        );
        if (!verifyCode.equals(user.getVerifyCode())) {
            throw new ApplicationException(HttpStatus.NOT_ACCEPTABLE, "Code in not acceptable");
        }
        user.setEmail(newEmail);
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return new JWTToken(user.getId(), user.getRole(), token);
    }

    public void changeUserStatus(String userId, Boolean isBlocked) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found")
        );
        user.setBlocked(isBlocked);
        userRepository.save(user);

    }

    public void deleteUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found")
        );
        userRepository.delete(user);
    }

    @Transactional
    public UserTo addUserToFriends(String userId, String friendLogin) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found")
        );
        User friend = userRepository.findByLogin(friendLogin).orElseThrow(() ->
                new ApplicationException(HttpStatus.NOT_FOUND, "Not found"));
        user.getFriends().add(friend);
        userRepository.save(friend);
        return userMapper.toUserTo(friend);
    }

    @Transactional(readOnly = true)
    public Set<UserTo> getUserFriends(String userId, String friendLogin) {
        User friend = userRepository.findByLogin(friendLogin).orElseThrow(() ->
                new ApplicationException(HttpStatus.NOT_FOUND, "Not found"));
        if (friend.isClosedFriends()) {

            throw new ApplicationException(HttpStatus.CONFLICT, "user have closed list of friends");
        }

        return friend.getFriends().stream().map(userMapper::toUserTo).collect(Collectors.toSet());
    }
}
