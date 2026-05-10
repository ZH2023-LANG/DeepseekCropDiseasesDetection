package com.example.Kcsj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.Kcsj.entity.TrainingJob;
import org.apache.ibatis.annotations.Select;

public interface TrainingJobMapper extends BaseMapper<TrainingJob> {
    @Select("select * from training_job where job_no = #{jobNo} limit 1")
    TrainingJob selectByJobNo(String jobNo);
}
