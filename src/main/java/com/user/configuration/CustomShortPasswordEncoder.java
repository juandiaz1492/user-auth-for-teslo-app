package com.user.configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CustomShortPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {

        String hash = md5(rawPassword.toString());

        // 🔥 Forzar requisitos en posiciones distintas
        StringBuilder sb = new StringBuilder(hash);

        // posición 0 → mayúscula
        sb.setCharAt(0, 'A');

        // posición 1 → minúscula
        sb.setCharAt(1, 'a');

        // posición 2 → número
        sb.setCharAt(2, '1');

        return sb.toString(); // 32 chars siempre
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}