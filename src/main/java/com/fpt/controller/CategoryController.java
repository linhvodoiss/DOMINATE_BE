package com.fpt.controller;

import com.fpt.dto.CategoryDTO;
import com.fpt.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

	private final ICategoryService service;

	@GetMapping
	public List<CategoryDTO> getAll() {
		return service.getAll();
	}

	@GetMapping("/{id}")
	public CategoryDTO getById(@PathVariable Long id) {
		return service.getById(id);
	}

	@PostMapping
	public CategoryDTO create(@RequestBody CategoryDTO dto) {
		return service.create(dto);
	}

	@PutMapping("/{id}")
	public CategoryDTO update(@PathVariable Long id, @RequestBody CategoryDTO dto) {
		return service.update(id, dto);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		service.delete(id);
	}
}
