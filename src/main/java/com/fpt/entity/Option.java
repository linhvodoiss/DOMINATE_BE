package com.fpt.entity;

import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.List;
import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "`option`")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE `option` SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @ManyToMany(mappedBy = "options")
    private List<SubscriptionPackage> subscriptionPackages;
}
