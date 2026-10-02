package com.app.models;

import com.app.enums.UserIdentifier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true,nullable = false)
    private String phoneNumber;

    @Column(unique = true,nullable = false)
    private long userId;

    private double balance;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserIdentifier userIdentifier;

    @Column(nullable = false)
    private String identifierValue;

    private String email;

}
