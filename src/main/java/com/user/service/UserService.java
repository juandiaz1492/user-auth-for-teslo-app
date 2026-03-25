package com.user.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.user.dto.LoginDto;
import com.user.dto.NestRegisterDto;
import com.user.dto.UserDto;

import com.user.entities.User;
import com.user.mapper.UserMapper;
import com.user.repository.UserRepository;

import jakarta.mail.MessagingException;

@Service
public class UserService {

    private final RestTemplate restTemplate;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Value("${nest.api.url}")
    private String nestApiUrl;

    @Value("${frontend.base.url}")
    private String frontendBaseUrl;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, UserRepository userRepository,
            EmailService emailService, RestTemplate restTemplate) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.restTemplate = restTemplate;
    }

    // REGISTER
    public ResponseEntity<?> register(UserDto dto) {
        // mail repetido
        Optional<User> existingUser = userRepository.findByMail(dto.getMail());
        if (existingUser.isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Ya existe un usuario con ese mail"));
        }

        User usersave = userMapper.dtotoUser(dto);

        String passwordHasheada = passwordEncoder.encode(usersave.getPassword());
        usersave.setPassword(passwordHasheada);
        usersave.setActive(false);

        String token = generateActivationToken();
        usersave.setActivationToken(token);
        usersave.setActivationTokenExpiresAt(LocalDateTime.now().plusHours(24));

        User save = userRepository.save(usersave);

        String activationUrl = frontendBaseUrl + "/auth/verify?token=" + token;
        try {
            emailService.enviarCorreoVerificacion(save.getMail(), activationUrl);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(Map.of("message", "Usuario registrado, revisa tu correo y verifica la cuenta."));
    }

    // LOGIN
    public ResponseEntity<?> login(LoginDto dto) {

        Optional<User> user = userRepository.findByMail(dto.getMail());

        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No existe el usuario con ese mail"));
        }

        if (!user.get().isActive()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Usuario no activo"));
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.get().getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Contraseña incorrecta"));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "email", dto.getMail(),
                    "password", user.get().getPassword());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Object> nestResponse = restTemplate.postForEntity(
                    nestApiUrl + "/auth/login",
                    request,
                    Object.class);

            return ResponseEntity.status(nestResponse.getStatusCode()).body(nestResponse.getBody());

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(
                    Map.of("message", e.getResponseBodyAsString()));

        } catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                    Map.of("message", "Error de conexión con Nest en login"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", e.getMessage()));
        }
    }

    private String generateActivationToken() {
        return UUID.randomUUID().toString();
    }

    // VERIFY
    public ResponseEntity<?> verifyCode(String token) {

        Optional<User> optionalUser = userRepository.findByActivationToken(token);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Token de activación no válido");
        }

        User user = optionalUser.get();

        if (user.isActive()) {
            return ResponseEntity.badRequest().body("La cuenta ya está verificada");
        }

        if (user.getActivationTokenExpiresAt() == null ||
                user.getActivationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("message", "El token ha expirado"));
        }

        NestRegisterDto nestDto = new NestRegisterDto(
                user.getMail(),
                user.getPassword(),
                user.getUsername());
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<NestRegisterDto> request = new HttpEntity<>(nestDto, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    nestApiUrl + "/auth/register",
                    request,
                    String.class);

            System.out.println("STATUS NEST: " + response.getStatusCode());
            System.out.println("BODY NEST: " + response.getBody());

        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            return ResponseEntity.status(e.getStatusCode()).body(
                    Map.of("message", e.getResponseBodyAsString()));

        } catch (ResourceAccessException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                    Map.of("message", "No se pudo conectar con Nest"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Error inesperado al registrar en Nest"));
        }

        user.setActive(true);
        user.setActivationToken(null);
        user.setActivationTokenExpiresAt(null);

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Cuenta verificada correctamente"));
    }
}