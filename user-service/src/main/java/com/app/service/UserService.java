package com.app.service;

import com.app.dto.UserResponseDTO;
import com.app.enums.UserIdentifier;
import com.app.models.User;
import com.app.repository.UserRepository;
import com.app.requests.ChangePasswordRequest;
import com.app.requests.UserCreateRequest;
import com.app.requests.WalletResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.app.constants.Constants.*;

@Slf4j
@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JWTService jwtService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RestTemplate restTemplate;

    public String verify(UserCreateRequest userCreateRequest) {

        log.info("In service to auth");
        String phone = userCreateRequest.getPhoneNumber();
        String password = userCreateRequest.getPassword();

        log.info("Phone from request: [{}]", phone);
        log.info("Password present: {}", password != null);

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(phone, password);

        log.info("Token principal: [{}]", token.getPrincipal());
        log.info("Token name: [{}]", token.getName());
        log.info("Token authenticated: {}", token.isAuthenticated());

        Authentication authentication =
                authenticationManager.authenticate(token);
        log.info("In service auth done");

        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(phone);
        }


       return "Failed";
    }

    public User createUser(UserCreateRequest userCreateRequest) {
        User user = userCreateRequest.toUser();
        user.setPassword(encoder.encode(userCreateRequest.getPassword()));
        User savedUser = userRepository.save(user);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(USER_CREATED_TOPIC_USER_ID, savedUser.getId());
        jsonObject.put(USER_CREATED_TOPIC_EMAIL, savedUser.getEmail());
        jsonObject.put(USER_CREATED_TOPIC_PHONE_NUMBER, savedUser.getPhoneNumber());
        jsonObject.put(USER_CREATED_TOPIC_IDENTIFIER_KEY, savedUser.getUserIdentifier());
        jsonObject.put(USER_CREATED_TOPIC_IDENTIFIER_VALUE, savedUser.getIdentifierValue());

        CompletableFuture<SendResult> future = kafkaTemplate.send(USER_CREATED_TOPIC, objectMapper.writeValueAsString(jsonObject));
        future.thenAccept(result -> {
            System.out.println("Message Published successfully");
        });
        return savedUser;
    }

    public List<UserResponseDTO> findByPhoneNumber(List<String> phoneNumbers) {

        List<User> users = userRepository.findByPhoneNumberIn(phoneNumbers);
        return users.stream()
                .map(this::toDTO)
                .toList();
    }

    public UserResponseDTO toDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }


    public User getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    public User updateUser(UserCreateRequest request) {
        User userUpdate = User.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .userIdentifier(UserIdentifier.valueOf(request.getUserIdentifier()))
                .identifierValue(request.getIdentifierValue())
                .build();

        return userRepository.save(userUpdate);
    }

    public boolean deleteUser(Long id) {
        return userRepository.deleteById(id);
    }

    public String changePassword(ChangePasswordRequest request) {

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber());

        if (user == null) {
            return "User not found";
        }

        if (!passwordEncoder.matches(
                request.getOldPassword(),
                user.getPassword())) {

            return "Old password is incorrect";
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        return "Password changed successfully";
    }

    public Double getWalletBalance(String phoneNumber) {
        String url = "http://localhost:6002/wallet/balance/" + phoneNumber;
        return restTemplate.getForObject(url, Double.class);
    }

    public WalletResponseDTO addMoney(String phoneNumber, Double amount) {

        String url = "http://localhost:6002/wallet/" + phoneNumber + "/add?amount=" + amount;

        return restTemplate.exchange(
                url,
                HttpMethod.PUT,
                null,
                WalletResponseDTO.class
        ).getBody();
    }
}
