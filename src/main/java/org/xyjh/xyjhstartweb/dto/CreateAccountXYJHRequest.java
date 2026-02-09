package org.xyjh.xyjhstartweb.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.xyjh.xyjhstartweb.util.MultiFormatLocalDateTimeDeserializer;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建XYJH账号请求DTO
 */
@Data
public class CreateAccountXYJHRequest {
    private String account;
    private String password;
    private String accountName;
    @JsonDeserialize(using = MultiFormatLocalDateTimeDeserializer.class)
    private LocalDateTime buyTime;
    private BigDecimal buyPrice;

    private Integer status;
    
    private Integer greenTicket = 0;
    private Integer yellowTicket = 0;
    private String remark;
    private String strongCharacter;
}