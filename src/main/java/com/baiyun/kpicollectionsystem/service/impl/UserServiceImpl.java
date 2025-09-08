package com.baiyun.kpicollectionsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baiyun.kpicollectionsystem.entity.Users;
import com.baiyun.kpicollectionsystem.mapper.UsersMapper;
import com.baiyun.kpicollectionsystem.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

	private final UsersMapper usersMapper;
	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(UsersMapper usersMapper, PasswordEncoder passwordEncoder) {
		this.usersMapper = usersMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public Users getByEmployeeId(String employeeId) {
		return usersMapper.selectOne(new LambdaQueryWrapper<Users>().eq(Users::getEmployeeId, employeeId));
	}

	@Override
	public void updatePassword(Integer id, String rawPassword) {
		Users u = usersMapper.selectById(id);
		if (u == null) throw new IllegalArgumentException("用户不存在");
		u.setPassword(passwordEncoder.encode(rawPassword));
		usersMapper.updateById(u);
	}

	@Override
	public void saveBatch(List<Users> users) {
        usersMapper.saveBatch(users);
    }
}



