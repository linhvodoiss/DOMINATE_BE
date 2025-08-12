package com.fpt.userServiceTest;

import com.fpt.dto.UserDTO;
import com.fpt.dto.UserListDTO;
import com.fpt.entity.*;
import com.fpt.exception.AccountBannedException;
import com.fpt.exception.AccountNotActivatedException;
import com.fpt.repository.UserRepository;
import com.fpt.repository.RegistrationUserTokenRepository;
import com.fpt.service.UserService;
import com.fpt.websocket.UserSocketService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock ModelMapper modelMapper;
    @Mock UserSocketService userSocketService;
    @Mock RegistrationUserTokenRepository registrationUserTokenRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    User user1, user2;

    @BeforeEach
    void setup() {
        user1 = new User(
                1L, "john", "john@example.com", "pass",
                "John", "Doe", "0123456789", "John Doe",
                Role.CUSTOMER, UserStatus.ACTIVE, true,
                "avatar1.png", LocalDateTime.now(), LocalDateTime.now()
        );

        user2 = new User(
                2L, "jane", "jane@example.com", "pass",
                "Jane", "Smith", "0987654321", "Jane Smith",
                Role.CUSTOMER, UserStatus.NOT_ACTIVE, false,
                "avatar2.png", LocalDateTime.now(), LocalDateTime.now()
        );
    }

    // -------------------- 1. getAllUser --------------------
    @Test
    void getAllUser_normal_returnsUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(user1, user2), pageable, 2));

        Page<User> result = userService.getAllUser(pageable, null, null, null);
        assertThat(result).hasSize(2);
    }

    @Test
    void getAllUser_boundary_pageSizeOne() {
        Pageable pageable = PageRequest.of(0, 1);
        when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(user1), pageable, 2));

        Page<User> result = userService.getAllUser(pageable, null, null, null);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void getAllUser_abnormal_noResults() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty());

        Page<User> result = userService.getAllUser(pageable, "notExists", null, null);
        assertThat(result).isEmpty();
    }

    @Test
    void getAllUser_externalException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> userService.getAllUser(pageable, null, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    // -------------------- 2. addUserByAdmin --------------------
    @Test
    void addUserByAdmin_normal_newUser() {
        UserDTO dto = new UserDTO();
        dto.setEmail("new@e.com");
        dto.setUserName("newuser");
        dto.setPhoneNumber("123");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUserName(any())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(any())).thenReturn(false);
        when(modelMapper.map(dto, User.class)).thenReturn(user1);
        when(userRepository.save(any())).thenReturn(user1);
        when(modelMapper.map(user1, UserDTO.class)).thenReturn(dto);

        assertThat(userService.addUserByAdmin(dto)).isNotNull();
    }


    @Test
    void addUserByAdmin_boundary_minFields() {
        UserDTO dto = new UserDTO();
        dto.setEmail("a@b.com");
        dto.setUserName("a1111111");
        dto.setPhoneNumber("123456789");
        dto.setPassword("123456");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUserName(any())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(modelMapper.map(dto, User.class)).thenReturn(user1);
        when(userRepository.save(any())).thenReturn(user1);
        when(modelMapper.map(user1, UserDTO.class)).thenReturn(dto);

        UserDTO result = userService.addUserByAdmin(dto);
        assertThat(result).isNotNull();
    }






    @Test
    void addUserByAdmin_abnormal_duplicateEmail() {
        UserDTO dto = new UserDTO();
        dto.setEmail("exist@e.com");
        when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThatThrownBy(() -> userService.addUserByAdmin(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email is exist");
    }

    @Test
    void addUserByAdmin_externalException_repoError() {
        UserDTO dto = new UserDTO();
        dto.setEmail("a@b.com");
        dto.setUserName("a");
        dto.setPhoneNumber("1");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUserName(any())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(any())).thenReturn(false);
        when(modelMapper.map(any(), eq(User.class))).thenReturn(user1);
        when(userRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> userService.addUserByAdmin(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    // -------------------- 3. updateUserByAdmin --------------------
    @Test
    void updateUserByAdmin_normal_updateFields() {
        UserDTO dto = new UserDTO();
        dto.setEmail("a@b.com");
        dto.setUserName("abc");
        dto.setPhoneNumber("123");
        dto.setStatus(UserStatus.ACTIVE);
        dto.setIsActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUserName(any())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(any())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user1);
        when(modelMapper.map(user1, UserDTO.class)).thenReturn(dto);

        assertThat(userService.updateUserByAdmin(1L, dto)).isNotNull();
    }

    @Test
    void updateUserByAdmin_boundary_statusChangeTriggersSocket() {
        UserDTO dto = new UserDTO();
        dto.setEmail(user1.getEmail());
        dto.setUserName(user1.getUserName());
        dto.setPhoneNumber("123");
        dto.setStatus(UserStatus.NOT_ACTIVE);
        dto.setIsActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.existsByPhoneNumber(any())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user1);

        userService.updateUserByAdmin(1L, dto);
        verify(userSocketService).sendUserStatusUpdate(1L);
    }

    @Test
    void updateUserByAdmin_abnormal_userNotFound() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserByAdmin(99L, new UserDTO()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User is not exist");
    }

    @Test
    void updateUserByAdmin_externalException_repoError() {
        UserDTO dto = new UserDTO();
        dto.setEmail("a@b.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUserName(any())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(any())).thenReturn(false);
        when(userRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> userService.updateUserByAdmin(1L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    // -------------------- 4. delete --------------------
    @Test
    void delete_normal_existingId() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(any());
        doNothing().when(userSocketService).sendUserStatusUpdate(anyLong());

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
        verify(userSocketService).sendUserStatusUpdate(1L);
    }

    @Test
    void delete_boundary_idZero() {
        when(userRepository.existsById(0L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(anyLong());
        doNothing().when(userSocketService).sendUserStatusUpdate(anyLong());

        userService.delete(0L);

        verify(userRepository).deleteById(0L);
    }

    @Test
    void delete_abnormal_notFound() {
        when(userRepository.existsById(111L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(111L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Option not found with id: 111");
    }

    @Test
    void delete_externalException_repoError() {
        when(userRepository.existsById(2L)).thenReturn(true);
        doThrow(new RuntimeException("DB error")).when(userRepository).deleteById(2L);

        assertThatThrownBy(() -> userService.delete(2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    // -------------------- 5. findUserByUserName --------------------
    @Test
    void findUserByUserName_normal_activeUser() {
        when(userRepository.findByUserName("john")).thenReturn(user1);

        assertThat(userService.findUserByUserName("john")).isEqualTo(user1);
    }

    @Test
    void findUserByUserName_boundary_notActiveStatus() {
        user2.setStatus(UserStatus.NOT_ACTIVE);
        user2.setIsActive(true);
        when(userRepository.findByUserName("jane")).thenReturn(user2);

        assertThatThrownBy(() -> userService.findUserByUserName("jane"))
                .isInstanceOf(AccountNotActivatedException.class);
    }

    @Test
    void findUserByUserName_abnormal_bannedUser() {
        user2.setStatus(UserStatus.ACTIVE);
        user2.setIsActive(false);
        when(userRepository.findByUserName("jane")).thenReturn(user2);

        assertThatThrownBy(() -> userService.findUserByUserName("jane"))
                .isInstanceOf(AccountBannedException.class);
    }

    @Test
    void findUserByUserName_externalException_usernameNotFound() {
        when(userRepository.findByUserName(any())).thenReturn(null);

        assertThatThrownBy(() -> userService.findUserByUserName("not_exist"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Not found user");
    }

    // -------------------- 6. convertToDto --------------------
    @Test
    void convertToDto_normal_validList() {
        List<User> users = List.of(user1, user2);
        when(modelMapper.map(user1, UserListDTO.class)).thenReturn(new UserListDTO());
        when(modelMapper.map(user2, UserListDTO.class)).thenReturn(new UserListDTO());

        List<UserListDTO> result = userService.convertToDto(users);
        assertThat(result).hasSize(2);
    }

    @Test
    void convertToDto_boundary_emptyList() {
        List<UserListDTO> result = userService.convertToDto(Collections.emptyList());
        assertThat(result).isEmpty();
    }

    @Test
    void convertToDto_abnormal_nullInput() {
        assertThatThrownBy(() -> userService.convertToDto(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void convertToDto_externalException_mapperThrows() {
        List<User> users = List.of(user1);
        when(modelMapper.map(any(), eq(UserListDTO.class)))
                .thenThrow(new RuntimeException("mapping error"));

        assertThatThrownBy(() -> userService.convertToDto(users))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("mapping error");
    }

    // -------------------- 7. activeUser --------------------
    @Test
    void activeUser_normal_activateSuccess() {
        RegistrationUserToken token = new RegistrationUserToken("tk", user1);
        token.setId(1L);
        when(registrationUserTokenRepository.findByToken("tk")).thenReturn(token);
        doNothing().when(registrationUserTokenRepository).deleteById(token.getId());
        when(userRepository.save(any())).thenReturn(user1);

        userService.activeUser("tk");

        verify(userRepository).save(any(User.class));
        verify(registrationUserTokenRepository).deleteById(1L);
        assertThat(user1.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void activeUser_boundary_emptyToken() {
        when(registrationUserTokenRepository.findByToken("")).thenReturn(null);

        assertThatThrownBy(() -> userService.activeUser(""))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void activeUser_abnormal_tokenNotExist() {
        when(registrationUserTokenRepository.findByToken("not_exist")).thenReturn(null);

        assertThatThrownBy(() -> userService.activeUser("not_exist"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void activeUser_externalException_repoError() {
        RegistrationUserToken token = new RegistrationUserToken("tk", user1);
        token.setId(2L);
        when(registrationUserTokenRepository.findByToken("tk")).thenReturn(token);
        doThrow(new RuntimeException("DB error")).when(registrationUserTokenRepository).deleteById(anyLong());

        assertThatThrownBy(() -> userService.activeUser("tk"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }
}
