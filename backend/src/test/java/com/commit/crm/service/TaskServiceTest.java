package com.commit.crm.service;

import com.commit.crm.dto.request.TaskRequest;
import com.commit.crm.dto.response.TaskResponse;
import com.commit.crm.model.*;
import com.commit.crm.repository.LeadRepository;
import com.commit.crm.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private LeadRepository leadRepository;
    @InjectMocks private TaskService taskService;

    private User gerente() {
        return User.builder().id(UUID.randomUUID()).name("Gerente").email("g@c.com")
                .passwordHash("h").role(UserRole.GERENTE_PROSPECCAO).build();
    }

    private User funcionario() {
        return User.builder().id(UUID.randomUUID()).name("Func").email("f@c.com")
                .passwordHash("h").role(UserRole.PROSPECCAO).build();
    }

    @Test
    void shouldCreateTask() {
        User gerente = gerente();
        Lead lead = Lead.builder().id(UUID.randomUUID()).build();
        when(leadRepository.findById(lead.getId())).thenReturn(Optional.of(lead));
        when(taskRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TaskRequest req = new TaskRequest("Titulo", null, TaskPriority.ALTA, gerente.getId(), null);
        TaskResponse resp = taskService.create(lead.getId(), req, gerente);

        assertEquals("Titulo", resp.title());
        assertEquals(TaskPriority.ALTA, resp.priority());
    }

    @Test
    void shouldThrowWhenFuncionarioCreates() {
        User func = funcionario();
        TaskRequest req = new TaskRequest("Titulo", null, TaskPriority.MEDIA, func.getId(), null);
        assertThrows(SecurityException.class, () -> taskService.create(UUID.randomUUID(), req, func));
    }

    @Test
    void shouldCompleteTask() {
        User gerente = gerente();
        User func = funcionario();
        Lead lead = Lead.builder().id(UUID.randomUUID()).build();
        Task task = Task.builder().id(UUID.randomUUID()).title("Tarefa").lead(lead)
                .createdBy(gerente).assignedTo(func).build();
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TaskResponse resp = taskService.complete(task.getId(), func);
        assertEquals("CONCLUIDA", resp.status().name());
    }

    @Test
    void shouldCancelByCreator() {
        User gerente = gerente();
        Task task = Task.builder().id(UUID.randomUUID()).title("Tarefa").createdBy(gerente).build();
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        taskService.cancel(task.getId(), gerente);
        verify(taskRepository).save(any());
    }
}
