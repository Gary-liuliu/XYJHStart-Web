package org.xyjh.xyjhstartweb.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.xyjh.xyjhstartweb.util.MultiFormatLocalDateTimeDeserializer;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 更新XYJH账号请求DTO
 */
@Data
public class UpdateAccountXYJHRequest {
    private Long id;
    
    private String account;
    private String password;
    private String accountName;
    @JsonDeserialize(using = MultiFormatLocalDateTimeDeserializer.class)
    private LocalDateTime buyTime;
    private Integer status;
    @JsonDeserialize(using = MultiFormatLocalDateTimeDeserializer.class)
    private LocalDateTime sellTime;
    private BigDecimal sellPrice;
    private BigDecimal buyPrice;
    private Integer greenTicket;
    private Integer yellowTicket;
    private String remark;
    private String strongCharacter;
}