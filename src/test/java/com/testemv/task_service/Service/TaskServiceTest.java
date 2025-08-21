package com.testemv.task_service.Service;

import com.testemv.task_service.DTO.UserDTO;
import com.testemv.task_service.Entities.Task;
import com.testemv.task_service.Exceptions.TaskExceptions.*;
import com.testemv.task_service.Feign.UserClient;
import com.testemv.task_service.Repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserClient userClient;

    private Task task;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        task = new Task();
        task.setId(1L);
        task.setTitle("Teste");
        task.setDescription("Descrição da task");
        task.setStatus("Pendente");
        task.setDueDate(LocalDateTime.now().plusDays(7));
        task.setUserId(100L);
    }

    @Test
    void createTask_Success() {
        when(userClient.getUserById(task.getUserId())).thenReturn(new UserDTO(100L, "Carlos", "carlos@test.com"));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.createTask(task);

        assertEquals("Teste", result.getTitle());
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void createTask_UserNotFound_ThrowsException() {
        when(userClient.getUserById(task.getUserId())).thenReturn(null);

        Exception ex = assertThrows(UserNotFoundForTaskException.class, () -> taskService.createTask(task));
        assertTrue(ex.getMessage().contains("Usuário responsável não encontrado"));

        verify(taskRepository, never()).save(any());
    }

    @Test
    void getAllTasks_ReturnsList() {
        when(taskRepository.findAll()).thenReturn(List.of(task));

        List<Task> result = taskService.getAllTasks();

        assertEquals(1, result.size());
        assertEquals("Teste", result.get(0).getTitle());
    }

    @Test
    void getTasksByUserId_ReturnsList() {
        when(taskRepository.findByUserId(100L)).thenReturn(List.of(task));

        List<Task> result = taskService.getTasksByUserId(100L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getUserId());
    }

    @Test
    void updateTask_Success() {
        Task updated = new Task();
        updated.setTitle("Novo título");
        updated.setDescription("Nova descrição");
        updated.setStatus("Em Andamento");
        updated.setDueDate(LocalDateTime.now().plusDays(10));
        updated.setUserId(200L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userClient.getUserById(200L)).thenReturn(new UserDTO(200L, "Maria", "maria@test.com"));
        when(taskRepository.save(any(Task.class))).thenReturn(updated);

        Task result = taskService.updateTask(1L, updated);

        assertEquals("Novo título", result.getTitle());
        assertEquals("Em Andamento", result.getStatus());
        assertEquals(200L, result.getUserId());
    }

    @Test
    void updateTask_TaskNotFound_ThrowsException() {
        when(taskRepository.findById(2L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(2L, task));
        assertTrue(ex.getMessage().contains("Task não encontrada"));
    }

    @Test
    void updateTask_InvalidStatus_ThrowsException() {
        task.setStatus("Concluída");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Exception ex = assertThrows(InvalidTaskStatusException.class, () -> taskService.updateTask(1L, task));
        assertTrue(ex.getMessage().contains("Somente tarefas com status 'Pendente'"));
    }

    @Test
    void updateTask_UserNotFound_ThrowsException() {
        Task updated = new Task();
        updated.setStatus("Pendente");
        updated.setUserId(200L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userClient.getUserById(200L)).thenReturn(null);

        Exception ex = assertThrows(UserNotFoundForTaskException.class, () -> taskService.updateTask(1L, updated));
        assertTrue(ex.getMessage().contains("Usuário responsável não encontrado"));
    }

    @Test
    void deleteTask_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertDoesNotThrow(() -> taskService.deleteTask(1L));
        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void deleteTask_TaskNotFound_ThrowsException() {
        when(taskRepository.findById(2L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(2L));
        assertTrue(ex.getMessage().contains("Task não encontrada"));

        verify(taskRepository, never()).delete(any());
    }
}
