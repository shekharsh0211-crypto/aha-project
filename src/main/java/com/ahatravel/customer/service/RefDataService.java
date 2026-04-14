package com.ahatravel.customer.service;

import com.ahatravel.customer.dto.response.RefDataResponse;
import com.ahatravel.customer.repository.RefDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RefDataService {

    private final RefDataRepository refDataRepository;

    @Cacheable(value = "refData", key = "#category")
    @Transactional(readOnly = true)
    public List<RefDataResponse> getByCategory(String category) {
        return refDataRepository
                .findAllByCategoryAndActiveTrueOrderBySortOrderAsc(category)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Cacheable(value = "refData", key = "'all'")
    @Transactional(readOnly = true)
    public List<RefDataResponse> getAll() {
        return refDataRepository.findAllByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private RefDataResponse toResponse(com.ahatravel.customer.entity.RefData r) {
        return RefDataResponse.builder()
                .id(r.getId())
                .category(r.getCategory())
                .code(r.getCode())
                .label(r.getLabel())
                .description(r.getDescription())
                .sortOrder(r.getSortOrder())
                .build();
    }
}
