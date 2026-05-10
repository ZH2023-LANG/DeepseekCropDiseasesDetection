package com.example.Kcsj.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("training_metric")
public class TrainingMetric {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String jobNo;
    private Integer epochNo;
    private Double trainLoss;
    private Double valLoss;
    private Double map50;
    private Double map5095;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobNo() {
        return jobNo;
    }

    public void setJobNo(String jobNo) {
        this.jobNo = jobNo;
    }

    public Integer getEpochNo() {
        return epochNo;
    }

    public void setEpochNo(Integer epochNo) {
        this.epochNo = epochNo;
    }

    public Double getTrainLoss() {
        return trainLoss;
    }

    public void setTrainLoss(Double trainLoss) {
        this.trainLoss = trainLoss;
    }

    public Double getValLoss() {
        return valLoss;
    }

    public void setValLoss(Double valLoss) {
        this.valLoss = valLoss;
    }

    public Double getMap50() {
        return map50;
    }

    public void setMap50(Double map50) {
        this.map50 = map50;
    }

    public Double getMap5095() {
        return map5095;
    }

    public void setMap5095(Double map5095) {
        this.map5095 = map5095;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
