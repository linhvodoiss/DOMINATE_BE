package com.fpt.controller;

import java.io.IOException;

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

	@PostMapping
	public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile image) throws IOException {
		if (!new FileManager().isTypeFileImage(image)) {
			return ResponseEntity.unprocessableEntity().body("File must be image!");
		}

		String imageUrl = fileService.uploadImage(image);
		return ResponseEntity.ok(imageUrl); // Trả lại đường dẫn URL để frontend dùng
	}

}
