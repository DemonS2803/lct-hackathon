package com.example.entities;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "partner_points")
@AllArgsConstructor
@NoArgsConstructor
public class PartnerPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty("id")
    private Long id;
    private String address;
    private int localeX;
    private int localeY;
    private LocalDateTime connectionDate;
    private boolean isCardsAndMaterialsDelivered;
    private int daysAfterLastCard;
    private int approvedRequest;
    private int deliveredCardsNumber;
    
}