package com.fpt.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VersionDTO {
    private Long id;
    private String version;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
