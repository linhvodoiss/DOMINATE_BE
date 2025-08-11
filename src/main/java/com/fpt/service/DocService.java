package com.fpt.service;

import com.fpt.dto.*;
import com.fpt.entity.*;
import com.fpt.repository.CategoryRepository;
import com.fpt.repository.DocRepository;
import com.fpt.repository.VersionRepository;
import com.fpt.specification.DocSpecificationBuilder;
import com.fpt.specification.PaymentOrderSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    public Page<DocDTO> getAllDoc(Pageable pageable, String search, Boolean isActive,Long categoryId,Long versionId) {
        DocSpecificationBuilder specification = new DocSpecificationBuilder(search,isActive,categoryId,versionId);
        return docRepository.findAll(specification.build(), pageable).map(this::toDto);
    }

    @Override
    public Page<DocDTO> getAllDocCustomer(Pageable pageable, String search,Long categoryId,Long versionId) {
        DocSpecificationBuilder specification = new DocSpecificationBuilder(search,true,categoryId,versionId);
        return docRepository.findAll(specification.build(), pageable).map(this::toDto);
    }

    @Override
    public List<Map<String, Object>> getDocsByAllVersions() {
        List<Version> versions = versionRepository.findByIsActiveTrueOrderByCreatedAtDesc();

        return versions.stream()
                .map(version -> {
                    List<Category> categories = categoryRepository.findByVersionIdAndIsActiveTrue(version.getId());

                    List<Map<String, Object>> categoryList = categories.stream()
                            .map(category -> {
                                List<Map<String, Object>> docList = category.getDocs().stream()
                                        .filter(Doc::getIsActive)
                                        .map(doc -> {
                                            Map<String, Object> docMap = new HashMap<>();
                                            docMap.put("docId", doc.getId());
                                            docMap.put("docName", doc.getTitle());
                                            return docMap;
                                        })
                                        .toList();

                                if (docList.isEmpty()) return null;

                                Map<String, Object> categoryMap = new HashMap<>();
                                categoryMap.put("categoryId", category.getId());
                                categoryMap.put("categoryName", category.getName());
                                categoryMap.put("docs", docList);
                                return categoryMap;
                            })
                            .filter(Objects::nonNull)
                            .toList();

                    Map<String, Object> versionMap = new HashMap<>();
                    versionMap.put("versionId", version.getId());
                    versionMap.put("versionName", version.getVersion());
                    versionMap.put("categories", categoryList);

                    return versionMap;
                })
                .toList();
    }

    public DocDTO getByIdIfActive(Long id) {
        return docRepository.findById(id)
                .filter(Doc::getIsActive)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Doc is inactive or not found"));
    }

    @Override
    public DocDTO getById(Long id) {
        return docRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Doc not found"));
    }

    @Override
    public DocDTO create(DocDTO dto) {
        Category category = categoryRepository.findById(Long.valueOf(dto.getCategoryId()))
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));
        Doc doc=new Doc();
        doc.setTitle(dto.getTitle());
        doc.setSlug(dto.getSlug());
        doc.setOrder(dto.getOrder());
        doc.setContent(dto.getContent());
        doc.setCategory(category);

        return toDto(docRepository.save(doc));
    }

    @Override
    public DocDTO update(Long id, DocDTO dto) {
        Doc doc = docRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doc not found with id: " + id));

        doc.setTitle(dto.getTitle());
        doc.setSlug(dto.getSlug());
        doc.setContent(dto.getContent());
        doc.setOrder(dto.getOrder());
        doc.setIsActive(dto.getIsActive());

        if (dto.getCategoryId() != null && !doc.getCategory().getId().equals(Long.valueOf(dto.getCategoryId()))) {
            Category category = categoryRepository.findById(Long.valueOf(dto.getCategoryId()))
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));
            doc.setCategory(category);
        }

        return toDto(docRepository.save(doc));
    }


    @Override
    public void delete(Long id) {
        Doc doc = docRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doc not found"));
        docRepository.delete(doc);
    }


    @Override
    public void deleteMore(List<Long> ids) {
        List<Doc> docs = docRepository.findAllById(ids);
        docRepository.deleteAll(docs);
    }

    private DocDTO toDto(Doc entity) {
        Category category = entity.getCategory();
        CategoryDTO categoryDTO = null;
        Version version = category.getVersion();
        VersionDTO versionDTO = null;

        if (version != null) {
            versionDTO = VersionDTO.builder()
                    .id(version.getId())
                    .version(version.getVersion())
                    .description(version.getDescription())
                    .isActive(version.getIsActive())
                    .createdAt(version.getCreatedAt())
                    .updatedAt(version.getUpdatedAt())
                    .build();
        }

        if (category != null) {
            categoryDTO = CategoryDTO.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .slug(category.getSlug())
                    .order(category.getOrder())
                    .isActive(category.getIsActive())
                    .versionId(category.getVersion().getId())
                    .version(versionDTO)
                    .createdAt(category.getCreatedAt())
                    .updatedAt(category.getUpdatedAt())
                    .build();
        }

        return DocDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .slug(entity.getSlug())
                .content(entity.getContent())
                .order(entity.getOrder())
                .isActive(entity.getIsActive())
                .categoryId(entity.getCategory().getId())
                .category(categoryDTO)
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
                .category(categoryRepository.findById(dto.getCategoryId()).orElseThrow())
                .build();
    }
}
