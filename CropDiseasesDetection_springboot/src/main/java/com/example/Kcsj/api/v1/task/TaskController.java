package com.example.Kcsj.api.v1.task;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Kcsj.api.v1.task.dto.TaskViewResponse;
import com.example.Kcsj.common.ApiResponse;
import com.example.Kcsj.service.AsyncTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
@Validated
public class TaskController {
    private final AsyncTaskService asyncTaskService;

    public TaskController(AsyncTaskService asyncTaskService) {
        this.asyncTaskService = asyncTaskService;
    }

    @GetMapping("/{taskNo}")
    public ApiResponse<TaskViewResponse> getTask(@PathVariable String taskNo) {
        return ApiResponse.success(asyncTaskService.getTask(taskNo));
    }

    @GetMapping
    public ApiResponse<Page<TaskViewResponse>> listTasks(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String status) {
        return ApiResponse.success(asyncTaskService.listTasks(pageNum, pageSize, status));
    }

    @PostMapping("/{taskNo}/cancel")
    public ApiResponse<Void> cancelTask(@PathVariable String taskNo) {
        asyncTaskService.cancelTask(taskNo);
        return ApiResponse.success(null);
    }

    @PostMapping("/{taskNo}/retry")
    public ApiResponse<Void> retryTask(@PathVariable String taskNo) {
        asyncTaskService.retryTask(taskNo);
        return ApiResponse.success(null);
    }
}
