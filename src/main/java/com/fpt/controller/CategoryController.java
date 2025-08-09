package com.fpt.controller;

import com.fpt.dto.CategoryDTO;
import com.fpt.dto.DocDTO;
import com.fpt.payload.PaginatedResponse;
import com.fpt.payload.SuccessNoResponse;
import com.fpt.payload.SuccessResponse;
import com.fpt.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

	private final ICategoryService service;
	    @GetMapping("/list")
		public ResponseEntity<SuccessResponse<List<CategoryDTO>>> getAll() {
			List<CategoryDTO> categories = service.getAll();
			return ResponseEntity.ok(
					new SuccessResponse<>(200, "Get all categories successfully!", categories)
			);
		}

	@GetMapping()
	public ResponseEntity<PaginatedResponse<CategoryDTO>> getAllCategories(
			@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) Boolean isActive,
			@RequestParam(required = false) Long versionId

	) {
		Page<CategoryDTO> dtoPage = service.getAllCategory(pageable, search,isActive,versionId);
		PaginatedResponse<CategoryDTO> response = new PaginatedResponse<>(dtoPage, HttpServletResponse.SC_OK, "Take list category successfully.");
		return ResponseEntity.ok(response);
	}
	@GetMapping("/customer")
	public ResponseEntity<PaginatedResponse<CategoryDTO>> getAllCategoriesCustomer(
			@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) Long versionId

	) {
		Page<CategoryDTO> dtoPage = service.getAllCategoryCustomer(pageable, search,versionId);
		PaginatedResponse<CategoryDTO> response = new PaginatedResponse<>(dtoPage, HttpServletResponse.SC_OK, "Take list category successfully.");
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<SuccessResponse<CategoryDTO>> getById(@PathVariable Long id) {
		CategoryDTO dto = service.getByIdIfActive(id);
		if (dto == null) {
			return ResponseEntity.status(404).body(new SuccessResponse<>(404, "Category not found or inactive", null));
		}
		return ResponseEntity.ok(new SuccessResponse<>(200, "Get category successfully!", dto));
	}

	@PostMapping
	public ResponseEntity<SuccessResponse<CategoryDTO>> create(@RequestBody CategoryDTO dto) {
		try {
			CategoryDTO saved = service.create(dto);
			return ResponseEntity.ok(new SuccessResponse<>(200, "Create successfully!", saved));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new SuccessResponse<>(500, "Create failed!", null));
		}

	}

	@PutMapping("/{id}")
	public ResponseEntity<SuccessResponse<CategoryDTO>> update(@PathVariable Long id, @RequestBody CategoryDTO dto) {
		try {
			CategoryDTO saved = service.update(id, dto);
			return ResponseEntity.ok(new SuccessResponse<>(200, "Update successfully!", saved));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new SuccessResponse<>(500, "Update failed!", null));
		}
	}
	@DeleteMapping("/{id}")
	public ResponseEntity<SuccessNoResponse> delete(@PathVariable Long id) {
		try {
			service.delete(id);
			return ResponseEntity.ok(new SuccessNoResponse(200, "Delete successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new SuccessNoResponse(500, "Delete failed!"));
		}
	}

	@DeleteMapping
	public ResponseEntity<SuccessNoResponse> deleteMore(@RequestBody List<Long> ids) {
		try {
			service.deleteMore(ids);
			return ResponseEntity.ok(new SuccessNoResponse(200, "Delete successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new SuccessNoResponse(500, "Delete failed!"));
		}
	}
}
