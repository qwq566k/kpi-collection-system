package com.baiyun.kpicollectionsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baiyun.kpicollectionsystem.entity.Users;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UsersMapper extends BaseMapper<Users> {
    void saveBatch(List<Users> users);
}



