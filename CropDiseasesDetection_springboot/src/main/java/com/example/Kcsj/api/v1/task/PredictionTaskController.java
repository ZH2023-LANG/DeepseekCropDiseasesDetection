package com.example.Kcsj.api.v1.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.Kcsj.api.v1.task.dto.BatchImageTaskCreateRequest;
import com.example.Kcsj.api.v1.task.dto.CameraTaskCreateRequest;
import com.example.Kcsj.api.v1.task.dto.ImageTaskCreateRequest;
import com.example.Kcsj.api.v1.task.dto.TaskCreatedResponse;
import com.example.Kcsj.api.v1.task.dto.VideoTaskCreateRequest;
import com.example.Kcsj.common.ApiException;
import com.example.Kcsj.common.ApiResponse;
import com.example.Kcsj.common.ErrorCode;
import com.example.Kcsj.entity.User;
import com.example.Kcsj.mapper.UserMapper;
import com.example.Kcsj.security.SecurityUtils;
import com.example.Kcsj.service.AsyncTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/predictions")
@Validated
public class PredictionTaskController {
    private final AsyncTaskService asyncTaskService;
    private final UserMapper userMapper;

    public PredictionTaskController(AsyncTaskService asyncTaskService, UserMapper userMapper) {
        this.asyncTaskService = asyncTaskService;
        this.userMapper = userMapper;
    }

    @PostMapping("/image-tasks")
    public ApiResponse<TaskCreatedResponse> createImageTask(@Valid @RequestBody ImageTaskCreateRequest request) {
        String username = currentUsername();
        Map<String, Object> payload = asyncTaskService.buildImageTaskPayload(
            username,
            request.getWeight(),
            request.getInputImg(),
            request.getConf(),
            request.getStartTime(),
            request.getAi()
        );
        return ApiResponse.success(asyncTaskService.createImageTask(payload));
    }

    @PostMapping("/batch-image-tasks")
    public ApiResponse<TaskCreatedResponse> createBatchTask(@Valid @RequestBody BatchImageTaskCreateRequest request) {
        String username = currentUsername();
        Map<String, Object> payload = asyncTaskService.buildBatchTaskPayload(
            username,
            request.getWeight(),
            request.getImgFolderUrl(),
            request.getConf()
        );
        return ApiResponse.success(asyncTaskService.createBatchTask(payload));
    }

    @PostMapping("/video-tasks")
    public ApiResponse<TaskCreatedResponse> createVideoTask(@Valid @RequestBody VideoTaskCreateRequest request) {
        String username = currentUsername();
        Map<String, Object> payload = asyncTaskService.buildVideoTaskPayload(
            username,
            request.getWeight(),
            request.getInputVideo(),
            request.getConf(),
            request.getStartTime()
        );
        return ApiResponse.success(asyncTaskService.createVideoTask(payload));
    }

    @PostMapping("/camera-tasks")
    public ApiResponse<TaskCreatedResponse> createCameraTask(@Valid @RequestBody CameraTaskCreateRequest request) {
        String username = currentUsername();
        Map<String, Object> payload = asyncTaskService.buildCameraTaskPayload(
            username,
            request.getWeight(),
            request.getConf(),
            request.getStartTime()
        );
        return ApiResponse.success(asyncTaskService.createCameraTask(payload));
    }

    private String currentUsername() {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", SecurityUtils.currentUserId()));
        if (user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "user not found");
        }
        return user.getUsername();
    }
}
