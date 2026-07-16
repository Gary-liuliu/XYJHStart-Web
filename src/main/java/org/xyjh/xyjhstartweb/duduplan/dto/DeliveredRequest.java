package org.xyjh.xyjhstartweb.duduplan.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record DeliveredRequest(@NotEmpty List<String> messageIds) {
}
