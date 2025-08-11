package com.fpt;

import com.fpt.dto.CategoryDTO;
import com.fpt.dto.VersionDTO;
import com.fpt.entity.*;
import com.fpt.repository.CategoryRepository;
import com.fpt.repository.UserRepository;
import com.fpt.repository.VersionRepository;
import com.fpt.service.CategoryService;
import com.fpt.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private VersionRepository versionRepository;

	@InjectMocks
	private CategoryService categoryService;

	private Category category;
	private Version version;

	@BeforeEach
	void setUp() {
		version = Version.builder()
				.id(1L)
				.version("1.0")
				.description("desc")
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build();

		category = Category.builder()
				.id(1L)
				.name("Test category")
				.slug("test-category")
				.order(1l)
				.isActive(true)
				.version(version)
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build();
	}

	// Positive: ADMIN can list categories
	@Test
	void testGetAll_WhenUserIsAdmin_ShouldReturnAllCategories() {
		// Simulate user context with ADMIN role (@WithMockUser(roles = "ADMIN") in Spring)
		when(categoryRepository.findAll()).thenReturn(List.of(category));
		var result = categoryService.getAll();
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("Test category");
		verify(categoryRepository, times(1)).findAll();
	}

	// Negative: Non-ADMIN cannot list categories
	@Test
	void testGetAll_WhenUserIsNotAdmin_ShouldThrowAccessDenied() {
		// Simulate user with USER role or without ADMIN
		assertThrows(AccessDeniedException.class, () -> categoryService.getAll());
		verify(categoryRepository, never()).findAll();
	}




	@Test
	void testGetByIdIfActive_success() {
		when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

		CategoryDTO dto = categoryService.getByIdIfActive(1L);

		assertThat(dto.getId()).isEqualTo(1L);
		assertThat(dto.getIsActive()).isTrue();
	}



	@Test
	void testGetByIdIfActive_inactiveOrNotFound() {
		category.setIsActive(false);
		when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

		assertThatThrownBy(() -> categoryService.getByIdIfActive(1L))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("inactive");
	}

	@Test
	void testGetById_found() {
		when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

		CategoryDTO dto = categoryService.getById(1L);

		assertThat(dto.getName()).isEqualTo("Test category");
	}

	@Test
	void testGetById_notFound() {
		when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> categoryService.getById(99L))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("not found");
	}

	@Test
	void testCreate_success() {
		CategoryDTO dto = CategoryDTO.builder()
				.name("New")
				.slug("new")
				.order(1l)
				.isActive(true)
				.versionId(1L)
				.build();

		when(versionRepository.findById(1L)).thenReturn(Optional.of(version));
		when(categoryRepository.save(any(Category.class))).thenReturn(category);

		CategoryDTO result = categoryService.create(dto);

		assertThat(result.getName()).isEqualTo("Test category");
	}

//	@Test
//	void testUpdate_changeVersion() {
//		CategoryDTO dto = CategoryDTO.builder()
//				.name("Updated")
//				.slug("updated")
//				.order(2L)
//				.isActive(false)
//				.versionId(2L)
//				.build();
//
//		Version newVersion = Version.builder().id(2L).version("2.0").build();
//
//		when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
//		when(versionRepository.findById(2L)).thenReturn(Optional.of(newVersion));
//		when(categoryRepository.save(any(Category.class))).thenReturn(category);
//
//		CategoryDTO result = categoryService.update(1L, dto);
//
//		verify(categoryRepository).save(any(Category.class));
//		assertThat(result.getName()).isEqualTo("Test category");
//	}

	@Test
	void testDelete_success() {
		when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

		categoryService.delete(1L);

		verify(categoryRepository).delete(category);
	}

	@Test
	void testDeleteMore_success() {
		when(categoryRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(category));

		categoryService.deleteMore(List.of(1L, 2L));

		verify(categoryRepository).deleteAll(anyList());
	}


}


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService; // Service containing getAllUser()

	// 1. Multiple users
	@Test
	void getAllUser_multipleUsers() {
		User u1 = User.builder()
				.id(1L).userName("alice").email("alice@example.com")
				.firstName("Alice").lastName("Smith")
				.phoneNumber("0123456789")
				.role(Role.CUSTOMER).status(UserStatus.ACTIVE)
				.isActive(true)
				.createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
				.build();

		User u2 = User.builder()
				.id(2L).userName("bob").email("bob")
				.firstName("Bob").lastName("Johnson")
				.phoneNumber("0987654321")
				.role(Role.CUSTOMER).status(UserStatus.ACTIVE)
				.isActive(true)
				.createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
				.build();

		Pageable pageable = PageRequest.of(0, 10);
		when(userRepository.findAll(any(Specification.class), eq(pageable)))
				.thenReturn(new PageImpl<>(List.of(u1, u2)));

		Page<User> result = userService.getAllUser(pageable, "a", 1, true);

		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getContent()).extracting("userName")
				.containsExactly("alice", "bob");
	}

	// 2. Single user
	@Test
	void getAllUser_singleUser() {
		User u1 = User.builder()
				.id(3L).userName("carol").email("carol@example.com")
				.firstName("Carol").lastName("White")
				.phoneNumber("0111222333")
				.role(Role.CUSTOMER).status(UserStatus.NOT_ACTIVE)
				.isActive(true)
				.createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
				.build();

		Pageable pageable = PageRequest.of(1, 5);
		when(userRepository.findAll(any(Specification.class), eq(pageable)))
				.thenReturn(new PageImpl<>(List.of(u1)));

		Page<User> result = userService.getAllUser(pageable, "carol", 2, true);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getUserName()).isEqualTo("carol");
	}

	// 3. Empty result
	@Test
	void getAllUser_emptyResult() {
		Pageable pageable = PageRequest.of(0, 1);
		when(userRepository.findAll(any(Specification.class), eq(pageable)))
				.thenReturn(Page.<User>empty());

		Page<User> result = userService.getAllUser(pageable, "none", null, false);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).isEmpty();
	}

	// 4. Exception from repository
	@Test
	void getAllUser_repositoryThrowsException() {
		Pageable pageable = PageRequest.of(0, 10);
		when(userRepository.findAll(any(Specification.class), eq(pageable)))
				.thenThrow(new RuntimeException("Test DB error"));

		assertThatThrownBy(() -> userService.getAllUser(pageable, "error", 0, null))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Test DB error");
	}
}

