package com.camping.system.dto;

import com.camping.system.enums.SiteStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class SiteDtos {

    private SiteDtos() {
    }

    public record SiteQuery(
            String keyword,
            String city,
            SiteStatus status,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }

    public record SaveSiteRequest(
            @NotBlank(message = "营地编码不能为空") String code,
            @NotBlank(message = "营地名称不能为空") String name,
            @NotBlank(message = "省份不能为空") String province,
            @NotBlank(message = "城市不能为空") String city,
            @NotBlank(message = "地址不能为空") String address,
            @NotNull(message = "纬度不能为空")
            @DecimalMin(value = "-90.0", message = "纬度超出范围")
            @DecimalMax(value = "90.0", message = "纬度超出范围")
            BigDecimal latitude,
            @NotNull(message = "经度不能为空")
            @DecimalMin(value = "-180.0", message = "经度超出范围")
            @DecimalMax(value = "180.0", message = "经度超出范围")
            BigDecimal longitude,
            @NotNull(message = "容量不能为空")
            @Min(value = 1, message = "容量必须大于 0")
            Integer capacity,
            @NotNull(message = "基础价格不能为空")
            @DecimalMin(value = "0.1", message = "基础价格必须大于 0")
            BigDecimal basePrice,
            @NotNull(message = "营地状态不能为空") SiteStatus status,
            @NotNull(message = "景观等级不能为空")
            @Min(value = 1, message = "景观等级至少为 1")
            Integer scenicLevel,
            @NotNull(message = "生态指数不能为空")
            @Min(value = 1, message = "生态指数至少为 1")
            Integer ecoIndex,
            List<String> facilities,
            List<String> tags,
            String description
    ) {
    }

    public record SiteItem(
            Long id,
            String code,
            String name,
            String province,
            String city,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            Integer capacity,
            Integer availableTents,
            Integer scenicLevel,
            Integer ecoIndex,
            BigDecimal basePrice,
            SiteStatus status,
            BigDecimal occupancyRate,
            List<String> facilities,
            List<String> tags,
            String description
    ) {
    }
}
