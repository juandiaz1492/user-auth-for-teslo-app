package com.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreoVerificacion(String to, String activationUrl) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Verificación de cuenta - Teslo Shop");

        String html = """
                <div style="font-family: Arial, sans-serif; text-align: center; background-color: #0b1a2b; padding: 30px; color: white;">

                    <img src="https://i.imgur.com/U7oaCvJ.png" style="width: 200px; height: auto; margin-bottom: 25px;" />

                    <h2>Activa tu cuenta</h2>

                    <p>Gracias por registrarte en <strong>Teslo Shop</strong>.</p>

                    <p>Para completar el proceso, haz clic en el botón:</p>

                    <a href="%s"
                       style="display: inline-block; padding: 12px 25px; margin-top: 20px;
                              background-color: #e50914; color: white; text-decoration: none;
                              border-radius: 5px; font-weight: bold;">
                        ACTIVAR CUENTA
                    </a>

                    <p style="margin-top: 30px; font-size: 12px; color: #cccccc;">
                        Si no has solicitado este registro, puedes ignorar este correo.
                    </p>

                </div>
                """
                .formatted(activationUrl);

        helper.setText(html, true);

        mailSender.send(message);
    }
}