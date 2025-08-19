package com.baiyun.kpicollectionsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baiyun.kpicollectionsystem.common.Result;
import com.baiyun.kpicollectionsystem.entity.ResearchAchievement;
import com.baiyun.kpicollectionsystem.mapper.ResearchAchievementMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ExportController {

	private final ResearchAchievementMapper mapper;

	@Value("${app.file.upload-dir}")
	private String uploadDir;

	public ExportController(ResearchAchievementMapper mapper) {
		this.mapper = mapper;
	}

	@PostMapping("/exportQuery")
	@PreAuthorize("hasRole('admin')")
	public Result<Map<String, Object>> exportQuery(@RequestBody Map<String, Object> req) {
		// 复用管理员条件查询，直接返回查询结果（简单实现）
		LambdaQueryWrapper<ResearchAchievement> qw = buildWrapperFromReq(req);
		List<ResearchAchievement> list = mapper.selectList(qw);
		Map<String, Object> data = new HashMap<>();
		data.put("total", list.size());
		data.put("rows", list);
		return Result.success(data);
	}

	@PostMapping("/exportExcel")
	@PreAuthorize("hasRole('admin')")
	public Result<Map<String, Object>> exportExcel(@RequestBody Map<String, Object> req) throws Exception {
		int exportType = (int) req.getOrDefault("exportType", 1);
		List<ResearchAchievement> list;
		if (exportType == 1) {
			list = mapper.selectList(null);
		} else {
			LambdaQueryWrapper<ResearchAchievement> qw = buildWrapperFromReq(req);
			list = mapper.selectList(qw);
		}
		XSSFWorkbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("成果");
		int r = 0;
		Row head = sheet.createRow(r++);
		head.createCell(0).setCellValue("ID");
		head.createCell(1).setCellValue("年度");
		head.createCell(2).setCellValue("部门");
		head.createCell(3).setCellValue("提交人");
		head.createCell(4).setCellValue("成果名称");
		head.createCell(5).setCellValue("领域");
		head.createCell(6).setCellValue("指标");
		head.createCell(7).setCellValue("标准");
		head.createCell(8).setCellValue("得分");
		head.createCell(9).setCellValue("状态");
		for (ResearchAchievement ra : list) {
			Row row = sheet.createRow(r++);
			row.createCell(0).setCellValue(ra.getId());
			row.createCell(1).setCellValue(ra.getYear());
			row.createCell(2).setCellValue(ra.getDepartment());
			row.createCell(3).setCellValue(ra.getSubmitterName());
			row.createCell(4).setCellValue(ra.getAchievementName());
			row.createCell(5).setCellValue(ra.getFieldName());
			row.createCell(6).setCellValue(ra.getIndicatorName());
			row.createCell(7).setCellValue(ra.getStandardName());
			row.createCell(8).setCellValue(ra.getScore() == null ? 0 : ra.getScore());
			row.createCell(9).setCellValue(ra.getStatus());
		}
		String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		Path dir = Paths.get(uploadDir, today);
		Files.createDirectories(dir);
		String filename = "export_" + System.currentTimeMillis() + ".xlsx";
		Path target = dir.resolve(filename);
		try (FileOutputStream fos = new FileOutputStream(target.toFile())) {
			wb.write(fos);
		}
		wb.close();
		Map<String, Object> data = new HashMap<>();
		data.put("fileUrl", "/export/" + today + "/" + filename);
		return Result.success(data);
	}

	private LambdaQueryWrapper<ResearchAchievement> buildWrapperFromReq(Map<String, Object> req) {
		LambdaQueryWrapper<ResearchAchievement> qw = new LambdaQueryWrapper<>();
		Object fieldName = req.get("fieldName");
		Object submitterName = req.get("submitterName");
		Object startDate = req.get("startDate");
		Object endDate = req.get("endDate");
		if (fieldName instanceof String s && !s.isBlank()) qw.like(ResearchAchievement::getFieldName, s);
		if (submitterName instanceof String s && !s.isBlank()) qw.like(ResearchAchievement::getSubmitterName, s);
		// 日期条件简化，假设按创建时间字符串处理
		return qw;
	}
}


