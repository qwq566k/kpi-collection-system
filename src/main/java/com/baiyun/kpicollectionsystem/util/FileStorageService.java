package com.baiyun.kpicollectionsystem.util;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileStorageService {

	@Value("${app.file.upload-dir}")
	private String uploadDir;

	public String getUploadRoot() {
		return uploadDir;
	}

	public String store(MultipartFile file) throws IOException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("上传文件为空");
		}
		String originalName = StringUtils.hasText(file.getOriginalFilename()) ?
				FilenameUtils.getName(file.getOriginalFilename()) : "file";
		// 清理原始文件名中的非法字符（兼容 Windows 非法字符与空白符）
		String sanitizedOriginal = originalName.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
		String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		Path dir = Paths.get(uploadDir, today);
		Files.createDirectories(dir);
		String name = UUID.randomUUID().toString().replaceAll("-", "") + "_" + sanitizedOriginal;
		Path target = dir.resolve(name);
		Files.copy(file.getInputStream(), target);
		return "/upload/" + today + "/" + name;
	}
}



