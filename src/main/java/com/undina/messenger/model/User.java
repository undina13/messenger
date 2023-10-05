package com.undina.messenger.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.undina.messenger.validation.ValueOfEnum;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class User {
    @Id
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "uuid2")
    @Column(length = 36, nullable = false, updatable = false)
    private String id;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String password;
    @ValueOfEnum(enumClass = UserRole.class)
    @Column(name = "role")
    private String role;
    @Column(name = "login", nullable = false, unique = true)
    private String login;
    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "last_name", nullable = false)
    private String lastName;
    @Column(name = "blocked", nullable = false)
    private Boolean blocked = true;
    @Column(name = "verify_code", length = 12)
    private String verifyCode;
    @Column(name = "created", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created = LocalDateTime.now();

    public User(String id, String email, String password, String role, Boolean blocked) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.blocked = blocked;
    }

    public User(String email, String password, String role) {
        this(null, email, password, role, true);
    }

    public User(String uuid, String email, String password, String role) {
        this(uuid, email, password, role, true);
    }

}
