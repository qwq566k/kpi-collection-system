package com.baiyun.kpicollectionsystem.service;

import com.baiyun.kpicollectionsystem.entity.Users;

import java.util.List;

public interface UserService {
	Users getByEmployeeId(String employeeId);
	void updatePassword(Integer id, String rawPassword);

	void saveBatch(List<Users> users);
}



