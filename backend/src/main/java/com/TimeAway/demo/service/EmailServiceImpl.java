package com.TimeAway.demo.service;

import com.TimeAway.demo.dto.VacationRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailServiceImpl implements EmailService{

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail ;



    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async("emailExecutor")
    @Override
    public void sendOrderNotification(VacationRequestDto requestDto, List<String> receiverEmails) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setBcc(receiverEmails.toArray(new String[0]));
            message.setSubject("Nouvelle demande de congé - " + requestDto.getEmployeeDTO().getFirstName() + " "
                    + requestDto.getEmployeeDTO().getLastName());

            String content = String.format(
                    "Bonjour,\n\n" +
                            "Une nouvelle demande de congé a été soumise :\n" +
                            "- Employé : %s\n" +
                            "- Date de début : %s\n" +
                            "- Date de fin : %s\n" +
                            "- Type de congé : %s\n\n",
                    requestDto.getEmployeeDTO().getFirstName() + " " + requestDto.getEmployeeDTO().getLastName(), // À adapter selon vos méthodes
                    requestDto.getFromDate(),              // À adapter selon vos méthodes
                    requestDto.getToDate(),                // À adapter selon vos méthodes
                    requestDto.getReason()               // À adapter selon vos méthodes
            );

            message.setText(content);

            mailSender.send(message);
        } catch (Exception e) {
            // Since it's async, exceptions won't reach the OrderService
            // You MUST handle/log them here
            System.err.println("❌ Async email error: " + e.getMessage());
        }
    }

    @Override
    public void sendNotificationWithMessage(VacationRequestDto requestDto, List<String> receiverEmails
            , String messageToBeSent ,String statusUpdate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setBcc(receiverEmails.toArray(new String[0]));
            message.setSubject(statusUpdate + " - " + requestDto.getEmployeeDTO().getFirstName() + " "
                    + requestDto.getEmployeeDTO().getLastName());

            String content = String.format(
                    "Bonjour,\n\n" +
                            messageToBeSent + ":\n" +
                            "- Employé : %s\n" +
                            "- Date de début : %s\n" +
                            "- Date de fin : %s\n" +
                            "- Type de congé : %s\n\n",
                    requestDto.getEmployeeDTO().getFirstName() + " " + requestDto.getEmployeeDTO().getLastName(), // À adapter selon vos méthodes
                    requestDto.getFromDate(),              // À adapter selon vos méthodes
                    requestDto.getToDate(),                // À adapter selon vos méthodes
                    requestDto.getReason()               // À adapter selon vos méthodes
            );

            message.setText(content);

            mailSender.send(message);
        } catch (Exception e) {
            // Since it's async, exceptions won't reach the OrderService
            // You MUST handle/log them here
            System.err.println("❌ Async email error: " + e.getMessage());
        }
    }
}
