package com.baiyun.kpicollectionsystem.controller;

import com.baiyun.kpicollectionsystem.entity.AssessmentField;
import com.baiyun.kpicollectionsystem.entity.ScoreStandard;
import com.baiyun.kpicollectionsystem.mapper.AssessmentFieldMapper;
import com.baiyun.kpicollectionsystem.mapper.ScoreStandardMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baiyun.kpicollectionsystem.common.Result;
import com.baiyun.kpicollectionsystem.entity.ResearchAchievement;
import com.baiyun.kpicollectionsystem.mapper.ResearchAchievementMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AdminController {

	private final ResearchAchievementMapper mapper;
	private final ScoreStandardMapper standardMapper;
	private final AssessmentFieldMapper fieldMapper;

	public AdminController(ResearchAchievementMapper mapper, ScoreStandardMapper standardMapper, AssessmentFieldMapper fieldMapper) {
		this.mapper = mapper;
        this.standardMapper = standardMapper;
        this.fieldMapper = fieldMapper;
    }

	@PreAuthorize("hasRole('admin')")
	@PostMapping("/admin/queryRecords")
	public Result<Map<String, Object>> query(@RequestBody QueryReq req) {
		Page<ResearchAchievement> page = new Page<>(req.getPage(), req.getSize());
		LambdaQueryWrapper<ResearchAchievement> qw = new LambdaQueryWrapper<>();
		if (req.getFieldId() != null) qw.eq(ResearchAchievement::getFieldId, req.getFieldId());
		if (req.getSubmitterName() != null && !req.getSubmitterName().isEmpty()) qw.like(ResearchAchievement::getSubmitterName, req.getSubmitterName());
		if (req.getStatus() != null) qw.eq(ResearchAchievement::getStatus, req.getStatus());
		qw.orderByDesc(ResearchAchievement::getCreatedAt);
		IPage<ResearchAchievement> p = mapper.selectPage(page, qw);
		Map<String, Object> data = new HashMap<>();
		data.put("total", p.getTotal());
		data.put("rows", p.getRecords());
		return Result.success(data);
	}

	@PreAuthorize("hasRole('admin')")
	@PutMapping("/approveRecord")
	@Transactional(rollbackFor = Exception.class)
	public Result<Void> approve(@RequestParam Integer id) {
		// 1. 使用CAS方式获取操作权：从状态1改为-1
		boolean lockAcquired = acquireOperationLock(id);
		if (!lockAcquired) {
			return Result.failure("记录不存在或正在被其他审核员操作");
		}

		try {
			// 2. 查询记录信息
			ResearchAchievement ra = mapper.selectById(id);
			if (ra == null) {
				throw new RuntimeException("记录不存在");
			}

			// 3. 获取当前登录用户信息
			String userAuthentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
			String[] userNameAndId = userAuthentication.split("#");
			Integer reviewerId = Integer.valueOf(userNameAndId[1]);
			String reviewerName = userNameAndId[0];

			// 4. 获取评分标准
			ScoreStandard standard = standardMapper.selectById(ra.getStandardId());

			// 5. 更新领域分值
			boolean fieldUpdated = incrementFieldScore(ra.getFieldId(), standard.getScore());
			if (!fieldUpdated) {
				throw new RuntimeException("更新领域分值失败");
			}

//			// 6. 获取其他必要信息
//			String scoringInstruction = standardMapper.selectInstructionById(ra.getStandardId());
//			String assessmentOrg = standardMapper.selectOrgById(ra.getStandardId());

			// 7. 完成审核操作：从状态-1改为2
			boolean operationCompleted = completeApproveOperation(
					id, standard.getScore(),
					reviewerId, reviewerName, LocalDateTime.now()
			);

			if (!operationCompleted) {
				throw new RuntimeException("审核操作失败");
			}

			return Result.success();

		} catch (Exception e) {
			// 发生异常时释放操作锁
			releaseOperationLock(id);
			return Result.failure("审核失败: " + e.getMessage());
		}
	}

	@PreAuthorize("hasRole('admin')")
	@PutMapping("/rejectRecord")
	@Transactional(rollbackFor = Exception.class)
	public Result<Void> reject(@RequestBody RejectReq req) {
		// 验证参数
		if (req.getRejectReason() == null || req.getRejectReason().isBlank()) {
			return Result.failure("退回原因必填");
		}

		// 1. 使用CAS方式获取操作权
		boolean lockAcquired = acquireOperationLock(req.getId());
		if (!lockAcquired) {
			return Result.failure("记录不存在或正在被其他审核员操作");
		}

		try {
			// 2. 获取当前登录用户信息
			String userAuthentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
			String[] userNameAndId = userAuthentication.split("#");
			Integer reviewerId = Integer.valueOf(userNameAndId[1]);
			String reviewerName = userNameAndId[0];

			// 3. 完成拒绝操作：从状态-1改为3
			boolean operationCompleted = completeRejectOperation(
					req.getId(), req.getRejectReason(),
					reviewerId, reviewerName, LocalDateTime.now()
			);

			if (!operationCompleted) {
				throw new RuntimeException("拒绝操作失败");
			}

			return Result.success();

		} catch (Exception e) {
			// 发生异常时释放操作锁
			releaseOperationLock(req.getId());
			return Result.failure("拒绝操作失败: " + e.getMessage());
		}
	}

	@PreAuthorize("hasRole('admin')")
	@GetMapping("/getSubmissionStats")
	public Result<Map<String, Object>> submissionStats() {
		long pending = mapper.selectCount(new LambdaQueryWrapper<ResearchAchievement>().eq(ResearchAchievement::getStatus, 1));
		long approved = mapper.selectCount(new LambdaQueryWrapper<ResearchAchievement>().eq(ResearchAchievement::getStatus, 2));
		long rejected = mapper.selectCount(new LambdaQueryWrapper<ResearchAchievement>().eq(ResearchAchievement::getStatus, 3));
		long total = pending + approved + rejected;
		Integer totalScore = mapper.selectList(new LambdaQueryWrapper<ResearchAchievement>().eq(ResearchAchievement::getStatus, 2))
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

	/**
	 * 获取操作锁：从状态1改为-1
	 */
	private boolean acquireOperationLock(Integer id) {
		UpdateWrapper<ResearchAchievement> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("id", id)
				.eq("status", 1)  // 只有状态为1时才能获取锁
				.set("status", -1)
				.set("updated_at", new Date());

		return mapper.update(null, updateWrapper) > 0;
	}

	/**
	 * 完成审核操作：从状态-1改为2
	 */
	private boolean completeApproveOperation(Integer id, Integer score,

											 Integer reviewerId, String reviewerName,
											 LocalDateTime reviewedAt) {
		UpdateWrapper<ResearchAchievement> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("id", id)
				.eq("status", -1)  // 只有状态为-1时才能完成操作
				.set("status", 2)
				.set("score", score)

				.set("reject_reason", null)
				.set("reviewer_id", reviewerId)
				.set("reviewer_name", reviewerName)
				.set("reviewed_at", reviewedAt)
				.set("updated_at", new Date());

		return mapper.update(null, updateWrapper) > 0;
	}

	/**
	 * 完成拒绝操作：从状态-1改为3
	 */
	private boolean completeRejectOperation(Integer id, String rejectReason,
											Integer reviewerId, String reviewerName,
											LocalDateTime reviewedAt) {
		UpdateWrapper<ResearchAchievement> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("id", id)
				.eq("status", -1)  // 只有状态为-1时才能完成操作
				.set("status", 3)
				.set("reject_reason", rejectReason)
				.set("reviewer_id", reviewerId)
				.set("reviewer_name", reviewerName)
				.set("reviewed_at", reviewedAt)
				.set("updated_at", new Date());

		return mapper.update(null, updateWrapper) > 0;
	}

	/**
	 * 释放操作锁：从状态-1改回1
	 */
	private boolean releaseOperationLock(Integer id) {
		UpdateWrapper<ResearchAchievement> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("id", id)
				.eq("status", -1)  // 只有状态为-1时才能释放锁
				.set("status", 1)
				.set("updated_at", new Date());

		return mapper.update(null, updateWrapper) > 0;
	}

	/**
	 * 原子增加领域分值
	 */
	private boolean incrementFieldScore(Integer fieldId, Integer increment) {
		// 假设 AssessmentField 也有对应的 mapper
		UpdateWrapper<AssessmentField> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("id", fieldId)
				.setSql("score = score + " + increment)
				.set("updated_at", new Date());

		return fieldMapper.update(null, updateWrapper) > 0;
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


