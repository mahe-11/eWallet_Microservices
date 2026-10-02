package com.app.service;

import com.app.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {
    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @KafkaListener(topics = Constants.SEND_EMAIL_TOPIC, groupId = "e-wallet_group")
    public void receiveEmailMessage(String message) throws ParseException {

        log.info("In send Email Method with message :" + message);
        JSONObject data = (JSONObject) new JSONParser().parse(message);


        String to = data.get(Constants.EMAIL).toString();
        String subject = "Transaction Notification";
        String body = data.get(Constants.EMAIL_MESSAGE).toString();

        sendEmail(to, subject, body);
    }

    public void sendEmail(String to, String subject, String body) {

        log.info("Sending Email");
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);

        System.out.println("Email sent successfully to: " + to);
    }
}
