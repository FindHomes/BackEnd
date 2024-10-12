package com.findhomes.findhomesbe.DTO;

import com.findhomes.findhomesbe.entity.House;

public class ErrorReponse {
    public ErrorReponse(Boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
    private Boolean success;
    private Integer code;
    private String message;
}
