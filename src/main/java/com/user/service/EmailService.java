package com.user.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    private final RestTemplate restTemplate;

    public EmailService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void enviarCorreoVerificacion(String to, String activationUrl) {

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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        Map<String, Object> body = Map.of(
                "from", "onboarding@resend.dev",
                "to", to,
                "subject", "Verificación de cuenta - Teslo Shop",
                "html", html);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.resend.com/emails",
                request,
                String.class);

        System.out.println("RESEND STATUS: " + response.getStatusCode());
        System.out.println("RESEND BODY: " + response.getBody());
    }
}