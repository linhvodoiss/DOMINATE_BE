package com.fpt.entity;
import lombok.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;
@Entity
@Table(name = "PaymentOrder")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE `PaymentOrder` SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private SubscriptionPackage subscriptionPackage;

    @Column(unique = true, nullable = false)
    private Integer orderId;
    private Float price;
    private String paymentLink;

    @Column(name = "payos_bin")
    private String bin;

    @Column(name = "payos_account_name")
    private String accountName;

    @Column(name = "payos_account_number")
    private String accountNumber;

    @Column(columnDefinition = "TEXT")
    private String cancelReason;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod = PaymentMethod.BANK;
    @Column(name = "license_created")
    private Boolean licenseCreated = false;
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    @Column(name = "date_transfer")
    private String dateTransfer;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PaymentStatus {
        PENDING,PROCESSING,SUCCESS, FAILED
    }
    public enum PaymentMethod {
        PAYOS, BANK
    }
}
