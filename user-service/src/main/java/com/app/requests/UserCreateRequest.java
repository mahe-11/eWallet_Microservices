package com.app.requests;

import com.app.constants.Constants;
import com.app.enums.UserIdentifier;
import com.app.models.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String userIdentifier;

    @NotBlank
    private String identifierValue;


    public User toUser() {
        return User.builder()
                .name(this.name)
                .phoneNumber(this.phoneNumber)
                .email(this.email)
                .password(this.password)
                .userIdentifier(UserIdentifier.valueOf(this.userIdentifier))
                .identifierValue(this.identifierValue)
                .role(Constants.USER)
                .build();

    }
}
