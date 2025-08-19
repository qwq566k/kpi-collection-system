package com.baiyun.kpicollectionsystem.service;

import com.baiyun.kpicollectionsystem.entity.Users;

public interface UserService {
	Users getByEmployeeId(String employeeId);
	void updatePassword(Integer id, String rawPassword);
}



