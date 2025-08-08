package com.fpt.service;

import java.util.List;

import com.fpt.dto.CategoryDTO;
import com.fpt.dto.DocDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.fpt.form.CategoryFormForCreating;
import com.fpt.form.CategoryFormForUpdating;
import com.fpt.entity.Category;

public interface ICategoryService {

//	Page<Category> getAllCategorys(Pageable pageable, String search);
//
//	boolean isCategoryExistsByName(String name);
//
//	void createCategory(CategoryFormForCreating form);
//
//	public Category getCategoryByID(int id);
//
//	public void updateCategory(CategoryFormForUpdating form);
//
//	public void deleteCategorys(List<Integer> ids);
List<CategoryDTO> getAll();
	Page<CategoryDTO> getAllCategory(Pageable pageable, String search, Boolean isActive, Long versionId);
	Page<CategoryDTO> getAllCategoryCustomer(Pageable pageable, String search,Long versionId);
	CategoryDTO getByIdIfActive(Long id);
	CategoryDTO getById(Long id);
	CategoryDTO create(CategoryDTO dto);
	CategoryDTO update(Long id, CategoryDTO dto);
	void delete(Long id);
	void deleteMore(List<Long> ids);

}
