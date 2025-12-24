package com.baiyun.kpicollectionsystem.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baiyun.kpicollectionsystem.entity.SysUserRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baiyun.kpicollectionsystem.entity.Users;
import com.baiyun.kpicollectionsystem.mapper.UsersMapper;
import com.baiyun.kpicollectionsystem.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void saveBatch(List<Users> users) {
        usersMapper.saveBatch(users);
		//建立用户角色关联
		// 1.验证ID是否回填成功
		for (Users user : users) {
			if (user.getId() == null) {
				throw new RuntimeException("用户ID回填失败: " + user.getUsername());
			}
		}
		//2.构建用户角色关系
		List<SysUserRole> userRoles = users.stream()
				.map(user -> {
					SysUserRole userRole = new SysUserRole();
					userRole.setUserId(user.getId()); // 这里已经有ID了
					userRole.setRoleId(2);//2 代表普通用户
					return userRole;
				})
				.toList();
		// 4. 批量插入用户角色
		usersMapper.batchInsertRoleWithUser(userRoles);
    }
}



