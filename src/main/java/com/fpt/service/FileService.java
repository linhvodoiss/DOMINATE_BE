package com.fpt.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fpt.utils.FileManager;

@Service
public class FileService implements IFileService {

    private FileManager fileManager = new FileManager();
    //	private String linkFolder = "C:\\Users\\pc\\Desktop\\Avatar";
//    private String linkFolder = "C:\\Users\\Admin QT\\Desktop\\source fe\\SOURCE_FE_VTI\\src\\assets";

	private final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";

	@Override
	public String uploadImage(MultipartFile file) throws IOException {
//		2 MB
		final long MAX_SIZE = 2 * 1024 * 1024;
		// type file is accepted
		final String[] allowedExtensions = { ".jpg", ".jpeg", ".png", ".gif", ".webp" };

		if (file.isEmpty()) {
			throw new IllegalArgumentException("File is empty");
		}

		if (file.getSize() > MAX_SIZE) {
			throw new IllegalArgumentException("File size exceeds the maximum limit (2MB)");
		}

		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null || !originalFilename.contains(".")) {
			throw new IllegalArgumentException("Invalid file name or format");
		}

		String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

		boolean isValidExtension = false;
		for (String allowed : allowedExtensions) {
			if (allowed.equals(extension)) {
				isValidExtension = true;
				break;
			}
		}

		if (!isValidExtension) {
			throw new IllegalArgumentException("Unsupported file type. Allowed: jpg, jpeg, png, gif, webp");
		}

		// Create if no exist
		File dir = new File(UPLOAD_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		// file unique
		String uniqueName = UUID.randomUUID().toString() + extension;

		File destination = new File(dir, uniqueName);
		file.transferTo(destination);

		return "/uploads/" + uniqueName;
	}

	@Override
	public void deleteImage(String fileName) {
		if (fileName == null || fileName.isEmpty()) return;

		if (fileName.startsWith("/uploads/")) {
			fileName = fileName.replace("/uploads/", "");
		}

		File file = new File(UPLOAD_DIR, fileName);
		if (file.exists()) {
			file.delete();
		}
	}
}
