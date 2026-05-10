package com.example.Kcsj.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Kcsj.api.v1.task.dto.TaskCreatedResponse;
import com.example.Kcsj.api.v1.task.dto.TaskViewResponse;
import com.example.Kcsj.common.ApiException;
import com.example.Kcsj.common.ErrorCode;
import com.example.Kcsj.common.TaskStatus;
import com.example.Kcsj.common.TaskType;
import com.example.Kcsj.entity.AsyncTask;
import com.example.Kcsj.entity.ImgRecords;
import com.example.Kcsj.mapper.AsyncTaskMapper;
import com.example.Kcsj.mapper.ImgRecordsMapper;
import com.example.Kcsj.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;

@Service
public class AsyncTaskService {
    private final AsyncTaskMapper asyncTaskMapper;
    private final ImgRecordsMapper imgRecordsMapper;
    private final RestTemplate restTemplate;
    private final Executor taskExecutor;
    private final AuditLogService auditLogService;

    @Value("${flask.base-url:http://127.0.0.1:5000}")
    private String flaskBaseUrl;

    public AsyncTaskService(AsyncTaskMapper asyncTaskMapper,
                            ImgRecordsMapper imgRecordsMapper,
                            RestTemplate restTemplate,
                            @Qualifier("taskExecutor") Executor taskExecutor,
                            AuditLogService auditLogService) {
        this.asyncTaskMapper = asyncTaskMapper;
        this.imgRecordsMapper = imgRecordsMapper;
        this.restTemplate = restTemplate;
        this.taskExecutor = taskExecutor;
        this.auditLogService = auditLogService;
    }

    public TaskCreatedResponse createImageTask(Map<String, Object> payload) {
        return createTask(TaskType.IMAGE_PREDICT, payload);
    }

    public TaskCreatedResponse createBatchTask(Map<String, Object> payload) {
        return createTask(TaskType.BATCH_IMAGE_PREDICT, payload);
    }

    public TaskCreatedResponse createVideoTask(Map<String, Object> payload) {
        return createTask(TaskType.VIDEO_PREDICT, payload);
    }

    public TaskCreatedResponse createCameraTask(Map<String, Object> payload) {
        return createTask(TaskType.CAMERA_PREDICT, payload);
    }

