package com.app.requests;

import lombok.Data;

@Data
public class WalletResponseDTO {
    private Long id;
    private String phoneNumber;
    private Double balance;
}
