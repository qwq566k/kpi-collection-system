package com.baiyun.kpicollectionsystem.controller;

import com.baiyun.kpicollectionsystem.mapper.AssessmentRelationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baiyun.kpicollectionsystem.common.Result;
import com.baiyun.kpicollectionsystem.entity.AssessmentField;
import com.baiyun.kpicollectionsystem.entity.KeyIndicator;
import com.baiyun.kpicollectionsystem.entity.ScoreStandard;
import com.baiyun.kpicollectionsystem.mapper.AssessmentFieldMapper;
import com.baiyun.kpicollectionsystem.mapper.KeyIndicatorMapper;
import com.baiyun.kpicollectionsystem.mapper.ScoreStandardMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DictController {

	private final AssessmentFieldMapper fieldMapper;
	private final AssessmentRelationMapper relationMapper;
//	private final KeyIndicatorMapper indicatorMapper;
//	private final ScoreStandardMapper standardMapper;

	public DictController(AssessmentFieldMapper fieldMapper, KeyIndicatorMapper indicatorMapper, AssessmentRelationMapper relationMapper, ScoreStandardMapper standardMapper) {
		this.fieldMapper = fieldMapper;
        this.relationMapper = relationMapper;
//        this.indicatorMapper = indicatorMapper;
//		this.standardMapper = standardMapper;
	}

	@GetMapping("/getAllField")
	public Result<List<Map<String, Object>>> fields() {
		List<AssessmentField> list = fieldMapper.selectList(new LambdaQueryWrapper<AssessmentField>().eq(AssessmentField::getStatus, 1));
		List<Map<String, Object>> res = new ArrayList<>();
		for (AssessmentField f : list) {
			Map<String, Object> m = new HashMap<>();
			m.put("areaId", f.getId());
			m.put("areaName", f.getName());
			res.add(m);
		}
		return Result.success(res);
	}

	@GetMapping("/getKeyIndicators")
	public Result<List<Map<String, Object>>> indicators(@RequestParam("fieldId") Integer fieldId) {
		List<KeyIndicator> list = relationMapper.selectIndicatorsListByFieldId(fieldId);
		List<Map<String, Object>> res = new ArrayList<>();
		for (KeyIndicator i : list) {
			Map<String, Object> m = new HashMap<>();
			m.put("indicatorId", i.getId());
			m.put("indicatorName", i.getName());
			res.add(m);
		}
		return Result.success(res);
	}

	@GetMapping("/getStandards")
	public Result<List<Map<String, Object>>> standards(@RequestParam Integer indicatorId) {
		List<ScoreStandard> list = relationMapper.selectStandardsListByIndicatorId(indicatorId);
		List<Map<String, Object>> res = new ArrayList<>();
		for (ScoreStandard s : list) {
			Map<String, Object> m = new HashMap<>();
			m.put("standardId", s.getId());
			m.put("standardName", s.getName());
			m.put("score", s.getScore());
			res.add(m);
		}
		return Result.success(res);
	}
}


