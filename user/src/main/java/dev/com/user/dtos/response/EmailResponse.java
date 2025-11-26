package dev.com.user.dtos.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public class EmailResponse {

    private UUID userId;
    private String emailFrom;
    private String emailTo;
    private String emailSubject;
    private String emailBody;

    public EmailResponse() {
    }

    public EmailResponse(UUID userId, String emailFrom, String emailTo, String emailSubject, String emailBody) {
        this.userId = userId;
        this.emailFrom = emailFrom;
        this.emailTo = emailTo;
        this.emailSubject = emailSubject;
        this.emailBody = emailBody;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }
}