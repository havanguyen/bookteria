package com.hanguyen.payment_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    CANT_NOT_SENT_EMAIL(1009, "Can not sent email", HttpStatus.BAD_REQUEST),
    INVALID_FILE(1010, "Invalid file type or size", HttpStatus.BAD_REQUEST),
    UPLOAD_FAILED(1011, "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    ID_AUTHOR_NOT_FOUND(1012, "Id author not found", HttpStatus.NOT_FOUND),
    ID_PUBLISH_NOT_FOUND(1013, "Id publisher not found", HttpStatus.NOT_FOUND),
    ID_PRODUCT_NOT_FOUND(1013, "Id product not found", HttpStatus.NOT_FOUND),
    ID_CATEGORY_NOT_FOUND(1013, "Id category not found", HttpStatus.NOT_FOUND),
    CART_IS_EMPTY(1014, "Cart is empty", HttpStatus.NOT_FOUND)
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
