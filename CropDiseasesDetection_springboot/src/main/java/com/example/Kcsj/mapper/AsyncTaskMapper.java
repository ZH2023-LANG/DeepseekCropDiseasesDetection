package com.example.Kcsj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.Kcsj.entity.AsyncTask;
import org.apache.ibatis.annotations.Select;

public interface AsyncTaskMapper extends BaseMapper<AsyncTask> {
    @Select("select * from async_task where task_no = #{taskNo} limit 1")
    AsyncTask selectByTaskNo(String taskNo);
}
