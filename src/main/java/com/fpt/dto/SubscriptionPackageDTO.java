package com.fpt.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fpt.entity.Option;
import com.fpt.entity.SubscriptionPackage;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionPackageDTO {
    private Long id;
    private String name;
    private String description;
    private Float price;
    private Float discount;
    private String billingCycle;
    private String typePackage;
    private Boolean isActive;
    private List<OptionDTO> options;
    private List<Long> optionsId;
    private Long simulatedCount;
    private Long realCount;
    private Long totalCount;
    private Boolean popular;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updatedAt;
}
