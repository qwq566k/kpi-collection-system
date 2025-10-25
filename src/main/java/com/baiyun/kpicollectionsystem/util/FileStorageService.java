package com.baiyun.kpicollectionsystem.util;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
		// 对文件名部分进行URL编码，保留扩展名
		String encodedFileName = encodeFileName(sanitizedOriginal);

		String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		Path dir = Paths.get(uploadDir, today);
		Files.createDirectories(dir);
		String name = UUID.randomUUID().toString().replaceAll("-", "") + "_" + encodedFileName;
		Path target = dir.resolve(name);
		Files.copy(file.getInputStream(), target);
		return "/upload/" + today + "/" + name;
	}
	private String encodeFileName(String fileName) {
		try {
			// 分离文件名和扩展名
			int lastDotIndex = fileName.lastIndexOf(".");
			if (lastDotIndex > 0) {
				String namePart = fileName.substring(0, lastDotIndex);
				String extension = fileName.substring(lastDotIndex);
				// 只对文件名部分进行编码，扩展名保持不变
				return URLEncoder.encode(namePart, "UTF-8") + extension;
			} else {
				return URLEncoder.encode(fileName, "UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			// 编码失败时回退到原始文件名
			return fileName;
		}
	}
}