    public TaskViewResponse getTask(String taskNo) {
        AsyncTask task = asyncTaskMapper.selectByTaskNo(taskNo);
        if (task == null) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "task not found");
        }
        checkOwner(task);
        return toView(task);
    }

    public Page<TaskViewResponse> listTasks(int pageNum, int pageSize, String status) {
        LambdaQueryWrapper<AsyncTask> wrapper = new LambdaQueryWrapper<>();
        if (!SecurityUtils.isAdmin()) {
            wrapper.eq(AsyncTask::getOwnerId, SecurityUtils.currentUserId());
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(AsyncTask::getStatus, status);
        }
        wrapper.orderByDesc(AsyncTask::getCreatedAt);

        Page<AsyncTask> page = asyncTaskMapper.selectPage(new Page<AsyncTask>(pageNum, pageSize), wrapper);
        Page<TaskViewResponse> output = new Page<TaskViewResponse>(pageNum, pageSize, page.getTotal());
        output.setRecords(new java.util.ArrayList<TaskViewResponse>());
        for (AsyncTask item : page.getRecords()) {
            output.getRecords().add(toView(item));
        }
        return output;
    }

    public void cancelTask(String taskNo) {
        AsyncTask task = asyncTaskMapper.selectByTaskNo(taskNo);
        if (task == null) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "task not found");
        }
        checkOwner(task);
        if (TaskStatus.SUCCESS.equals(task.getStatus()) || TaskStatus.FAILED.equals(task.getStatus())) {
            throw new ApiException(ErrorCode.CONFLICT, "finished task cannot be cancelled");
        }
        task.setStatus(TaskStatus.CANCELLED);
        task.setUpdatedAt(LocalDateTime.now());
        task.setFinishedAt(LocalDateTime.now());
        asyncTaskMapper.updateById(task);
        auditLogService.tryLog(SecurityUtils.currentUserId(), "TASK_CANCEL", "ASYNC_TASK", task.getTaskNo(), "cancel");
    }

    public void retryTask(String taskNo) {
        AsyncTask task = asyncTaskMapper.selectByTaskNo(taskNo);
        if (task == null) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "task not found");
        }
        checkOwner(task);
        if (!TaskStatus.FAILED.equals(task.getStatus()) && !TaskStatus.CANCELLED.equals(task.getStatus())) {
            throw new ApiException(ErrorCode.CONFLICT, "only failed or cancelled task can retry");
        }
        task.setStatus(TaskStatus.PENDING);
        task.setErrorCode(null);
        task.setErrorMessage(null);
        task.setProgress(0);
        task.setStartedAt(null);
        task.setFinishedAt(null);
        task.setUpdatedAt(LocalDateTime.now());
        asyncTaskMapper.updateById(task);
        submitTask(task.getTaskNo());
        auditLogService.tryLog(SecurityUtils.currentUserId(), "TASK_RETRY", "ASYNC_TASK", task.getTaskNo(), "retry");
    }

    private TaskCreatedResponse createTask(String taskType, Map<String, Object> payload) {
        String taskNo = "TSK" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        AsyncTask task = new AsyncTask();
        task.setTaskNo(taskNo);
        task.setTaskType(taskType);
        task.setOwnerId(SecurityUtils.currentUserId());
        task.setRequestJson(JSON.toJSONString(payload));
        task.setStatus(TaskStatus.PENDING);
        task.setProgress(0);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        asyncTaskMapper.insert(task);

        submitTask(taskNo);
        auditLogService.tryLog(SecurityUtils.currentUserId(), "TASK_CREATE", "ASYNC_TASK", taskNo, taskType);

        TaskCreatedResponse response = new TaskCreatedResponse();
        response.setTaskNo(taskNo);
        response.setStatus(TaskStatus.PENDING);
        return response;
    }

    private void submitTask(final String taskNo) {
        taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                executeTask(taskNo);
            }
        });
    }

    private void executeTask(String taskNo) {
        AsyncTask task = asyncTaskMapper.selectByTaskNo(taskNo);
        if (task == null || TaskStatus.CANCELLED.equals(task.getStatus())) {
            return;
        }
        task.setStatus(TaskStatus.RUNNING);
        task.setProgress(20);
        task.setStartedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        asyncTaskMapper.updateById(task);

        try {
            JSONObject requestJson = JSON.parseObject(task.getRequestJson());
            JSONObject result;
            if (TaskType.IMAGE_PREDICT.equals(task.getTaskType())) {
                result = callFlask("/predictImg", requestJson);
                persistImageRecord(requestJson, result);
            } else if (TaskType.BATCH_IMAGE_PREDICT.equals(task.getTaskType())) {
                result = callFlask("/predictImgBatch", requestJson);
            } else {
                throw new ApiException(ErrorCode.TASK_EXECUTION_ERROR, "video/camera async is not supported by current flask stream interface");
            }

            task.setStatus(TaskStatus.SUCCESS);
            task.setProgress(100);
            task.setResultJson(result.toJSONString());
            task.setErrorCode(null);
            task.setErrorMessage(null);
            task.setFinishedAt(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
            asyncTaskMapper.updateById(task);
        } catch (ApiException ex) {
            markFailed(task, ex.getErrorCode().name(), ex.getMessage());
        } catch (Exception ex) {
            markFailed(task, ErrorCode.UPSTREAM_FLASK_ERROR.name(), ex.getMessage());
        }
    }

    private JSONObject callFlask(String path, JSONObject payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<String>(payload.toJSONString(), headers);
        String response = restTemplate.postForObject(flaskBaseUrl + path, requestEntity, String.class);
        if (!StringUtils.hasText(response)) {
            throw new ApiException(ErrorCode.UPSTREAM_FLASK_ERROR, "empty response from flask");
        }
        JSONObject json = JSON.parseObject(response);
        if (json == null) {
            throw new ApiException(ErrorCode.UPSTREAM_FLASK_ERROR, "flask response parse error");
        }
        if (json.containsKey("status") && Integer.valueOf(400).equals(json.getInteger("status"))) {
            throw new ApiException(ErrorCode.UPSTREAM_FLASK_ERROR, json.getString("message"));
        }
        return json;
    }

    private void persistImageRecord(JSONObject request, JSONObject result) {
        ImgRecords imgRecords = new ImgRecords();
        imgRecords.setWeight(request.getString("weight"));
        imgRecords.setConf(request.getString("conf"));
        imgRecords.setInputImg(request.getString("inputImg"));
        imgRecords.setUsername(request.getString("username"));
        imgRecords.setStartTime(request.getString("startTime"));
        imgRecords.setAi(request.getString("ai"));
        imgRecords.setLable(result.getString("label"));
        imgRecords.setConfidence(result.getString("confidence"));
        imgRecords.setAllTime(result.getString("allTime"));
        imgRecords.setOutImg(result.getString("outImg"));
        imgRecords.setSuggestion(result.getString("suggestion"));
        imgRecordsMapper.insert(imgRecords);
    }

    private void markFailed(AsyncTask task, String errorCode, String errorMessage) {
        task.setStatus(TaskStatus.FAILED);
        task.setProgress(100);
        task.setErrorCode(errorCode);
        task.setErrorMessage(errorMessage);
        task.setFinishedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        asyncTaskMapper.updateById(task);
    }

    private void checkOwner(AsyncTask task) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        if (!task.getOwnerId().equals(SecurityUtils.currentUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "no permission for this task");
        }
    }

    private TaskViewResponse toView(AsyncTask task) {
        TaskViewResponse view = new TaskViewResponse();
        view.setTaskNo(task.getTaskNo());
        view.setTaskType(task.getTaskType());
        view.setOwnerId(task.getOwnerId());
        view.setStatus(task.getStatus());
        view.setProgress(task.getProgress());
        view.setErrorCode(task.getErrorCode());
        view.setErrorMessage(task.getErrorMessage());
        view.setResultJson(task.getResultJson());
        view.setStartedAt(task.getStartedAt());
        view.setFinishedAt(task.getFinishedAt());
        view.setCreatedAt(task.getCreatedAt());
        view.setUpdatedAt(task.getUpdatedAt());
        return view;
    }

    public Map<String, Object> buildImageTaskPayload(String username, String weight, String inputImg, String conf, String startTime, String ai) {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("username", username);
        payload.put("weight", weight);
        payload.put("inputImg", inputImg);
        payload.put("conf", conf);
        payload.put("startTime", startTime);
        payload.put("ai", ai == null ? "" : ai);
        return payload;
    }

    public Map<String, Object> buildBatchTaskPayload(String username, String weight, String imgFolderUrl, String conf) {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("username", username);
        payload.put("weight", weight);
        payload.put("imgFolderUrl", imgFolderUrl);
        payload.put("conf", conf);
        return payload;
    }

    public Map<String, Object> buildVideoTaskPayload(String username, String weight, String inputVideo, String conf, String startTime) {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("username", username);
        payload.put("weight", weight);
        payload.put("inputVideo", inputVideo);
        payload.put("conf", conf);
        payload.put("startTime", startTime);
        return payload;
    }

    public Map<String, Object> buildCameraTaskPayload(String username, String weight, String conf, String startTime) {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("username", username);
        payload.put("weight", weight);
        payload.put("conf", conf);
        payload.put("startTime", startTime);
        return payload;
    }
}
