package com.example.Kcsj.api.v1.task.dto;

import javax.validation.constraints.NotBlank;

public class CameraTaskCreateRequest {
    @NotBlank(message = "weight不能为空")
    private String weight;

    @NotBlank(message = "conf不能为空")
    private String conf;

    @NotBlank(message = "startTime不能为空")
    private String startTime;

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getConf() {
        return conf;
    }

    public void setConf(String conf) {
        this.conf = conf;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}
