package com.fpt.service;

import com.fpt.dto.CategoryDTO;
import com.fpt.dto.DocDTO;
import com.fpt.dto.VersionDTO;
import com.fpt.entity.Category;
import com.fpt.entity.Doc;
import com.fpt.entity.Version;
import com.fpt.repository.CategoryRepository;
import com.fpt.repository.VersionRepository;
import com.fpt.specification.CategorySpecificationBuilder;
import com.fpt.specification.DocSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService implements ICategoryService {

	private final CategoryRepository categoryRepository;
	private final VersionRepository versionRepository;

	@Override
	public List<CategoryDTO> getAll() {
		return categoryRepository.findAll().stream()
				.map(this::toDto)
				.toList();
	}

	@Override
	public Page<CategoryDTO> getAllCategory(Pageable pageable, String search, Boolean isActive, Long versionId) {
		CategorySpecificationBuilder specification = new CategorySpecificationBuilder(search,isActive,versionId);
		return categoryRepository.findAll(specification.build(), pageable).map(this::toDto);
	}

	@Override
	public Page<CategoryDTO> getAllCategoryCustomer(Pageable pageable, String search,Long versionId) {
		CategorySpecificationBuilder specification = new CategorySpecificationBuilder(search,true,versionId);
		return categoryRepository.findAll(specification.build(), pageable).map(this::toDto);
	}

	public CategoryDTO getByIdIfActive(Long id) {
		return categoryRepository.findById(id)
				.filter(Category::getIsActive)
				.map(this::toDto)
				.orElseThrow(() -> new RuntimeException("Category is inactive or not found"));
	}

	@Override
	public CategoryDTO getById(Long id) {
		return categoryRepository.findById(id)
				.map(this::toDto)
				.orElseThrow(() -> new RuntimeException("Category not found"));
	}

	@Override
	public CategoryDTO create(CategoryDTO dto) {
		Version version = versionRepository.findById(dto.getVersionId())
				.orElseThrow(() -> new RuntimeException("Version not found with id: " + dto.getVersionId()));
Category category=new Category();
		category.setName(dto.getName());
		category.setSlug(dto.getSlug());
		category.setOrder(dto.getOrder());
		category.setVersion(version);
		return toDto(categoryRepository.save(category));
	}

	@Override
	public CategoryDTO update(Long id, CategoryDTO dto) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

		category.setName(dto.getName());
		category.setSlug(dto.getSlug());
		category.setOrder(dto.getOrder());
		category.setIsActive(dto.getIsActive());

		if (dto.getVersionId() != null && !category.getVersion().getId().equals(dto.getVersionId())) {
			Version version = versionRepository.findById(dto.getVersionId())
					.orElseThrow(() -> new RuntimeException("Version not found with id: " + dto.getVersionId()));
			category.setVersion(version);
		}

		return toDto(categoryRepository.save(category));
	}


	@Override
	public void delete(Long id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Doc not found"));
		categoryRepository.delete(category);
	}


	@Override
	public void deleteMore(List<Long> ids) {
		List<Category> categories = categoryRepository.findAllById(ids);
		categoryRepository.deleteAll(categories);
	}

	private CategoryDTO toDto(Category entity) {
		Version version = entity.getVersion();
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

		return CategoryDTO.builder()
				.id(entity.getId())
				.name(entity.getName())
				.slug(entity.getSlug())
				.order(entity.getOrder())
				.isActive(entity.getIsActive())
				.versionId(entity.getVersion().getId())
				.version(versionDTO)
				.createdAt(entity.getCreatedAt())
				.updatedAt(entity.getUpdatedAt())
				.build();
	}

	private Category toEntity(CategoryDTO dto) {
		return Category.builder()
				.name(dto.getName())
				.slug(dto.getSlug())
				.order(dto.getOrder())
				.isActive(dto.getIsActive())
				.version(versionRepository.findById(dto.getVersionId()).orElseThrow())
				.build();
	}
}
