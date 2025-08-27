package com.baiyun.kpicollectionsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baiyun.kpicollectionsystem.common.Result;
import com.baiyun.kpicollectionsystem.entity.ResearchAchievement;
import com.baiyun.kpicollectionsystem.mapper.ResearchAchievementMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AdminController {

	private final ResearchAchievementMapper mapper;

	public AdminController(ResearchAchievementMapper mapper) {
		this.mapper = mapper;
	}

	@PreAuthorize("hasRole('admin')")
	@PostMapping("/admin/queryRecords")
	public Result<Map<String, Object>> query(@RequestBody QueryReq req) {
		Page<ResearchAchievement> page = new Page<>(req.getPage(), req.getSize());
		LambdaQueryWrapper<ResearchAchievement> qw = new LambdaQueryWrapper<>();
		if (req.getFieldId() != null) qw.eq(ResearchAchievement::getFieldId, req.getFieldId());
		if (req.getSubmitterName() != null && !req.getSubmitterName().isEmpty()) qw.like(ResearchAchievement::getSubmitterName, req.getSubmitterName());
		if (req.getStatus() != null) qw.eq(ResearchAchievement::getStatus, mapStatus(req.getStatus()));
		qw.orderByDesc(ResearchAchievement::getCreatedAt);
		IPage<ResearchAchievement> p = mapper.selectPage(page, qw);
		Map<String, Object> data = new HashMap<>();
		data.put("total", p.getTotal());
		data.put("rows", p.getRecords());
		return Result.success(data);
	}

	@PreAuthorize("hasRole('admin')")
	@PutMapping("/approveRecord")
	public Result<Void> approve(@RequestParam Integer id) {
		ResearchAchievement ra = mapper.selectById(id);
		if (ra == null) return Result.failure("记录不存在");
		ra.setStatus(2);
		ra.setRejectReason(null);
		ra.setReviewedAt(LocalDateTime.now());
		mapper.updateById(ra);
		return Result.success();
	}

	@PreAuthorize("hasRole('admin')")
	@PutMapping("/rejectRecord")
	public Result<Void> reject(@RequestBody RejectReq req) {
		ResearchAchievement ra = mapper.selectById(req.getId());
		if (ra == null) return Result.failure("记录不存在");
		if (req.getRejectReason() == null || req.getRejectReason().isBlank()) return Result.failure("退回原因必填");
		ra.setStatus(3);
		ra.setRejectReason(req.getRejectReason());
		ra.setReviewedAt(LocalDateTime.now());
		mapper.updateById(ra);
		return Result.success();
	}

	@PreAuthorize("hasRole('admin')")
	@GetMapping("/getSubmissionStats")
	public Result<Map<String, Object>> submissionStats() {
		long total = mapper.selectCount(null);
		long approved = mapper.selectCount(new LambdaQueryWrapper<ResearchAchievement>().eq(ResearchAchievement::getStatus, "approved"));
		long pending = mapper.selectCount(new LambdaQueryWrapper<ResearchAchievement>().eq(ResearchAchievement::getStatus, "pending"));
		long rejected = mapper.selectCount(new LambdaQueryWrapper<ResearchAchievement>().eq(ResearchAchievement::getStatus, "rejected"));
		Integer totalScore = mapper.selectList(new LambdaQueryWrapper<ResearchAchievement>().eq(ResearchAchievement::getStatus, "approved"))
				.stream().map(ResearchAchievement::getScore).filter(s -> s != null).mapToInt(Integer::intValue).sum();
		Map<String, Object> data = new HashMap<>();
		data.put("total", total);
		data.put("approved", approved);
		data.put("pending", pending);
		data.put("rejected", rejected);
		data.put("totalScore", totalScore);
		return Result.success(data);
	}

	@PreAuthorize("hasRole('admin')")
	@GetMapping("/getAreaStats")
	public Result<List<Map<String, Object>>> areaStats() {
		return Result.success(mapper.selectAreaStats());
	}

	@PreAuthorize("hasRole('admin')")
	@GetMapping("/getSubmitterStats")
	public Result<List<Map<String, Object>>> submitterStats() {
		return Result.success(mapper.selectSubmitterStats());
	}

	private String mapStatus(Integer s) {
		return switch (s) {
			case 0 -> "draft";
			case 1 -> "pending";
			case 2 -> "approved";
			case 3 -> "rejected";
			default -> null;
		};
	}

	@Data
	public static class QueryReq {
		private Integer fieldId;
		private String submitterName;
		private Integer status;
		private Integer page = 1;
		private Integer size = 10;
	}

	@Data
	public static class RejectReq {
		@NotNull
		private Integer id;
		private String rejectReason;
	}
}


