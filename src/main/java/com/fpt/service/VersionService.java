package com.fpt.service;

import com.fpt.dto.VersionDTO;
import com.fpt.entity.Version;
import com.fpt.repository.VersionRepository;
import lombok.RequiredArgsConstructor;
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
    public VersionDTO getById(Long id) {
        return versionRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Version not found"));
    }

    @Override
    public VersionDTO create(VersionDTO dto) {
        return toDto(versionRepository.save(toEntity(dto)));
    }

    @Override
    public VersionDTO update(Long id, VersionDTO dto) {
        Version version = versionRepository.findById(id).orElseThrow();
        version.setVersion(dto.getVersion());
        version.setDescription(dto.getDescription());
        return toDto(versionRepository.save(version));
    }

    @Override
    public void delete(Long id) {
        versionRepository.deleteById(id);
    }

    private VersionDTO toDto(Version version) {
        return VersionDTO.builder()
                .id(version.getId())
                .version(version.getVersion())
                .description(version.getDescription())
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
