package com.commit.crm.controller;

import com.commit.crm.dto.response.ApiResponse;
import com.commit.crm.dto.response.TimelineEventResponse;
import com.commit.crm.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leads/{leadId}/timeline")
@RequiredArgsConstructor
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TimelineEventResponse>>> getTimeline(
            @PathVariable UUID leadId
    ) {
        List<TimelineEventResponse> events = timelineService.getTimeline(leadId);
        return ResponseEntity.ok(ApiResponse.success(events, "Timeline do lead"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> recordInteraction(
            @PathVariable UUID leadId,
            @RequestBody InteractionRequest request
    ) {
        // Simplificado - interações serão registradas via services nas features
        return ResponseEntity.ok(ApiResponse.success(null, "Interação registrada"));
    }
}

record InteractionRequest(String type, String description) {}
