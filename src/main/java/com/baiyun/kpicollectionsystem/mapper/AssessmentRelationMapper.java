package com.baiyun.kpicollectionsystem.mapper;

import com.baiyun.kpicollectionsystem.entity.KeyIndicator;
import com.baiyun.kpicollectionsystem.entity.ScoreStandard;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baiyun.kpicollectionsystem.entity.AssessmentRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssessmentRelationMapper extends BaseMapper<AssessmentRelation> {
    @Select("select ki.* from assessment_relations ar join key_indicators ki on ar.indicator_id = ki.id where ar.field_id = #{fieldId} group by ki.id")
    List<KeyIndicator> selectIndicatorsListByFieldId(Integer fieldId);

    @Select("select ss.* from assessment_relations ar join score_standard ss on ar.standard_id = ss.id where ar.indicator_id = #{indicatorId}")
    List<ScoreStandard> selectStandardsListByIndicatorId(Integer indicatorId);
}





