package com.fpt.service;

import com.fpt.dto.SubscriptionPackageDTO;
import com.fpt.entity.SubscriptionPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ISubscriptionPackageService {
    Page<SubscriptionPackageDTO> getAllPackage(Pageable pageable, String search);
    Page<SubscriptionPackageDTO> getAllPackageCustomer(Pageable pageable, String search);
    List<SubscriptionPackageDTO> convertToDto(List<SubscriptionPackage> data);
    List<SubscriptionPackageDTO> getAll();
    SubscriptionPackageDTO getById(Long id);
    SubscriptionPackageDTO create(SubscriptionPackageDTO dto);
    SubscriptionPackageDTO update(Long id, SubscriptionPackageDTO dto);
    void delete(Long id);
    void deleteMore(List<Long> ids);
}
