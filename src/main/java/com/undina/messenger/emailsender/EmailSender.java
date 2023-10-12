package com.undina.messenger.emailsender;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class EmailSender {
    @Value("${spring.mail.username}")
    private String username;
    private final JavaMailSender javaMailSender;

    public void sendMessage(String subject, String text, String to) {
        log.info("sendMessage- start: {}, {}, {}", to, subject, text);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(username);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        log.info("sendMessage - start sending: {}", message);
        javaMailSender.send(message);
        log.info("sendMessage - end");
    }
}
