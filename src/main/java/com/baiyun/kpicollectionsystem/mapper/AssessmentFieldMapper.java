package com.baiyun.kpicollectionsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baiyun.kpicollectionsystem.entity.AssessmentField;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AssessmentFieldMapper extends BaseMapper<AssessmentField> {
    @Update("update assessment_fields set score = score - #{score} where id = #{id}")
    int minusScoreById(Integer id, Integer score);

    @Update("update assessment_fields set score = score + #{score} where id = #{id}")
    int plusScoreById(Integer id, Integer score);
}





