package org.xyjh.xyjhstartweb.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountXYJHAccountingSummary {
    private BigDecimal huoxinggeTotal;
    private BigDecimal kakaTotal;
    private Integer accountedCount;
}
