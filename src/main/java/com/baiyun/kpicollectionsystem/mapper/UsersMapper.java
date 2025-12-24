package com.baiyun.kpicollectionsystem.mapper;

import com.baiyun.kpicollectionsystem.entity.SysUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baiyun.kpicollectionsystem.entity.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UsersMapper extends BaseMapper<Users> {
    void saveBatch(List<Users> users);

    void batchInsertRoleWithUser(List<SysUserRole> userRoles);

    @Select("select id from users where employee_id = #{employeeId}")
    int getIdByEmployeeId(String employeeId);

}



