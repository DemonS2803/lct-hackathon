package com.example.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import com.example.dto.EditPartnerPointDTO;
import com.example.entities.PartnerPoint;
import com.example.repositories.PartnerPointsRepository;

@Service
public class PartnerPointService {
    
    @Autowired
    private PartnerPointsRepository partnerPointsRepository;

    public boolean createNewPartnerPoint(PartnerPoint point) {
        try {
            point.setConnectionDate(LocalDateTime.now());
            point.setEdited(true);
            partnerPointsRepository.save(point);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean editPartnerPoint(EditPartnerPointDTO dto) {
        try {
            PartnerPoint point = partnerPointsRepository.findById(dto.getId()).get();
            point.setAddress(dto.getAddress());
            point.setEdited(true);
            point.setApprovedRequest(dto.getApprovedRequest());
            point.setCardsAndMaterialsDelivered(dto.isCardsAndMaterialsDelivered());
            point.setDaysAfterLastCard(dto.getDaysAfterLastCard());
            point.setDeliveredCardsNumber(dto.getDeliveredCardsNumber());
            partnerPointsRepository.save(point);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deletePoint(Long id) {
        partnerPointsRepository.deleteById(id);
        return true;
    }
}
