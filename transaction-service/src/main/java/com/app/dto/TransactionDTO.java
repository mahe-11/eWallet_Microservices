package com.app.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {

    @NotBlank
    private String sender;

    @NotBlank
    private String receiver;

    @NotBlank
    @Positive
    private double amount;

    private String reason;

}
