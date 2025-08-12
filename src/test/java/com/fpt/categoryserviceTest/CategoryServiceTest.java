package com.fpt.categoryserviceTest;

import com.fpt.dto.CategoryDTO;
import com.fpt.dto.VersionDTO;
import com.fpt.entity.Category;
import com.fpt.entity.Version;
import com.fpt.repository.CategoryRepository;
import com.fpt.repository.VersionRepository;
import com.fpt.service.CategoryService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CategoryServiceTest {

    @Mock CategoryRepository categoryRepository;
    @Mock VersionRepository versionRepository;

    @InjectMocks CategoryService service;

    Version version1;
    Category category1, category2;
    CategoryDTO dto1, dto2;

    @BeforeEach
    void setup() {
        version1 = Version.builder()
                .id(1L)
                .version("1.0")
                .description("Desc")
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build();
        category1 = Category.builder()
                .id(1L)
                .name("Cat1")
                .slug("cat-1")
                .order(1L)
                .isActive(true)
                .version(version1)
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build();
        category2 = Category.builder()
                .id(2L)
                .name("Cat2")
                .slug("cat-2")
                .order(2L)
                .isActive(false)
                .version(version1)
                .createdAt(LocalDateTime.now().minusDays(3))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
        dto1 = CategoryDTO.builder()
                .id(1L)
                .name("Cat1")
                .slug("cat-1")
                .order(1L)
                .isActive(true)
                .versionId(1L)
                .version(VersionDTO.builder()
                        .id(1L)
                        .version("1.0")
                        .description("Desc")
                        .build())
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build();
        dto2 = CategoryDTO.builder()
                .id(2L)
                .name("Cat2")
                .slug("cat-2")
                .order(2L)
                .isActive(false)
                .versionId(1L)
                .version(VersionDTO.builder()
                        .id(1L)
                        .version("1.0")
                        .description("Desc")
                        .build())
                .createdAt(LocalDateTime.now().minusDays(3))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    // 1. getAll
    @Test
    void getAll_normal() {
        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));
        List<CategoryDTO> result = service.getAll();
        assertThat(result).hasSize(2);
    }
    @Test
    void getAll_boundary_emptyList() {
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        List<CategoryDTO> result = service.getAll();
        assertThat(result).isEmpty();
    }
    @Test
    void getAll_abnormal_nullRepo() {
        when(categoryRepository.findAll()).thenThrow(new NullPointerException());
        assertThatThrownBy(() -> service.getAll()).isInstanceOf(NullPointerException.class);
    }
    @Test
    void getAll_externalError() {
        when(categoryRepository.findAll()).thenThrow(new RuntimeException("DB error"));
        assertThatThrownBy(() -> service.getAll())
                .isInstanceOf(RuntimeException.class).hasMessageContaining("DB error");
    }

    // 2. getAllCategory
    @Test
    void getAllCategory_normal() {
        Pageable pageable = PageRequest.of(0, 10);
        when(categoryRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        Page<CategoryDTO> result = service.getAllCategory(pageable, null, true, 1L);
        assertThat(result).hasSize(2);
    }
    @Test
    void getAllCategory_boundary_pageSize1() {
        Pageable pageable = PageRequest.of(0, 1);
        when(categoryRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        Page<CategoryDTO> result = service.getAllCategory(pageable, null, true, 1L);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }
    @Test
    void getAllCategory_abnormal_searchNoResult() {
        Pageable pageable = PageRequest.of(0, 10);
        when(categoryRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        Page<CategoryDTO> result = service.getAllCategory(pageable, "not_found", true, 1L);
        assertThat(result).isEmpty();
    }
    @Test
    void getAllCategory_externalError() {
        Pageable pageable = PageRequest.of(0, 10);
        when(categoryRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        assertThatThrownBy(() -> service.getAllCategory(pageable, null, true, 1L))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("DB error");
    }

    // 3. getAllCategoryCustomer
    @Test
    void getAllCategoryCustomer_normal() {
        Pageable pageable = PageRequest.of(0, 10);
        when(categoryRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        Page<CategoryDTO> result = service.getAllCategoryCustomer(pageable, null, 1L);
        assertThat(result).hasSize(1);
    }
    @Test
    void getAllCategoryCustomer_boundary_noResult() {
        Pageable pageable = PageRequest.of(0, 10);
        when(categoryRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        Page<CategoryDTO> result = service.getAllCategoryCustomer(pageable, "nomatch", 2L);
        assertThat(result).isEmpty();
    }
    @Test
    void getAllCategoryCustomer_abnormal_nullPageable() {
        assertThatThrownBy(() -> service.getAllCategoryCustomer(null, null, 1L))
                .isInstanceOf(NullPointerException.class);
    }
    @Test
    void getAllCategoryCustomer_externalError() {
        Pageable pageable = PageRequest.of(0, 10);
        when(categoryRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        assertThatThrownBy(() -> service.getAllCategoryCustomer(pageable, null, 1L))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("DB error");
    }

    // 4. getByIdIfActive
    @Test
    void getByIdIfActive_normal() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        CategoryDTO result = service.getByIdIfActive(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }
    @Test
    void getByIdIfActive_boundary_inactiveCategory() {
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category2));
        assertThatThrownBy(() -> service.getByIdIfActive(2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("inactive or not found");
    }
    @Test
    void getByIdIfActive_abnormal_notFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getByIdIfActive(99L))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("inactive or not found");
    }
    @Test
    void getByIdIfActive_externalError() {
        when(categoryRepository.findById(anyLong()))
                .thenThrow(new RuntimeException("DB error"));
        assertThatThrownBy(() -> service.getByIdIfActive(1L))
                .isInstanceOf(RuntimeException.class);
    }

    // 5. getById
    @Test
    void getById_normal() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        CategoryDTO result = service.getById(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }
    @Test
    void getById_boundary_id0() {
        when(categoryRepository.findById(0L)).thenReturn(Optional.of(category1));
        CategoryDTO result = service.getById(0L);
        assertThat(result.getId()).isEqualTo(1L);
    }
    @Test
    void getById_abnormal_notFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("Category not found");
    }
    @Test
    void getById_externalError() {
        when(categoryRepository.findById(anyLong()))
                .thenThrow(new RuntimeException("DB error"));
        assertThatThrownBy(() -> service.getById(1L))
                .isInstanceOf(RuntimeException.class);
    }

    // 6. create
    @Test
    void create_normal() {
        CategoryDTO dto = CategoryDTO.builder()
                .name("NewCat")
                .slug("newcat")
                .order(3L)
                .isActive(true)
                .versionId(1L)
                .build();
        when(versionRepository.findById(1L)).thenReturn(Optional.of(version1));
        when(categoryRepository.save(any())).thenReturn(category1);
        CategoryDTO result = service.create(dto);
        assertThat(result.getName()).isEqualTo("Cat1");
    }
    @Test
    void create_boundary_minFields() {
        CategoryDTO dto = CategoryDTO.builder()
                .name("")
                .slug("")
                .order(0L)
                .isActive(true)
                .versionId(1L)
                .build();
        when(versionRepository.findById(1L)).thenReturn(Optional.of(version1));
        when(categoryRepository.save(any())).thenReturn(category1);
        CategoryDTO result = service.create(dto);
        assertThat(result).isNotNull();
    }
    @Test
    void create_abnormal_versionNotFound() {
        CategoryDTO dto = CategoryDTO.builder()
                .name("Cat")
                .versionId(99L)
                .build();
        when(versionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Version not found");
    }
    @Test
    void create_externalError() {
        CategoryDTO dto = CategoryDTO.builder()
                .name("Cat")
                .versionId(1L)
                .build();
        when(versionRepository.findById(1L)).thenReturn(Optional.of(version1));
        when(categoryRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    // 7. update
    @Test
    void update_normal() {
        CategoryDTO dto = CategoryDTO.builder()
                .name("Updated")
                .slug("upd")
                .order(2L)
                .isActive(true)
                .versionId(1L)
                .build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.save(any())).thenReturn(category1);
        CategoryDTO result = service.update(1L, dto);
        assertThat(result.getName()).isEqualTo("Cat1");
    }
    @Test
    void update_boundary_changeVersion() {
        Version version2 = Version.builder().id(2L).version("2.0").description("desc2").build();
        CategoryDTO dto = CategoryDTO.builder()
                .name("Upd")
                .slug("upd")
                .order(2L)
                .isActive(true)
                .versionId(2L)
                .build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(versionRepository.findById(2L)).thenReturn(Optional.of(version2));
        when(categoryRepository.save(any())).thenReturn(category1);
        CategoryDTO result = service.update(1L, dto);
        assertThat(result).isNotNull();
    }
    @Test
    void update_abnormal_idNotFound() {
        CategoryDTO dto = CategoryDTO.builder().name("Upd").build();
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(99L, dto))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("not found");
    }
    @Test
    void update_externalError() {
        CategoryDTO dto = CategoryDTO.builder().name("Upd").versionId(1L).build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("DB error");
    }

    // 8. delete
    @Test
    void delete_normal() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        service.delete(1L);
        verify(categoryRepository).delete(category1);
    }
    @Test
    void delete_boundary_id0() {
        when(categoryRepository.findById(0L)).thenReturn(Optional.of(category1));
        service.delete(0L);
        verify(categoryRepository).delete(category1);
    }
    @Test
    void delete_abnormal_notFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Doc not found");
    }
    @Test
    void delete_externalError() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        doThrow(new RuntimeException("DB error")).when(categoryRepository).delete(any());
        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    // 9. deleteMore
    @Test
    void deleteMore_normal_multiple() {
        List<Long> ids = List.of(1L, 2L);
        when(categoryRepository.findAllById(ids)).thenReturn(List.of(category1, category2));
        doNothing().when(categoryRepository).deleteAll(anyList());
        service.deleteMore(ids);
        verify(categoryRepository).deleteAll(anyList());
    }
    @Test
    void deleteMore_boundary_emptyList() {
        service.deleteMore(Collections.emptyList());
        verify(categoryRepository).deleteAll(anyList());
    }
    @Test
    void deleteMore_abnormal_idsWithNonExisting() {
        List<Long> ids = List.of(99L, 100L);
        when(categoryRepository.findAllById(ids)).thenReturn(Collections.emptyList());
        service.deleteMore(ids);
        verify(categoryRepository).deleteAll(anyList());
    }
    @Test
    void deleteMore_externalError() {
        List<Long> ids = List.of(1L, 2L);
        when(categoryRepository.findAllById(ids)).thenThrow(new RuntimeException("DB error"));
        assertThatThrownBy(() -> service.deleteMore(ids))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("DB error");
    }
}
