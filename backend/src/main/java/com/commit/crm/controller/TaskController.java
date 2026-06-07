package com.commit.crm.controller;

import com.commit.crm.dto.request.TaskRequest;
import com.commit.crm.dto.response.ApiResponse;
import com.commit.crm.dto.response.TaskResponse;
import com.commit.crm.model.User;
import com.commit.crm.service.TaskService;
import com.commit.crm.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    @PostMapping("/api/leads/{leadId}/tasks")
    public ResponseEntity<ApiResponse<TaskResponse>> create(
            @PathVariable UUID leadId,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal String userId
    ) {
        User user = userService.findById(UUID.fromString(userId));
        TaskResponse response = taskService.create(leadId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tarefa criada"));
    }

    @GetMapping("/api/leads/{leadId}/tasks")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> listByLead(
            @PathVariable UUID leadId
    ) {
        List<TaskResponse> tasks = taskService.getByLead(leadId);
        return ResponseEntity.ok(ApiResponse.success(tasks, "Tarefas do lead"));
    }

    @GetMapping("/api/tasks/my")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> myTasks(
            @AuthenticationPrincipal String userId
    ) {
        List<TaskResponse> tasks = taskService.getMyTasks(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(tasks, "Minhas tarefas"));
    }

    @PatchMapping("/api/tasks/{id}/complete")
    public ResponseEntity<ApiResponse<TaskResponse>> complete(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId
    ) {
        User user = userService.findById(UUID.fromString(userId));
        TaskResponse response = taskService.complete(id, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Tarefa concluída"));
    }

    @PatchMapping("/api/tasks/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId
    ) {
        User user = userService.findById(UUID.fromString(userId));
        taskService.cancel(id, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Tarefa cancelada"));
    }
}
