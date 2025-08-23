package com.baiyun.kpicollectionsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baiyun.kpicollectionsystem.common.Result;
import com.baiyun.kpicollectionsystem.entity.Users;
import com.baiyun.kpicollectionsystem.mapper.UsersMapper;
import com.baiyun.kpicollectionsystem.util.JwtUtil;
import com.baiyun.kpicollectionsystem.service.UserService;
import com.baiyun.kpicollectionsystem.entity.SysUserRole;
import com.baiyun.kpicollectionsystem.entity.SysRole;
import com.baiyun.kpicollectionsystem.mapper.SysUserRoleMapper;
import com.baiyun.kpicollectionsystem.mapper.SysRoleMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

	private final UsersMapper usersMapper;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final UserService userService;
	private final SysUserRoleMapper userRoleMapper;
	private final SysRoleMapper roleMapper;

	public UserController(UsersMapper usersMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, UserService userService, SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper) {
		this.usersMapper = usersMapper;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
		this.userService = userService;
		this.userRoleMapper = userRoleMapper;
		this.roleMapper = roleMapper;
	}

	@PostMapping("/login")
	public Result<Map<String, Object>> login(@RequestBody @Valid LoginRequest req) {
		Users user = usersMapper.selectOne(new LambdaQueryWrapper<Users>().eq(Users::getEmployeeId, req.getEmployeeId()));
		if (user == null || user.getStatus() != null && user.getStatus() == 0) {
			return Result.failure("用户不存在或已禁用");
		}
		if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
			return Result.failure("账号或密码不正确");
		}
		// 从用户角色表查询角色
		String role = "teacher";
		SysUserRole ur = userRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()));
		if (ur != null) {
			SysRole r = roleMapper.selectById(ur.getRoleId());
			if (r != null && r.getCode() != null && !r.getCode().isBlank()) {
				role = r.getCode();
			}
		}
		String token = jwtUtil.generateToken(user.getId(), user.getName(), role);
		Map<String, Object> data = new HashMap<>();
		data.put("id", user.getId());
		data.put("token", token);
		return Result.success(data);
	}

	@PostMapping("/logout")
	public Result<Void> logout() {
		return Result.success();
	}

	@PutMapping("/updatePassword")
	public Result<Void> updatePassword(@RequestBody @Valid UpdatePasswordRequest req) {
		userService.updatePassword(req.getId(), req.getNewPassword());
		return Result.success();
	}

	@GetMapping("/me")
	public Result<Map<String, Object>> me(@RequestHeader(value = "Authorization", required = false) String auth) {
		Map<String, Object> data = new HashMap<>();
		if (auth != null && auth.startsWith("Bearer ")) {
			var claims = jwtUtil.parseToken(auth.substring(7));
			data.put("id", claims.get("id"));
			data.put("name", claims.get("name"));
			data.put("role", claims.get("role"));
		}
		return Result.success(data);
	}

	@Data
	public static class LoginRequest {
		@NotBlank
		private String employeeId;
		@NotBlank
		private String password;
	}

	@Data
	public static class UpdatePasswordRequest {
		@NotNull
		private Integer id;
		@NotBlank
		private String newPassword;
	}
}


