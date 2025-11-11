package com.hanguyen.payment_service.dto.reponse;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayIpnResponse {
    String RspCode;
    String Message;
}
