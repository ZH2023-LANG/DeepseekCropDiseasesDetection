package com.example.Kcsj.api.v1.task.dto;

public class TaskCreatedResponse {
    private String taskNo;
    private String status;

    public String getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(String taskNo) {
        this.taskNo = taskNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
