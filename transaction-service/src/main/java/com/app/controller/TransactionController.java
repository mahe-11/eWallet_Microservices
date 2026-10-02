package com.app.controller;

import com.app.dto.TransactionDTO;
import com.app.dto.UserResponseDTO;
import com.app.service.TransactionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    @Autowired
    RestTemplate restTemplate;

    private final String USER_APP_URL = "http://localhost:6001/user";

    @PostMapping("/transact")
    public ResponseEntity<Object> transact(@RequestBody @Valid TransactionDTO transactionDTO) {
        log.info("In Transaction Service Controller with DTO :" + transactionDTO);



        List<String> phoneNumbers = List.of(transactionDTO.getSender(), transactionDTO.getReceiver());
        log.info("Phone numbers :" + phoneNumbers);
        ResponseEntity<List<UserResponseDTO>> response = restTemplate.exchange(
                USER_APP_URL + "/getUsers",
                HttpMethod.POST,
                new HttpEntity<>(phoneNumbers),
                new ParameterizedTypeReference<>() {
                }
        );

        List<UserResponseDTO> users = response.getBody();
        log.info("User Service response received");
        log.info("Status: {}", response.getStatusCode());
        log.info("Users Retrieved: {}", users);
        if (users == null || users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Sender or receiver not found");
        }

        return ResponseEntity.ok(transactionService.transact(transactionDTO, users));
    }

    @GetMapping("/transactionStatus/{transactionId}")
    public ResponseEntity<Object> getTransactionStatus(@RequestParam String transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionStatus(transactionId));
    }
}
