package com.commit.crm.dto.response;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record DashboardResponse(
        Summary summary,
        Map<String, Long> pipelineChart,
        List<TaskSummary> recentTasks
) {
    @Builder public record Summary(long totalLeads, long leadsThisMonth, long tasksPending, long tasksOverdue) {}
    @Builder public record TaskSummary(String title, String status, String leadName, long daysOverdue) {}
}
