package com.fpt.entity;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "License")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE `License` SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private SubscriptionPackage subscriptionPackage;
    private Integer orderId;
    @Column(length = 36, unique = true, nullable = false)
    private String licenseKey;

    private Integer duration;

    private String ip;

    @Column(name = "hardware_id")
    private String hardwareId;
    @Column(name = "can_used")
    private Boolean canUsed = true;
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;
}
