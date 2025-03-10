package com.findhomes.findhomesbe.global;

import lombok.Data;

@Data
public class Response {
    public Response(Boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public Response(Boolean success, Integer code, String message, Object result) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.result = result;
    }

    private Boolean success;
    private Integer code;
    private String message;
    private Object result;
}
