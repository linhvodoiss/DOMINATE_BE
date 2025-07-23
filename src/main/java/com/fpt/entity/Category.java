package com.fpt.entity;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE `Category` SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "version_id", nullable = false)
	private Version version;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String slug;
	@Column(name = "`order`")
	private Long order;

	private Boolean isActive = true;
	@Column(name = "is_deleted", nullable = false)
	private Boolean isDeleted = false;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Doc> docs;
}
