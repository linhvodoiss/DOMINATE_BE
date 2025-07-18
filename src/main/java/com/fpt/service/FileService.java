package com.fpt.service;

import java.io.IOException;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fpt.utils.FileManager;

@Service
public class FileService implements IFileService {

	private FileManager fileManager = new FileManager();
//	private String linkFolder = "C:\\Users\\pc\\Desktop\\Avatar";
private String linkFolder = "C:\\Users\\Admin QT\\Desktop\\source fe\\SOURCE_FE_VTI\\src\\assets";
	@Override
	public String uploadImage(MultipartFile image) throws IOException {

		String nameImage = new Date().getTime() + "." + fileManager.getFormatFile(image.getOriginalFilename());

		String path = linkFolder + "\\" + nameImage;

		fileManager.createNewMultiPartFile(path, image);

		// TODO save link file to database

		// return link uploaded file
		return nameImage;
	}
}
