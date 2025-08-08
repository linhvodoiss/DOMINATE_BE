package com.fpt.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
	private Long id;
	private String name;
	private String slug;
	private Long order;
	private Boolean isActive;
	private Long versionId;
	private VersionDTO version;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
