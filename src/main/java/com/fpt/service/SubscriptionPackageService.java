package com.fpt.service;

import com.fpt.dto.OptionDTO;
import com.fpt.dto.SubscriptionPackageDTO;
import com.fpt.dto.UserListDTO;
import com.fpt.entity.Option;
import com.fpt.entity.SubscriptionPackage;
import com.fpt.entity.User;
import com.fpt.repository.OptionRepository;
import com.fpt.repository.SubscriptionPackageRepository;
import com.fpt.specification.SubscriptionPackageSpecificationBuilder;
import com.fpt.specification.UserSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionPackageService implements ISubscriptionPackageService {

    private final SubscriptionPackageRepository repository;
    private final OptionRepository optionRepository;
@Autowired
private ModelMapper modelMapper;
    @Override
    public Page<SubscriptionPackageDTO> getAllPackage(Pageable pageable, String search, Boolean isActive, Double minPrice,Double maxPrice,SubscriptionPackage.TypePackage type,SubscriptionPackage.BillingCycle cycle) {
        SubscriptionPackageSpecificationBuilder specification = new SubscriptionPackageSpecificationBuilder(search,isActive,minPrice,maxPrice,type,cycle);
        return repository.findAll(specification.build(), pageable)
                .map(this::toDto);
//                .map(subscription -> modelMapper.map(subscription, SubscriptionPackageDTO.class));
    }

    @Override
    public Page<SubscriptionPackageDTO> getAllPackageCustomer( Pageable pageable, String search, Double minPrice,Double maxPrice,SubscriptionPackage.TypePackage type,SubscriptionPackage.BillingCycle cycle) {
        SubscriptionPackageSpecificationBuilder specification = new SubscriptionPackageSpecificationBuilder(search,true,minPrice,maxPrice,type,cycle);
        return repository.findAll(specification.build(), pageable)
                .map(this::toDto);
//                .map(subscription -> modelMapper.map(subscription, SubscriptionPackageDTO.class));
    }

    @Override
    public List<SubscriptionPackageDTO> convertToDto(List<SubscriptionPackage> subscriptionPackages) {
        List<SubscriptionPackageDTO> subscriptionPackageDTOs = new ArrayList<>();
        for (SubscriptionPackage subscriptionPackage : subscriptionPackages) {
            SubscriptionPackageDTO subscriptionPackageDTO = modelMapper.map(subscriptionPackage, SubscriptionPackageDTO.class);
            subscriptionPackageDTOs.add(subscriptionPackageDTO);
        }
        return subscriptionPackageDTOs;
    }

    @Override
    public List<SubscriptionPackageDTO> getAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public SubscriptionPackageDTO getById(Long id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
    }


//    public SubscriptionPackageDTO create(SubscriptionPackageDTO dto) {
//        return toDto(repository.save(toEntity(dto)));
//    }

    @Override
    public SubscriptionPackageDTO update(Long id, SubscriptionPackageDTO dto) {
        SubscriptionPackage entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setDiscount(dto.getDiscount());
        entity.setBillingCycle(SubscriptionPackage.BillingCycle.valueOf(dto.getBillingCycle()));
        entity.setIsActive(dto.getIsActive());
        entity.setSimulatedCount(dto.getSimulatedCount());

        if (dto.getOptionsId() != null && !dto.getOptionsId().isEmpty()) {
            List<Option> options = optionRepository.findAllById(dto.getOptionsId());
            if (options.size() != dto.getOptionsId().size()) {
                throw new RuntimeException("Some Option IDs not found!");
            }
            entity.setOptions(options);
        } else {
            entity.setOptions(null);
        }


        return toDto(repository.save(entity));
    }




    @Override
    public void delete(Long id) {
        SubscriptionPackage subscription = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        repository.delete(subscription);
    }


    @Override
    public void deleteMore(List<Long> ids) {
        List<SubscriptionPackage> packages = repository.findAllById(ids);
        repository.deleteAll(packages);
    }


    @Override
    public SubscriptionPackageDTO create(SubscriptionPackageDTO dto) {
        SubscriptionPackage entity = new SubscriptionPackage();
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setDiscount(dto.getDiscount());
        entity.setBillingCycle(SubscriptionPackage.BillingCycle.valueOf(dto.getBillingCycle()));
        entity.setTypePackage(SubscriptionPackage.TypePackage.valueOf(dto.getTypePackage()));
        entity.setSimulatedCount(0L);

        if (dto.getOptionsId() != null && !dto.getOptionsId().isEmpty()) {
            List<Option> options = optionRepository.findAllById(dto.getOptionsId());
            if (options.size() != dto.getOptionsId().size()) {
                throw new RuntimeException("One or more options not found");
            }
            entity.setOptions(options);
        }


        SubscriptionPackage saved = repository.save(entity);
        return toDto(saved);
    }



    private SubscriptionPackageDTO toDto(SubscriptionPackage entity) {
        List<OptionDTO> optionDTOs = entity.getOptions().stream()
                .filter(option -> Boolean.TRUE.equals(option.getIsActive()))
                .map(option -> OptionDTO.builder()
                        .id(option.getId())
                        .name(option.getName())
                        .isActive(option.getIsActive())
                        .createdAt(option.getCreatedAt())
                        .updatedAt(option.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return SubscriptionPackageDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .price(entity.getPrice())
                .discount(entity.getDiscount())
                .billingCycle(entity.getBillingCycle().name())
                .typePackage(entity.getTypePackage().name())
                .isActive(entity.getIsActive())
                .options(optionDTOs)
                .simulatedCount(entity.getSimulatedCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

}
