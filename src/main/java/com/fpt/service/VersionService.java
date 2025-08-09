package com.fpt.service;

import com.fpt.dto.DocDTO;
import com.fpt.dto.VersionDTO;
import com.fpt.entity.Doc;
import com.fpt.entity.Option;
import com.fpt.entity.Version;
import com.fpt.repository.VersionRepository;
import com.fpt.specification.DocSpecificationBuilder;
import com.fpt.specification.VersionSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VersionService implements IVersionService {

    private final VersionRepository versionRepository;

    @Override
    public List<VersionDTO> getAll() {
        return versionRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }
    @Override
    public Page<VersionDTO> getAllVersion(Pageable pageable, String search, Boolean isActive) {
        VersionSpecificationBuilder specification = new VersionSpecificationBuilder(search,isActive);
        return versionRepository.findAll(specification.build(), pageable).map(this::toDto);
    }

    @Override
    public Page<VersionDTO> getAllVersionCustomer(Pageable pageable, String search) {
        VersionSpecificationBuilder specification = new VersionSpecificationBuilder(search,true);
        return versionRepository.findAll(specification.build(), pageable).map(this::toDto);
    }

    @Override
    public VersionDTO getById(Long id) {
        return versionRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Version not found"));
    }
    @Override
    public VersionDTO getByIdIfActive(Long id) {
        return versionRepository.findById(id)
                .filter(Version::getIsActive)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Version is inactive or not found"));
    }

    @Override
    public VersionDTO create(VersionDTO dto) {
        Version version = new Version();
        version.setVersion(dto.getVersion());
        version.setDescription(dto.getDescription());
        return toDto(versionRepository.save(version));
    }

    @Override
    public VersionDTO update(Long id, VersionDTO dto) {
        Version version = versionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Version not found with id: " + id));

        version.setVersion(dto.getVersion());
        version.setDescription(dto.getDescription());
        version.setIsActive(dto.getIsActive());


        return toDto(versionRepository.save(version));
    }


    @Override
    public void delete(Long id) {
        Version version = versionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Version not found"));
        versionRepository.delete(version);
    }


    @Override
    public void deleteMore(List<Long> ids) {
        List<Version> versions = versionRepository.findAllById(ids);
        versionRepository.deleteAll(versions);
    }

    private VersionDTO toDto(Version version) {
        return VersionDTO.builder()
                .id(version.getId())
                .version(version.getVersion())
                .description(version.getDescription())
                .isActive(version.getIsActive())
                .createdAt(version.getCreatedAt())
                .updatedAt(version.getUpdatedAt())
                .build();
    }

    private Version toEntity(VersionDTO dto) {
        return Version.builder()
                .version(dto.getVersion())
                .description(dto.getDescription())
                .build();
    }
}
