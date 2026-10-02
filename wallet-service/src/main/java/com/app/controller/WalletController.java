package com.app.controller;

import com.app.models.Wallet;
import com.app.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    WalletService walletService;

    @GetMapping("/{phoneNumber}")
    public ResponseEntity<Wallet> getWallet(
            @PathVariable String phoneNumber) {

        Wallet wallet = walletService.getWalletByPhoneNumber(phoneNumber);

        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/{phoneNumber}/balance")
    public ResponseEntity<Double> getBalance(@PathVariable String phoneNumber) {
        Double balance = walletService.getBalance(phoneNumber);

        if (balance == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(balance);
    }

    @PutMapping("/{phoneNumber}/add")
    public ResponseEntity<Wallet> addMoney(
            @PathVariable String phoneNumber,
            @RequestParam Double amount) {

        Wallet wallet = walletService.addMoney(phoneNumber, amount);

        if (wallet == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(wallet);
    }
}