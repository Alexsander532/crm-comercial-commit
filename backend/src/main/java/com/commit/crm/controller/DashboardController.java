package com.commit.crm.controller;

import com.commit.crm.dto.response.ApiResponse;
import com.commit.crm.dto.response.DashboardResponse;
import com.commit.crm.model.User;
import com.commit.crm.service.DashboardService;
import com.commit.crm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal String userId
    ) {
        User user = userService.findById(UUID.fromString(userId));
        DashboardResponse response = dashboardService.getDashboard(user);
        return ResponseEntity.ok(ApiResponse.success(response, "Dashboard carregado"));
    }
}
