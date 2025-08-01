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
		File dir = new File(UPLOAD_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		String originalFilename = file.getOriginalFilename();
		String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		String uniqueName = UUID.randomUUID().toString() + extension;

		File destination = new File(dir, uniqueName);
		file.transferTo(destination);

		return "/uploads/" + uniqueName;
	}

}
