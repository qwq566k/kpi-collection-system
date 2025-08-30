package com.baiyun.kpicollectionsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baiyun.kpicollectionsystem.entity.ScoreStandard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ScoreStandardMapper extends BaseMapper<ScoreStandard> {
    @Select("select scoring_instruction from score_standard where id = #{id}")
    String selectInstructionById(Integer id);

    @Select("select ao.name from assessment_org ao join assessment_relations ar on ar.org_id = ao.id where ar.standard_id = #{id}")
    String selectOrgById(Integer id);
}





