package com.fpt.entity;

import lombok.*;
import java.util.List;
import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "`option`")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "options")
    private List<SubscriptionPackage> subscriptionPackages;
}
