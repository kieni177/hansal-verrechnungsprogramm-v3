package com.hansal.verrechnungsprogramm.service;

import com.hansal.verrechnungsprogramm.dto.MeatCutAvailabilityDTO;
import com.hansal.verrechnungsprogramm.model.MeatCut;
import com.hansal.verrechnungsprogramm.repository.MeatCutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeatCutService {

    private final MeatCutRepository meatCutRepository;

    public List<MeatCut> getAllMeatCuts() {
        List<MeatCut> meatCuts = meatCutRepository.findAll();
        log.info("Listed meat cuts: count={}", meatCuts.size());
        return meatCuts;
    }

    public MeatCut getMeatCutById(Long id) {
        MeatCut meatCut = meatCutRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Meat cut not found: id={}", id);
                    return new RuntimeException("Meat cut not found with id: " + id);
                });
        log.info("Fetched meat cut: id={}, product={}", id, meatCut.getProduct().getName());
        return meatCut;
    }

    public List<MeatCut> getAvailableMeatCuts() {
        List<MeatCut> available = meatCutRepository.findAllAvailable();
        log.info("Listed available meat cuts: count={}", available.size());
        return available;
    }

    public List<MeatCut> getMeatCutsBySlaughter(Long slaughterId) {
        List<MeatCut> meatCuts = meatCutRepository.findBySlaughterId(slaughterId);
        log.info("Listed meat cuts for slaughter: slaughterId={}, count={}", slaughterId, meatCuts.size());
        return meatCuts;
    }

    public List<MeatCut> searchMeatCuts(Long productId, BigDecimal minWeight) {
        List<MeatCut> meatCuts = meatCutRepository.findByProductIdAndMinWeight(productId, minWeight);
        log.info("Searched meat cuts: productId={}, minWeight={}, count={}", productId, minWeight, meatCuts.size());
        return meatCuts;
    }

    public List<MeatCutAvailabilityDTO> getAvailabilityByProduct(Long productId) {
        List<MeatCut> availableMeatCuts = meatCutRepository.findAvailableByProductId(productId);

        List<MeatCutAvailabilityDTO> result = availableMeatCuts.stream()
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
        log.info("Fetched availability for product: productId={}, count={}", productId, result.size());
        return result;
    }
}
