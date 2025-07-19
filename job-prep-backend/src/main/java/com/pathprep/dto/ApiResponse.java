package com.pathprep.dto;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private int statusCode; // <-- Added field

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data) {
        this(success, message, data, 200);
    }

    public ApiResponse(boolean success, String message, T data, int statusCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.statusCode = statusCode;
    }

    // Static factory methods
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, 400); // default statusCode to 400
    }


    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(false, message, null, 500);
    }

    public static <T> ApiResponse<T> error(int statusCode, String message) {
        return new ApiResponse<>(false, message, null, statusCode);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
