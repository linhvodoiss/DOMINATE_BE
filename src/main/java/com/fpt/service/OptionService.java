package com.fpt.service;

import com.fpt.dto.LicenseDTO;
import com.fpt.dto.OptionDTO;
import com.fpt.entity.Option;
import com.fpt.repository.OptionRepository;
import com.fpt.specification.LicenseSpecificationBuilder;
import com.fpt.specification.OptionSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OptionService implements IOptionService {

    private final OptionRepository optionRepository;
    @Override
    public Page<OptionDTO> getAllOptions(Pageable pageable, String search, Boolean isActive) {
        OptionSpecificationBuilder specification = new OptionSpecificationBuilder(search,isActive);
        return optionRepository.findAll(specification.build(), pageable)
                .map(this::toDto);
    }
    @Override
    public List<OptionDTO> getAll() {
        return optionRepository.findAll().stream()
                .filter(Option::getIsActive)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public OptionDTO getById(Long id) {
        Option option = optionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Option not found with id: " + id));
        return toDto(option);
    }

    @Override
    public OptionDTO create(OptionDTO dto) {
        Option option = new Option();
        option.setName(dto.getName());
        return toDto(optionRepository.save(option));
    }

    @Override
    public OptionDTO update(Long id, OptionDTO dto) {
        Option option = optionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Option not found with id: " + id));
        option.setName(dto.getName());
        option.setIsActive(dto.getIsActive());
        return toDto(optionRepository.save(option));
    }

    @Override
    public void delete(Long id) {
        if (!optionRepository.existsById(id)) {
            throw new RuntimeException("Option not found with id: " + id);
        }
        optionRepository.deleteById(id);
    }
    @Override
    public void deleteMany(List<Long> ids) {
        List<Option> options = optionRepository.findAllById(ids);
        if (options.size() != ids.size()) {
            throw new RuntimeException("One or more Option IDs not found!");
        }
        optionRepository.deleteAll(options);
    }

    private OptionDTO toDto(Option option) {
        return OptionDTO.builder()
                .id(option.getId())
                .name(option.getName())
                .isActive(option.getIsActive())
                .createdAt(option.getCreatedAt())
                .updatedAt(option.getUpdatedAt())
                .build();
    }
}
