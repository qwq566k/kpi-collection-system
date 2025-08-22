package com.baiyun.kpicollectionsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baiyun.kpicollectionsystem.common.Result;
import com.baiyun.kpicollectionsystem.entity.Users;
import com.baiyun.kpicollectionsystem.mapper.UsersMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserManageController {

	private final UsersMapper usersMapper;
	private final PasswordEncoder passwordEncoder;

	public UserManageController(UsersMapper usersMapper, PasswordEncoder passwordEncoder) {
		this.usersMapper = usersMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@PostMapping("/queryUsers")
	public Result<Map<String, Object>> query(@RequestBody QueryReq req) {
		Page<Users> page = new Page<>(req.getPage(), req.getSize());
		LambdaQueryWrapper<Users> qw = new LambdaQueryWrapper<>();
		if (req.getName() != null && !req.getName().isBlank()) qw.like(Users::getName, req.getName());
		if (req.getEmployeeId() != null && !req.getEmployeeId().isBlank()) qw.eq(Users::getEmployeeId, req.getEmployeeId());
		if (req.getDepartment() != null && !req.getDepartment().isBlank()) qw.like(Users::getDepartment, req.getDepartment());
		IPage<Users> p = usersMapper.selectPage(page, qw);
		Map<String, Object> data = new HashMap<>();
		data.put("total", p.getTotal());
		data.put("rows", p.getRecords());
		return Result.success(data);
	}

	@PostMapping("/addUser")
	public Result<Void> addUser(@RequestBody @Valid AddUserReq req) {
		Users u = new Users();
		u.setEmployeeId(req.getEmployeeId());
		u.setName(req.getName());
		u.setPhone(req.getPhone());
		u.setDepartment(req.getDepartment());
		u.setUsername(req.getEmployeeId());
		u.setPassword(passwordEncoder.encode(req.getPassword()));
		u.setStatus(1);
		usersMapper.insert(u);
		return Result.success();
	}

	@PutMapping("/updateUser")
	public Result<Void> updateUser(@RequestBody @Valid UpdateUserReq req) {
		Users u = usersMapper.selectById(req.getId());
		if (u == null) return Result.failure("用户不存在");
		u.setEmployeeId(req.getEmployeeId());
		u.setName(req.getName());
		u.setPhone(req.getPhone());
		u.setDepartment(req.getDepartment());
		usersMapper.updateById(u);
		return Result.success();
	}

	@DeleteMapping("/deleteUser")
	public Result<Void> deleteUser(@RequestParam String ids) {
		for (String s : ids.split(",")) {
			if (!s.isBlank()) {
				usersMapper.deleteById(Integer.parseInt(s.trim()));
			}
		}
		return Result.success();
	}

	@PostMapping("/importUsers")
	public Result<Map<String, Object>> importUsers(MultipartFile file) {
		int success = 0;
		int fail = 0;
		try (InputStream is = file.getInputStream()) {
			// TODO: 使用 Apache POI 解析Excel，这里先返回占位统计
			success = 0;
			fail = 0;
		} catch (Exception e) {
			return Result.failure("导入失败: " + e.getMessage());
		}
		Map<String, Object> data = new HashMap<>();
		data.put("successCount", success);
		data.put("failCount", fail);
		data.put("failList", java.util.List.of());
		return Result.success(data);
	}

	@Data
	public static class QueryReq {
		private String name;
		private String employeeId;
		private String department;
		private Integer page = 1;
		private Integer size = 10;
	}

	@Data
	public static class AddUserReq {
		@NotBlank private String employeeId;
		@NotBlank private String name;
		@NotBlank private String phone;
		@NotBlank private String department;
		@NotBlank private String password;
		@NotNull private Integer role; // 预留，暂未落表
	}

	@Data
	public static class UpdateUserReq {
		@NotNull private Integer id;
		@NotBlank private String employeeId;
		@NotBlank private String name;
		@NotBlank private String phone;
		@NotBlank private String department;
		@NotNull private Integer role; // 预留
	}
}





