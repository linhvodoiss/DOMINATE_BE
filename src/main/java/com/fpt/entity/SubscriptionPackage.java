package com.fpt.entity;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "SubscriptionPackage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE `SubscriptionPackage` SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class SubscriptionPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    private Float price;

    private Float discount;

    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;

    @Enumerated(EnumType.STRING)
    private TypePackage typePackage;


    private Boolean isActive = true;
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @ManyToMany
    @JoinTable(
            name = "subscription_package_option",
            joinColumns = @JoinColumn(name = "subscription_package_id"),
            inverseJoinColumns = @JoinColumn(name = "option_id")
    )
    private List<Option> options;

    private Long simulatedCount=0L;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum BillingCycle {
        MONTHLY,
        HALF_YEARLY,
        YEARLY
    }
    public enum TypePackage {
        DEV, RUNTIME
    }
}
