package com.hansal.verrechnungsprogramm.service;

import com.hansal.verrechnungsprogramm.dto.MeatCutAvailabilityDTO;
import com.hansal.verrechnungsprogramm.model.MeatCut;
import com.hansal.verrechnungsprogramm.repository.MeatCutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeatCutService {

    private final MeatCutRepository meatCutRepository;

    public List<MeatCut> getAllMeatCuts() {
        return meatCutRepository.findAll();
    }

    public MeatCut getMeatCutById(Long id) {
        return meatCutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meat cut not found with id: " + id));
    }

    public List<MeatCut> getAvailableMeatCuts() {
        return meatCutRepository.findAllAvailable();
    }

    public List<MeatCut> getMeatCutsBySlaughter(Long slaughterId) {
        return meatCutRepository.findBySlaughterId(slaughterId);
    }

    public List<MeatCut> searchMeatCuts(Long productId, BigDecimal minWeight) {
        return meatCutRepository.findByProductIdAndMinWeight(productId, minWeight);
    }

    public List<MeatCutAvailabilityDTO> getAvailabilityByProduct(Long productId) {
        List<MeatCut> availableMeatCuts = meatCutRepository.findAvailableByProductId(productId);

        return availableMeatCuts.stream()
                .map(meatCut -> new MeatCutAvailabilityDTO(
                        meatCut.getId(),
                        meatCut.getSlaughter().getCowTag(),
                        meatCut.getSlaughter().getCowId(),
                        meatCut.getSlaughter().getSlaughterDate(),
                        meatCut.getAvailableWeight(),
                        meatCut.getTotalWeight(),
                        meatCut.getPricePerKg(),
                        meatCut.getProduct().getName()
                ))
                .collect(Collectors.toList());
    }
}
