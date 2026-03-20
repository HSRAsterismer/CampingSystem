package com.camping.system.dto;

import com.camping.system.enums.ObservationStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class ObservationDtos {

    private ObservationDtos() {
    }

    public record CreateObservationRequest(
            @NotNull(message = "露营点不能为空") Long siteId,
            @NotBlank(message = "物种名称不能为空") String speciesName,
            @NotBlank(message = "分类不能为空") String category,
            @NotNull(message = "数量不能为空")
            @Min(value = 1, message = "数量至少为 1")
            Integer quantity,
            String weather,
            @NotNull(message = "观察时间不能为空") LocalDateTime observationTime,
            String photoUrl,
            String coordinates,
            String habitat,
            String rarityLevel,
            Integer environmentalScore,
            @Size(max = 2000, message = "备注长度不能超过 2000")
            String notes
    ) {
    }

    public record ObservationItem(
            Long id,
            Long siteId,
            String siteName,
            String observerName,
            String speciesName,
            String category,
            Integer quantity,
            String weather,
            LocalDateTime observationTime,
            String photoUrl,
            String coordinates,
            String habitat,
            String rarityLevel,
            Integer environmentalScore,
            String notes,
            ObservationStatus status,
            LocalDateTime createdAt
    ) {
    }
}
