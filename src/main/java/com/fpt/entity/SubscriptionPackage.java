package com.fpt.entity;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
public class SubscriptionPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Float price;

    private Float discount;

    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;

    private Boolean isActive = true;

    @ManyToMany
    @JoinTable(
            name = "subscription_package_option",
            joinColumns = @JoinColumn(name = "subscription_package_id"),
            inverseJoinColumns = @JoinColumn(name = "option_id")
    )
    private List<Option> options;

    private Long simulatedCount;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum BillingCycle {
        MONTHLY, YEARLY
    }
}
