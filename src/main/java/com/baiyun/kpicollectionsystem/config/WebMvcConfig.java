package com.baiyun.kpicollectionsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Value("${app.file.upload-dir}")
	private String uploadDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		Path root = Paths.get(uploadDir);
		String location = root.toUri().toString();
		registry.addResourceHandler("/upload/**").addResourceLocations(location);
		registry.addResourceHandler("/export/**").addResourceLocations(location);
	}
}



