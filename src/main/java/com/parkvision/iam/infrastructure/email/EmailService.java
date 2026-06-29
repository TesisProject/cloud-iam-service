package com.parkvision.iam.infrastructure.email;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final SendGrid sendGrid;
    private final String fromAddress;

    public EmailService(
            @Value("${sendgrid.api-key}") String apiKey,
            @Value("${spring.mail.from}") String fromAddress) {
        this.sendGrid = new SendGrid(apiKey);
        this.fromAddress = fromAddress;
    }

    @Async
    public void sendOtp(String to, String otp) {
        Mail mail = new Mail(
                new Email(fromAddress),
                "Tu código de recuperación — ParkVision",
                new Email(to),
                new Content("text/plain",
                        "Hola,\n\n" +
                        "Tu código de verificación es: " + otp + "\n\n" +
                        "Este código expira en 10 minutos.\n\n" +
                        "Si no solicitaste este código, ignora este mensaje.")
        );

        try {
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            var response = sendGrid.api(request);
            if (response.getStatusCode() >= 400) {
                log.error("SendGrid API error {}: {}", response.getStatusCode(), response.getBody());
            } else {
                log.info("OTP email sent to {} — status {}", to, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
        }
    }
}
