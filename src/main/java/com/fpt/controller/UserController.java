package com.fpt.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.fpt.annotation.CurrentUserId;
import com.fpt.dto.*;
import com.fpt.entity.PaymentOrder;
import com.fpt.form.ChangePasswordForm;
import com.fpt.payload.SuccessNoResponse;
import com.fpt.payload.SuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.entity.User;
import com.fpt.service.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping(value = "/api/v1/users")
@Validated
public class UserController {

	@Autowired
	private IUserService userService;
	@GetMapping("/checkEmail")
	public ResponseEntity<?> existsUserByEmail(@RequestParam(name = "email") String email) {
		// get entity
		boolean result = userService.existsUserByEmail(email);
		Map<String, Object> response = new HashMap<>();
		response.put("code", HttpServletResponse.SC_OK);
		response.put("check", result);
		// return result
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/checkUserName")
	public ResponseEntity<?> existsUserByUserName(@RequestParam(name = "userName") String userName) {
		// get entity
		boolean result = userService.existsUserByUserName(userName);
		Map<String, Object> response = new HashMap<>();
		response.put("code", HttpServletResponse.SC_OK);
		response.put("check", result);
		// return result
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/checkPhoneNumber")
	public ResponseEntity<?> existsUserByPhoneNumber(@RequestParam(name = "phoneNumber") String phoneNumber) {
		// get entity
		boolean result = userService.existsUserByPhoneNumber(phoneNumber);
		Map<String, Object> response = new HashMap<>();
		response.put("code", HttpServletResponse.SC_OK);
		response.put("check", result);
		// return result
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping()
	public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody UserDTO dto) {
		try {
			userService.createUser(dto.toEntity());

			Map<String, Object> response = new HashMap<>();
			response.put("code", HttpServletResponse.SC_OK);
			response.put("message", "Register successfully, please check email to active account!");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			errorResponse.put("message", "Register account failed.");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}


	@GetMapping("/activeUser")
	// validate: check exists, check not expired
	public ResponseEntity<?> activeUserViaEmail(@RequestParam String token) {
		// active user
		userService.activeUser(token);

		return new ResponseEntity<>("Active success!", HttpStatus.OK);
	}

	// resend confirm
	@GetMapping("/userRegistrationConfirmRequest")
	// validate: email exists, email not active
	public ResponseEntity<?> resendConfirmRegistrationViaEmail(@RequestParam String email) {

		userService.sendConfirmUserRegistrationViaEmail(email);

		return new ResponseEntity<>("We have sent an email. Please check email to active account!", HttpStatus.OK);
	}

	// reset password confirm
	@GetMapping("/resetPasswordRequest")
	// validate: email exists, email not active
	public ResponseEntity<?> sendResetPasswordViaEmail(@RequestParam String email) {

		userService.resetPasswordViaEmail(email);
		Map<String, Object> response = new HashMap<>();
		response.put("code", HttpServletResponse.SC_OK);
		response.put("message", "We have sent email, Please check "+email+" to change password");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// resend reset password
	@GetMapping("/resendResetPassword")
	// validate: email exists, email not active
	public ResponseEntity<?> resendResetPasswordViaEmail(@RequestParam String email) {

		userService.sendResetPasswordViaEmail(email);

		return new ResponseEntity<>("We have sent an email. Please check email to reset password!", HttpStatus.OK);
	}

	@GetMapping("/resetPassword")
	// validate: check exists, check not expired
	public ResponseEntity<?> resetPasswordViaEmail(@RequestParam String token, @RequestParam String newPassword) {
		Map<String, Object> response = new HashMap<>();
		try {
			// reset password
			userService.resetPassword(token, newPassword);

			response.put("code", HttpServletResponse.SC_OK);
			response.put("message", "Update password successfully.");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.put("code", HttpServletResponse.SC_NOT_FOUND);
			response.put("message", "Update password failed.");
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/profile")
	// validate: check exists, check not expired
	public ResponseEntity<SuccessResponse<ProfileDTO>> getUserProfile(Authentication authentication) {
		
		// get username from token
		String username = authentication.getName();
		
		// get user info
		User user = userService.findUserByUserName(username);
		
        // convert user entity to user dto
//		test git
		ProfileDTO profileDto = new ProfileDTO(
        		user.getUserName(), 
        		user.getEmail(), 
        		user.getFirstName(), 
        		user.getLastName(),
        		user.getPhoneNumber(),
        		user.getRole(),
        		user.getStatus().toString(),
        		user.getAvatarUrl());
		return ResponseEntity.ok(new SuccessResponse<>(200, "Get profile successfully!", profileDto));

	}
	@PreAuthorize("isAuthenticated()")
	@PutMapping("/profile")
	// validate: check exists, check not expired
	public ResponseEntity<?> changeUserProfile(Authentication authentication,@Valid @RequestBody ChangePublicProfileDTO dto) {
		
		// get username from token
		String username = authentication.getName();
		userService.changeUserProfile(username, dto);
		SuccessNoResponse response = new SuccessNoResponse(HttpServletResponse.SC_OK, "Thay đổi thông tin thành công");
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PreAuthorize("isAuthenticated()")
	@PatchMapping("/profile/changePassword")
	public ResponseEntity<?> changePassword(
			@CurrentUserId Long userId,
			@RequestBody ChangePasswordForm request
	) {
		try {
			userService.changePasswordUser(userId, request);
			Map<String, Object> response = new HashMap<>();
			response.put("code", HttpStatus.OK.value());
			response.put("message", "Change password successfully.");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("code", HttpStatus.BAD_REQUEST.value());
			response.put("message", "Change password failed: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

	}}
