package dev.com.email.enums;

public enum EmailStatus {
    PENDING("PENDING"),
    SENT("SENT"),
    FAILED("FAILED"),
    DELIVERED("DELIVERED");

    private String status;

    EmailStatus(String status) {
        this.status = status;
    }
}
