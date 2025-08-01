package com.fpt.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import com.fpt.entity.User;
import com.fpt.payload.SuccessResponse;
import com.fpt.repository.UserRepository;
import com.fpt.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fpt.service.IFileService;
import com.fpt.utils.FileManager;

@CrossOrigin("*")
@RestController
@RequestMapping(value = "/api/v1/files")
@Validated
public class FileController {

	@Autowired
	private IFileService fileService;
	@Autowired
	private IUserService userService;
	@Autowired
	private UserRepository userRepository;
	@PostMapping
	public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile image) throws IOException {
		if (!new FileManager().isTypeFileImage(image)) {
			return ResponseEntity.unprocessableEntity().body("File must be image!");
		}

		String imageUrl = fileService.uploadImage(image);
		return ResponseEntity.ok(imageUrl);
	}
	@PostMapping("/avatar")
	public ResponseEntity<SuccessResponse<Map<String, String>>> updateAvatar(
			@RequestParam("image") MultipartFile file,
			Principal principal
	) throws IOException {
		String avatarFullUrl = userService.updateUserAvatar(principal.getName(), file);

		Map<String, String> data = new HashMap<>();
		data.put("avatarUrl", avatarFullUrl);

		return ResponseEntity.ok(new SuccessResponse<>(200, "Update avatar successfully", data));
	}


}
