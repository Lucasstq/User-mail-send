package dev.com.email.entities;

import dev.com.email.enums.EmailStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Table(name = "tb_email")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailEntity {

    private final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "email_id")
    private UUID emailId;
    private UUID userId;
    @Column(name = "email_from")
    private String emailFrom;
    @Column(name = "email_to")
    private String emailTo;
    @Column(name = "email_subject")
    private String emailSubject;
    @Column(name = "email_body", columnDefinition = "BODY")
    private String emailBody;
    @Column(name = "send_date_email")
    private LocalDateTime sendDateEmail;
    @Enumerated(EnumType.STRING)
    @Column(name = "status_email")
    private EmailStatus statusEmail;


}
