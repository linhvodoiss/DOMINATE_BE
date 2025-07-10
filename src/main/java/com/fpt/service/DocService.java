package com.fpt.service;

import com.fpt.dto.DocDTO;
import com.fpt.entity.Doc;
import com.fpt.repository.CategoryRepository;
import com.fpt.repository.DocRepository;
import com.fpt.repository.VersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DocService implements IDocService {

    private final DocRepository docRepository;
    private final CategoryRepository categoryRepository;
    private final VersionRepository versionRepository;

    @Override
    public List<DocDTO> getAll() {
        return docRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public DocDTO getById(Long id) {
        return docRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Doc not found"));
    }

    @Override
    public DocDTO create(DocDTO dto) {
        return toDto(docRepository.save(toEntity(dto)));
    }

    @Override
    public DocDTO update(Long id, DocDTO dto) {
        Doc doc = docRepository.findById(id).orElseThrow();
        doc.setTitle(dto.getTitle());
        doc.setSlug(dto.getSlug());
        doc.setContent(dto.getContent());
        doc.setOrder(dto.getOrder());
        doc.setIsActive(dto.getIsActive());
        return toDto(docRepository.save(doc));
    }

    @Override
    public void delete(Long id) {
        docRepository.deleteById(id);
    }

    private DocDTO toDto(Doc entity) {
        return DocDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .slug(entity.getSlug())
                .content(entity.getContent())
                .order(entity.getOrder())
                .isActive(entity.getIsActive())
                .versionId(Math.toIntExact(entity.getVersion().getId()))
                .categoryId(Math.toIntExact(entity.getCategory().getId()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Doc toEntity(DocDTO dto) {
        return Doc.builder()
                .title(dto.getTitle())
                .slug(dto.getSlug())
                .content(dto.getContent())
                .order(dto.getOrder())
                .isActive(dto.getIsActive())
                .version(versionRepository.findById(Long.valueOf(dto.getVersionId())).orElseThrow())
                .category(categoryRepository.findById(Long.valueOf(dto.getCategoryId())).orElseThrow())
                .build();
    }
}
