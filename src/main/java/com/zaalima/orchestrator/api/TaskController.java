package com.zaalima.orchestrator.api;

import com.zaalima.orchestrator.api.dto.SubmitTaskRequest;
import com.zaalima.orchestrator.api.dto.TaskResponse;
import com.zaalima.orchestrator.core.Task;
import com.zaalima.orchestrator.core.TaskService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> submit(@Valid @RequestBody SubmitTaskRequest request) {
        Task task = taskService.submit(request.capability(), request.payload());
        return ResponseEntity.accepted()
                .location(URI.create("/api/v1/tasks/" + task.taskId()))
                .body(TaskResponse.from(task));
    }

    @GetMapping("/{taskId}")
    public TaskResponse findById(@PathVariable UUID taskId) {
        return TaskResponse.from(taskService.findById(taskId));
    }

    @DeleteMapping("/{taskId}")
    public TaskResponse cancel(@PathVariable UUID taskId) {
        return TaskResponse.from(taskService.cancel(taskId));
    }
}
