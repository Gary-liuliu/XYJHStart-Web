package org.xyjh.xyjhstartweb.duduplan.dto;

import jakarta.validation.constraints.NotBlank;

public record ReadRequest(@NotBlank String upToMessageId) {
}
