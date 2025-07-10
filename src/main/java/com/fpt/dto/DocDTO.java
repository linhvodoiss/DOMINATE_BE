package com.fpt.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocDTO {
    private Long id;
    private String title;
    private String slug;
    private String content;
    private Integer order;
    private Boolean isActive;
    private Integer categoryId;
    private Integer versionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
