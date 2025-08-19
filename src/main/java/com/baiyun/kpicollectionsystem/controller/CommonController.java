package com.baiyun.kpicollectionsystem.controller;

import com.baiyun.kpicollectionsystem.common.Result;
import com.baiyun.kpicollectionsystem.util.FileStorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommonController {

	private final FileStorageService fileStorageService;

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
}



