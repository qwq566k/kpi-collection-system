package com.baiyun.kpicollectionsystem.controller;

import com.baiyun.kpicollectionsystem.mapper.ScoreStandardMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baiyun.kpicollectionsystem.common.Result;
import com.baiyun.kpicollectionsystem.entity.ResearchAchievement;
import com.baiyun.kpicollectionsystem.mapper.ResearchAchievementMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
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
	@Value("${app.template.download-dir}")
	private String excelTemplateDir;

	public ExportController(ResearchAchievementMapper mapper, ScoreStandardMapper standardMapper) {
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
	public void exportExcel(@RequestBody Map<String, Object> req, HttpServletResponse response) throws Exception {
		int exportType = (int) req.getOrDefault("exportType", 1);
		List<ResearchAchievement> list;
		if (exportType == 1) {
			list = mapper.selectList(null);
		} else {
			LambdaQueryWrapper<ResearchAchievement> qw = buildWrapperFromReq(req);
			list = mapper.selectList(qw);
		}

		// 检查模板文件是否存在
		String templatePath = excelTemplateDir + "/成果导出模板.xlsx";
		File templateFile = new File(templatePath);
		Workbook wb;

		if (templateFile.exists()) {
			// 使用模板导出
			try (InputStream is = new FileInputStream(templateFile)) {
				wb = new XSSFWorkbook(is);
				Sheet sheet = wb.getSheetAt(0); // 获取第一个工作表

				// 从第二行开始填充数据（假设第一行是表头）
				int startRow = 1;
				int r = startRow;

				for (ResearchAchievement ra : list) {
					if (ra.getStatus() != 2) continue;

					Row row = sheet.createRow(r++);
					row.createCell(0).setCellValue(r - startRow); // 序号
					row.createCell(1).setCellValue(ra.getDepartment());
					row.createCell(2).setCellValue(ra.getAchievementName());
					row.createCell(3).setCellValue(ra.getSubmitterName());
					row.createCell(4).setCellValue(ra.getTeamMembers() == null ? "" : ra.getTeamMembers().toString());
					row.createCell(5).setCellValue(ra.getObtainDate().toString());
					row.createCell(6).setCellValue(ra.getFieldName());
					row.createCell(7).setCellValue(ra.getIndicatorName());
					row.createCell(8).setCellValue(ra.getStandardName());
					row.createCell(9).setCellValue(ra.getScore() == null ? 0 : ra.getScore());
					row.createCell(10).setCellValue(ra.getScoringInstruction());
					row.createCell(11).setCellValue(ra.getAssessmentOrg());
				}
			}
		} else {
			// 使用默认导出（原有逻辑）
			wb = new XSSFWorkbook();
			Sheet sheet = wb.createSheet("成果");

			// 创建表头
			int r = 0;
			Row head = sheet.createRow(r++);
			String[] headList = {"序号", "部门", "成果名称", "团队负责人", "团队成员", "获得日期", "考核领域", "关键成效指标", "评价标准", "分值", "赋分说明", "考核组织部门"};
			for (int i = 0; i < headList.length; i++) {
				head.createCell(i).setCellValue(headList[i]);
			}

			// 填充数据
			for (ResearchAchievement ra : list) {
				if (ra.getStatus() != 2) continue;
				Row row = sheet.createRow(r++);
				row.createCell(0).setCellValue(r - 1);
				row.createCell(1).setCellValue(ra.getDepartment());
				row.createCell(2).setCellValue(ra.getAchievementName());
				row.createCell(3).setCellValue(ra.getSubmitterName());
				row.createCell(4).setCellValue(ra.getTeamMembers() == null ? "" : ra.getTeamMembers().toString());
				row.createCell(5).setCellValue(ra.getObtainDate().toString());
				row.createCell(6).setCellValue(ra.getFieldName());
				row.createCell(7).setCellValue(ra.getIndicatorName());
				row.createCell(8).setCellValue(ra.getStandardName());
				row.createCell(9).setCellValue(ra.getScore() == null ? 0 : ra.getScore());
				row.createCell(10).setCellValue(ra.getScoringInstruction());
				row.createCell(11).setCellValue(ra.getAssessmentOrg());
			}
		}

		// 设置响应头，直接返回文件流
		String filename = "KPI成果导出_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(filename, "UTF-8") + "\"");

		// 直接写入响应输出流
		try (ServletOutputStream out = response.getOutputStream()) {
			wb.write(out);
		} finally {
			wb.close();
		}
	}

	@PostMapping("/exportFieldExcel")
	@PreAuthorize("hasRole('admin')")
	public void exportFieldExcel(HttpServletResponse response) throws Exception {

		List<Map<String, Object>> list = mapper.selectAreaStats();
		// 检查模板文件是否存在
		String templatePath = excelTemplateDir + "/考核领域数据导出模板.xlsx";
		File templateFile = new File(templatePath);
		Workbook wb;

		if (templateFile.exists()) {
			// 使用模板导出
			try (InputStream is = new FileInputStream(templateFile)) {
				wb = new XSSFWorkbook(is);
				Sheet sheet = wb.getSheetAt(0); // 获取第一个工作表

				// 从第二行开始填充数据（假设第一行是表头）
				int startRow = 1;
				int r = startRow;

				for (Map<String, Object> ra : list) {
					Row row = sheet.createRow(r++);
					row.createCell(0).setCellValue(r - startRow); // 序号
					row.createCell(1).setCellValue(ra.get("fieldName").toString());
					row.createCell(2).setCellValue(ra.get("score").toString());
				}
			}
		} else {
			// 使用默认导出（原有逻辑）
			wb = new XSSFWorkbook();
			Sheet sheet = wb.createSheet("成果");

			// 创建表头
			int r = 0;
			Row head = sheet.createRow(r++);
			String[] headList = {"序号","考核领域","分值"};
			for (int i = 0; i < headList.length; i++) {
				head.createCell(i).setCellValue(headList[i]);
			}

			// 填充数据
			for (Map<String, Object> ra : list) {
				Row row = sheet.createRow(r++);
				row.createCell(0).setCellValue(r - 1);
				row.createCell(1).setCellValue(ra.get("fieldName").toString());
				row.createCell(2).setCellValue(ra.get("score").toString());
			}
		}

		// 设置响应头，直接返回文件流
		String filename = "考核领域数据导出_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(filename, "UTF-8") + "\"");

		// 直接写入响应输出流
		try (ServletOutputStream out = response.getOutputStream()) {
			wb.write(out);
		} finally {
			wb.close();
		}
	}

	@PostMapping("/exportSubmitterExcel")
	@PreAuthorize("hasRole('admin')")
	public void exportSubmitterExcel(HttpServletResponse response) throws Exception {

		List<Map<String, Object>> list = mapper.selectSubmitterStats();
		// 检查模板文件是否存在
		String templatePath = excelTemplateDir + "/负责人成果数据导出模板.xlsx";
		File templateFile = new File(templatePath);
		Workbook wb;

		if (templateFile.exists()) {
			// 使用模板导出
			try (InputStream is = new FileInputStream(templateFile)) {
				wb = new XSSFWorkbook(is);
				Sheet sheet = wb.getSheetAt(0); // 获取第一个工作表

				// 从第二行开始填充数据（假设第一行是表头）
				int startRow = 1;
				int r = startRow;

				for (Map<String, Object> ra : list) {
					Row row = sheet.createRow(r++);
					row.createCell(0).setCellValue(r - startRow); // 序号
					row.createCell(1).setCellValue(ra.get("submitterName").toString());
					row.createCell(2).setCellValue(ra.get("score").toString());
				}
			}
		} else {
			// 使用默认导出（原有逻辑）
			wb = new XSSFWorkbook();
			Sheet sheet = wb.createSheet("成果");

			// 创建表头
			int r = 0;
			Row head = sheet.createRow(r++);
			String[] headList = {"序号","负责人名称","分值"};
			for (int i = 0; i < headList.length; i++) {
				head.createCell(i).setCellValue(headList[i]);
			}

			// 填充数据
			for (Map<String, Object> ra : list) {
				Row row = sheet.createRow(r++);
				row.createCell(0).setCellValue(r - 1);
				row.createCell(1).setCellValue(ra.get("submitterName").toString());
				row.createCell(2).setCellValue(ra.get("score").toString());
			}
		}

		// 设置响应头，直接返回文件流
		String filename = "负责人成果数据导出_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(filename, "UTF-8") + "\"");

		// 直接写入响应输出流
		try (ServletOutputStream out = response.getOutputStream()) {
			wb.write(out);
		} finally {
			wb.close();
		}
	}

	private LambdaQueryWrapper<ResearchAchievement> buildWrapperFromReq(Map<String, Object> req) {
		LambdaQueryWrapper<ResearchAchievement> qw = new LambdaQueryWrapper<>();
		Object fieldId = req.get("fieldId");
		Object submitterName = req.get("submitterName");
		Object startDate = req.get("startDate");
		Object endDate = req.get("endDate");
		if (fieldId instanceof Integer id) qw.eq(ResearchAchievement::getFieldId, id);
		if (submitterName instanceof String s && !s.isBlank()) qw.like(ResearchAchievement::getSubmitterName, s);
		if (startDate instanceof String start && !start.isBlank()) qw.ge(ResearchAchievement::getObtainDate, startDate);
		if (endDate instanceof String end && !end.isBlank()) qw.le(ResearchAchievement::getObtainDate, endDate);
		qw.ne(ResearchAchievement::getStatus, 4);
		return qw;
	}

	// 状态转换方法
	private String convertStatusToText(Integer status) {
		if (status == null) {
			return "未知";
		}

		switch (status) {
			case 0: return "草稿";
			case 1: return "待审核";
			case 2: return "已通过";
			case 3: return "已退回";
			default: return "未知状态(" + status + ")";
		}
	}
}


