package com.fpt.service;

import com.fpt.dto.CategoryDTO;
import com.fpt.entity.Category;
import com.fpt.repository.CategoryRepository;
import com.fpt.repository.VersionRepository;
import lombok.RequiredArgsConstructor;
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
	public CategoryDTO getById(Long id) {
		return categoryRepository.findById(id)
				.map(this::toDto)
				.orElseThrow(() -> new RuntimeException("Category not found"));
	}

	@Override
	public CategoryDTO create(CategoryDTO dto) {
		return toDto(categoryRepository.save(toEntity(dto)));
	}

	@Override
	public CategoryDTO update(Long id, CategoryDTO dto) {
		Category category = categoryRepository.findById(id).orElseThrow();
		category.setName(dto.getName());
		category.setSlug(dto.getSlug());
		category.setOrder(dto.getOrder());
		category.setIsActive(dto.getIsActive());
		return toDto(categoryRepository.save(category));
	}

	@Override
	public void delete(Long id) {
		categoryRepository.deleteById(id);
	}

	private CategoryDTO toDto(Category entity) {
		return CategoryDTO.builder()
				.id(entity.getId())
				.name(entity.getName())
				.slug(entity.getSlug())
				.order(entity.getOrder())
				.isActive(entity.getIsActive())
				.versionId(entity.getVersion().getId())
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
