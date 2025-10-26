package org.xyjh.xyjhstartweb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
//这个类代表服务器成功激活后返回给客户端的 JSON 数据。
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivationResponse {
    private String token; // The JWT
}