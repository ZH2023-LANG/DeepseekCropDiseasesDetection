package com.example.Kcsj.api.v1.task.dto;

import javax.validation.constraints.NotBlank;

public class ImageTaskCreateRequest {
    @NotBlank(message = "weight不能为空")
    private String weight;

    @NotBlank(message = "inputImg不能为空")
    private String inputImg;

    @NotBlank(message = "conf不能为空")
    private String conf;

    @NotBlank(message = "startTime不能为空")
    private String startTime;

    private String ai;

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getInputImg() {
        return inputImg;
    }

    public void setInputImg(String inputImg) {
        this.inputImg = inputImg;
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

    public String getAi() {
        return ai;
    }

    public void setAi(String ai) {
        this.ai = ai;
    }
}
