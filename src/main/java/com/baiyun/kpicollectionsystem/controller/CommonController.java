package com.baiyun.kpicollectionsystem.controller;

import com.baiyun.kpicollectionsystem.common.Result;
import com.baiyun.kpicollectionsystem.util.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommonController {

	private final FileStorageService fileStorageService;

	@Value("${app.template.download-dir}")
	private String templateDir;

	public CommonController(FileStorageService fileStorageService) {
		this.fileStorageService = fileStorageService;
	}

	@PostMapping("/uploadFile")
	public Result<Map<String, Object>> upload(MultipartFile file) throws Exception {
		String url = fileStorageService.store(file);
		Map<String, Object> data = new HashMap<>();
		data.put("fileUrl", url);
		return Result.success(data);
	}

	@GetMapping("/download")
	public ResponseEntity<PathResource> download(@RequestParam("path") String path) throws Exception {
		// 支持前端传入如 /upload/20250101/xxx.pdf 或 20250101/xxx.pdf
		String normalized = path.startsWith("/") ? path.substring(1) : path;
		if (normalized.startsWith("upload/")) {
			normalized = normalized.substring("upload/".length());
		}
		// 物理路径 = 配置的上传根目录 + 归一化相对路径
		Path root = Paths.get(fileStorageService.getUploadRoot());
		Path target = root.resolve(normalized).normalize();
		if (!target.startsWith(root) || !Files.exists(target) || Files.isDirectory(target)) {
			return ResponseEntity.notFound().build();
		}
		String filename = target.getFileName().toString();
		String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
		PathResource resource = new PathResource(target);
		String contentType = Files.probeContentType(target);
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded)
			.contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
			.body(resource);
	}

	@GetMapping("/downloadTemplate")
	public ResponseEntity<PathResource> downloadTemplate(@RequestParam("name") String name) throws Exception {
		if (name == null || name.isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		Path root = Paths.get(templateDir);
		Files.createDirectories(root);
		Path target = root.resolve(name).normalize();
		if (!target.startsWith(root) || !Files.exists(target) || Files.isDirectory(target)) {
			return ResponseEntity.notFound().build();
		}
		String filename = target.getFileName().toString();
		String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
		PathResource resource = new PathResource(target);
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded)
			.contentType(MediaType.APPLICATION_PDF)
			.body(resource);
	}
}



