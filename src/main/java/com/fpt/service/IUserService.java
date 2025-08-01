package com.fpt.service;
import com.fpt.dto.UserDTO;
import com.fpt.dto.UserListDTO;
import com.fpt.entity.UserStatus;
import com.fpt.form.ChangePasswordForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.fpt.dto.ChangePublicProfileDTO;
import com.fpt.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IUserService extends UserDetailsService {

	Page<User> getAllUser(Pageable pageable, String search, Integer status, Boolean isActive);
	UserDTO addUserByAdmin(UserDTO dto);
	UserDTO updateUserByAdmin(Long userId, UserDTO dto);
	void delete(Long id);
	void deleteMany(List<Long> ids);
	void createUser(User user);

	User findUserByEmail(String email);

	User findUserByUserName(String username);

	void activeUser(String token);

	void sendConfirmUserRegistrationViaEmail(String email);

	boolean existsUserByEmail(String email);

	boolean existsUserByUserName(String userName);

	boolean existsUserByPhoneNumber(String phoneNumber);
	void resetPasswordViaEmail(String email);

	void resetPassword(String token, String newPassword);

	void sendResetPasswordViaEmail(String email);

	UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
	
	void changeUserProfile(String username, ChangePublicProfileDTO dto);
	void changePasswordUser(Long userId, ChangePasswordForm newPassword);
	void changePasswordAdmin(Long userId, ChangePasswordForm newPassword);
	String updateUserAvatar(String username, MultipartFile file) throws IOException;
	Boolean updateActiveStatus(Long userId);
	UserDTO UpdateUserInformation(Long userId, ChangePublicProfileDTO dto);
	List<UserListDTO> convertToDto(List<User> data);
	Long countCustomerAccounts();
}
