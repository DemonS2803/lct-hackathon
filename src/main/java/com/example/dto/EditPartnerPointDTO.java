package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditPartnerPointDTO {
    
    private Long id;
    private String address;
    private boolean isCardsAndMaterialsDelivered;
    private int daysAfterLastCard;
    private int approvedRequest;
    private int deliveredCardsNumber;
}
