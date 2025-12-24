package com.baiyun.kpicollectionsystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baiyun.kpicollectionsystem.common.Result;
import com.baiyun.kpicollectionsystem.entity.ResearchAchievement;
import com.baiyun.kpicollectionsystem.service.ResearchAchievementService;
import com.baiyun.kpicollectionsystem.util.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ResearchAchievementController {

	private final ResearchAchievementService service;
	private final JwtUtil jwtUtil;

	public ResearchAchievementController(ResearchAchievementService service, JwtUtil jwtUtil) {
		this.service = service;
		this.jwtUtil = jwtUtil;
	}

	@PostMapping("/submitRecord")
	public Result<Void> submitRecord(@RequestBody @Valid ResearchAchievement req) {
		service.submit(req, false);
		return Result.success();
	}

	@PostMapping("/saveDraft")
	public Result<Void> saveDraft(@RequestBody @Valid ResearchAchievement req) {
		if (req.getFieldId() == null || req.getIndicatorId() == null || req.getStandardId() == null){
			return Result.failure("考核领域，关键指标，评价标准为必填项");
		}
		service.submit(req, true);
		return Result.success();
	}

	@PutMapping("/updateRecord")
	public Result<Void> update(@RequestBody @Valid ResearchAchievement req) {
		service.updateRecord(req);
		return Result.success();
	}

	@GetMapping("/getRecordDetail")
	public Result<ResearchAchievement> detail(@RequestParam @NotNull Integer id) {
		return Result.success(service.findById(id));
	}

	@GetMapping("/getAllRecords")
	public Result<Map<String, Object>> list(@RequestParam int page, @RequestParam int size, @RequestHeader(value = "token", required = false) String token) {
		Integer submitterId = null;
		try {
			if (token != null) {
				var claims = jwtUtil.parseToken(token);
				Object idObj = claims.get("id");
				if (idObj instanceof Number n) {
					submitterId = n.intValue();
				}
			}
		} catch (Exception ignored) {}
		if (submitterId == null) {
			return Result.failure("未登录");
		}
		IPage<ResearchAchievement> p = service.queryBySubmitter(submitterId, page, size);
		Map<String, Object> data = new HashMap<>();
		data.put("total", p.getTotal());
		data.put("rows", p.getRecords());
		return Result.success(data);
	}

	@DeleteMapping("/deleteRecord")
	public Result<Void> deleteRecord(@RequestParam @NotNull Integer id) {
		service.deleteRecord(id);
		return Result.success();
	}

}


