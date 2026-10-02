package com.app.controller;

import com.app.dto.UserResponseDTO;
import com.app.models.User;
import com.app.requests.ChangePasswordRequest;
import com.app.requests.UserCreateRequest;
import com.app.requests.WalletResponseDTO;
import com.app.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Object> createUser(@RequestBody UserCreateRequest userCreateRequest) {
        if (userService.createUser(userCreateRequest) != null) {
            return new ResponseEntity<>("User Created!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error In Creating User", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public String login(@RequestBody UserCreateRequest loginRequest) {
        log.info("in controller");
        return userService.verify(loginRequest);
    }

    @PostMapping("/getUsers")
    public List<UserResponseDTO> getUsers(
            @RequestBody List<String> phoneNumbers) {
        log.info("In User controller with Phone Numbers : " + phoneNumbers);
        return userService.findByPhoneNumber(phoneNumbers);
    }

    @GetMapping("/test")
    public String test() {
        log.info("IN Test controller method");
        return " you are authenticated!";
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @PathVariable Long id) {

        User user = userService.getUserById(id);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<User> getUserByPhoneNumber(
            @PathVariable String phoneNumber) {

        User user = userService.getUserByPhoneNumber(phoneNumber);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(
            Authentication authentication) {

        String phoneNumber = authentication.getName();

        User user = userService.getUserByPhoneNumber(phoneNumber);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update")
    public ResponseEntity<User> updateUser(
            @RequestBody UserCreateRequest request) {

        User updatedUser = userService.updateUser(request);

        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id) {

        boolean deleted = userService.deleteUser(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok("User deleted successfully");
    }

    @PatchMapping("/changePassword")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordRequest request) {

        return ResponseEntity.ok(userService.changePassword(request));
    }


    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication authentication) {

        String phoneNumber = authentication.getName();

        Double balance = userService.getWalletBalance(phoneNumber);

        return ResponseEntity.ok(balance);
    }

    @PutMapping("/wallet/add")
    public ResponseEntity<WalletResponseDTO> addMoney(
            @RequestParam Double amount,
            Authentication authentication) {

        String phoneNumber = authentication.getName();

        WalletResponseDTO wallet =
                userService.addMoney(phoneNumber, amount);

        return ResponseEntity.ok(wallet);
    }

}
