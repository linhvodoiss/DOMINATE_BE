package com.fpt.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fpt.dto.UserDTO;
import com.fpt.dto.UserListDTO;
import com.fpt.entity.*;
import com.fpt.exception.AccountBannedException;
import com.fpt.exception.AccountNotActivatedException;
import com.fpt.form.ChangePasswordForm;
import com.fpt.specification.UserSpecificationBuilder;
import com.fpt.websocket.PaymentSocketService;
import com.fpt.websocket.UserSocketService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fpt.dto.ChangePublicProfileDTO;
import com.fpt.event.OnResetPasswordViaEmailEvent;
import com.fpt.event.OnSendRegistrationUserConfirmViaEmailEvent;
import com.fpt.repository.RegistrationUserTokenRepository;
import com.fpt.repository.ResetPasswordTokenRepository;
import com.fpt.repository.UserRepository;
import org.springframework.web.multipart.MultipartFile;

@Component
@Transactional
public class UserService implements IUserService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private FileService fileService;

	@Autowired
	private RegistrationUserTokenRepository registrationUserTokenRepository;

	@Autowired
	private ResetPasswordTokenRepository resetPasswordTokenRepository;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ModelMapper modelMapper;
	private final UserSocketService userSocketService;
	@Autowired
	public UserService(UserRepository userRepository, ModelMapper modelMapper, UserSocketService userSocketService) {
		this.userRepository = userRepository;
		this.modelMapper = modelMapper;
		this.userSocketService = userSocketService;
	}
	@Override
	public Page<User> getAllUser(Pageable pageable, String search,Integer status,Boolean isActive) {
		UserSpecificationBuilder specification = new UserSpecificationBuilder(search, status,isActive);
		return userRepository.findAll(specification.build(), pageable);
	}

	public UserDTO addUserByAdmin(UserDTO dto) {
		if (userRepository.existsByEmail(dto.getEmail())) {
			throw new IllegalArgumentException("Email is exist");
		}
		if (userRepository.existsByUserName(dto.getUserName())) {
			throw new IllegalArgumentException("Username is exist");
		}
		if (userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
			throw new IllegalArgumentException("Phone number is exist");
		}

		User user = modelMapper.map(dto, User.class);
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setStatus(UserStatus.ACTIVE);
		user.setIsActive(true);
		User saved = userRepository.save(user);
		return modelMapper.map(saved, UserDTO.class);
	}

	public UserDTO updateUserByAdmin(Long userId, UserDTO dto) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User is not exist"));

		if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
			throw new IllegalArgumentException("Email is exist");
		}
		if (!user.getUserName().equals(dto.getUserName()) && userRepository.existsByUserName(dto.getUserName())) {
			throw new IllegalArgumentException("Username is exist");
		}
		if (!user.getPhoneNumber().equals(dto.getPhoneNumber()) && userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
			throw new IllegalArgumentException("Phone number is exist");
		}


		boolean statusChanged = !user.getStatus().equals(dto.getStatus());
		boolean isActiveChanged = !user.getIsActive().equals(dto.getIsActive());

		user.setFirstName(dto.getFirstName());
		user.setLastName(dto.getLastName());
		user.setEmail(dto.getEmail());
		user.setPhoneNumber(dto.getPhoneNumber());
		user.setAvatarUrl(dto.getAvatarUrl());
		user.setRole(dto.getRole());
		user.setIsActive(dto.getIsActive());
		user.setStatus(dto.getStatus());
		user.setUpdatedAt(LocalDateTime.now());

		User saved = userRepository.save(user);

		// Send socket
		if (statusChanged || isActiveChanged) {
			userSocketService.sendUserStatusUpdate(userId);
		}

		return modelMapper.map(saved, UserDTO.class);
	}


	@Override
	public void delete(Long id) {
		if (!userRepository.existsById(id)) {
			throw new RuntimeException("Option not found with id: " + id);
		}
		userRepository.deleteById(id);
		userSocketService.sendUserStatusUpdate(id);
	}
	@Override
	public void deleteMany(List<Long> ids) {
		List<User> users = userRepository.findAllById(ids);
		if (users.size() != ids.size()) {
			throw new RuntimeException("One or more Option IDs not found!");
		}
		userRepository.deleteAll(users);
		for (User user : users) {
			userSocketService.sendUserStatusUpdate(user.getId());
		}
	}


	public List<UserListDTO> convertToDto(List<User> users) {
		List<UserListDTO> userDTOs = new ArrayList<>();
		for (User user : users) {
            UserListDTO userDTO = modelMapper.map(user, UserListDTO.class);
			userDTOs.add(userDTO);
		}
		return userDTOs;
	}

	@Override
	public void createUser(User user) {

		// encode password
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setIsActive(true);
		user.setStatus(UserStatus.NOT_ACTIVE);
		// create user
		userRepository.save(user);
		// create new user registration token
		createNewRegistrationUserToken(user);

		// send email to confirm
		sendConfirmUserRegistrationViaEmail(user.getEmail());
	}

	private void createNewRegistrationUserToken(User user) {

		// create new token for confirm Registration
		final String newToken = UUID.randomUUID().toString();
		RegistrationUserToken token = new RegistrationUserToken(newToken, user);

		registrationUserTokenRepository.save(token);
	}

	@Override
	public void sendConfirmUserRegistrationViaEmail(String email) {
		eventPublisher.publishEvent(new OnSendRegistrationUserConfirmViaEmailEvent(email));
	}

	@Override
	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public User findUserByUserName(String username) {
		User user = userRepository.findByUserName(username);

		if (user == null) {
			throw new UsernameNotFoundException("Not found user");
		}

		if (user.getStatus() == UserStatus.NOT_ACTIVE) {
			throw new AccountNotActivatedException();
		}

		if (Boolean.FALSE.equals(user.getIsActive())) {
			throw new AccountBannedException();
		}

		return user;
	}


	@Override
	public boolean existsUserByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	@Override
	public boolean existsUserByUserName(String userName) {
		return userRepository.existsByUserName(userName);
	}

	@Override
	public boolean existsUserByPhoneNumber(String phoneNumber) {
		return userRepository.existsByPhoneNumber(phoneNumber);
	}

	@Override
	public void activeUser(String token) {

		// get token
		RegistrationUserToken registrationUserToken = registrationUserTokenRepository.findByToken(token);

		// active user
		User user = registrationUserToken.getUser();
		user.setStatus(UserStatus.ACTIVE);
		userRepository.save(user);

		// remove Registration User Token
		registrationUserTokenRepository.deleteById(registrationUserToken.getId());
	}

	@Override
	public void resetPasswordViaEmail(String email) {

		// find user by email
		User user = findUserByEmail(email);

		// remove token token if exists
		resetPasswordTokenRepository.deleteByUserId(user.getId());

		// create new reset password token
		createNewResetPasswordToken(user);

		// send email
		sendResetPasswordViaEmail(email);
	}

	@Override
	public void sendResetPasswordViaEmail(String email) {
		eventPublisher.publishEvent(new OnResetPasswordViaEmailEvent(email));
	}

	private void createNewResetPasswordToken(User user) {

		// create new token for Reseting password
		final String newToken = UUID.randomUUID().toString();
		ResetPasswordToken token = new ResetPasswordToken(newToken, user);

		resetPasswordTokenRepository.save(token);
	}

	@Override
	public void resetPassword(String token, String newPassword) {
		// get token
		ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository.findByToken(token);

		// change password
		User user = resetPasswordToken.getUser();
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		// remove Reset Password
		resetPasswordTokenRepository.deleteById(resetPasswordToken.getId());
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// Check user exists by username
		User user = userRepository.findByUserName(username);

		if (user == null) {
			throw new UsernameNotFoundException(username);
		}

		return new org.springframework.security.core.userdetails.User(user.getUserName(), user.getPassword(),
				AuthorityUtils.createAuthorityList(String.valueOf(user.getRole())));
	}

	@Override
	public void changeUserProfile(String username, ChangePublicProfileDTO dto) {
		User user = userRepository.findByUserName(username);
		
		user.setAvatarUrl(dto.getAvatarUrl());
		user.setFirstName(dto.getFirstName());
		user.setLastName(dto.getLastName());
//		user.setEmail(dto.getEmail());
		user.setFullName(dto.getFirstName() + dto.getLastName());
		user.setPhoneNumber(dto.getPhoneNumber());
		userRepository.save(user);
		
		// TODO other field
	}

	@Override
	public void changePasswordUser(Long userId, ChangePasswordForm form) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User is not exist"));
		if (!passwordEncoder.matches(form.getOldPassword(), user.getPassword())) {
			throw new IllegalArgumentException("Old password is not match");
		}
		user.setPassword(passwordEncoder.encode(form.getNewPassword()));
		userRepository.save(user);
	}

	@Override
	public Boolean updateActiveStatus(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User is not exist"));

		boolean newStatus = !Boolean.TRUE.equals(user.getIsActive());
		user.setIsActive(newStatus);
		userRepository.save(user);

		return newStatus;
	}
	@Override
	public UserDTO UpdateUserInformation(Long userId, ChangePublicProfileDTO dto) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User is not exist"));

		user.setAvatarUrl(dto.getAvatarUrl());
		user.setFirstName(dto.getFirstName());
		user.setLastName(dto.getLastName());
		user.setPhoneNumber(dto.getPhoneNumber());
		user.setEmail(dto.getEmail());
		user.setUpdatedAt(LocalDateTime.now());

		User savedUser = userRepository.save(user);
		return modelMapper.map(savedUser, UserDTO.class);
	}

	@Override
	public void changePasswordAdmin(Long userId, ChangePasswordForm form) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User is not exist"));
		user.setPassword(passwordEncoder.encode(form.getNewPassword()));
		userRepository.save(user);
	}
	@Override
	public String updateUserAvatar(String username, MultipartFile file) throws IOException {
		User user = userRepository.findByUserName2(username)
				.orElseThrow(() -> new RuntimeException("User not found"));

		if (user.getAvatarUrl() != null) {
			fileService.deleteImage(user.getAvatarUrl());
		}

		String newAvatarUrl = fileService.uploadImage(file);
		user.setAvatarUrl(newAvatarUrl);
		userRepository.save(user);

		return newAvatarUrl;
	}

	@Override
	public Long countCustomerAccounts() {
		return userRepository.countByRole(Role.CUSTOMER);
	}

}
