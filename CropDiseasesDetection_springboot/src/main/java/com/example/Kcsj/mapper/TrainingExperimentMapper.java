package com.example.Kcsj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.Kcsj.entity.TrainingExperiment;
import org.apache.ibatis.annotations.Select;

public interface TrainingExperimentMapper extends BaseMapper<TrainingExperiment> {
    @Select("select * from training_experiment where experiment_no = #{experimentNo} limit 1")
    TrainingExperiment selectByExperimentNo(String experimentNo);
}
