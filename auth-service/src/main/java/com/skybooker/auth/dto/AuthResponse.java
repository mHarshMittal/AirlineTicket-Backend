package com.skybooker.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // null fields hide karega
public class AuthResponse {

    private String message;
    private String token;

    // Used for registration or general success responses.
    public static AuthResponse successMessage(String msg){
        AuthResponse res = new AuthResponse();
        res.setMessage(msg);
        return res;
    }

    // Used for login success response, Return token
    public static AuthResponse token(String token){
        AuthResponse res = new AuthResponse();
        res.setToken(token);
        return res;
    }

    
}