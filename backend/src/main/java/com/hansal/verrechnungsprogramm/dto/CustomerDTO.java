package com.hansal.verrechnungsprogramm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private String name;
    private String phone;
    private String address;
    private LocalDateTime lastOrderDate;
}
