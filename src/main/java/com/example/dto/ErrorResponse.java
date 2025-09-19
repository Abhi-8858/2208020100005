package com.example.dto;

public class ErrorResponse {
    private String message;

    // No-args constructor
    public ErrorResponse() {
    }

    // Parameterized constructor
    public ErrorResponse(String message) {
        this.message = message;
    }

    //  Getter
    public String getMessage() {
        return message;
    }

    //  Setter
    public void setMessage(String message) {
        this.message = message;
    }
}
