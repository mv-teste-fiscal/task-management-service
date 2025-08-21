package com.testemv.task_service.Integration.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testemv.task_service.DTO.UserDTO;
import com.testemv.task_service.Entities.Task;
import com.testemv.task_service.Exceptions.TaskExceptions.*;
import com.testemv.task_service.Repository.TaskRepository;
import com.testemv.task_service.Feign.UserClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    private Task task;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();

        UserDTO userDTO = new UserDTO(1L, "Carlos", "carlos@test.com");
        when(userClient.getUserById(1L)).thenReturn(userDTO);

        task = new Task();
        task.setTitle("Tarefa Teste");
        task.setDescription("Descrição da tarefa");
        task.setStatus("Pendente");
        task.setUserId(1L);
        task.setDueDate(LocalDateTime.now().plusDays(7));

        task = taskRepository.save(task);
    }

    @Test
    void getTasks_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/task"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Tarefa Teste")));
    }

    @Test
    void getTasksByUserId_ShouldReturnTasks() throws Exception {
        mockMvc.perform(get("/task/userId/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId", is(1)));
    }

    @Test
    void createTask_ShouldReturnCreatedTask() throws Exception {
        Task newTask = new Task();
        newTask.setTitle("Nova Tarefa");
        newTask.setDescription("Descrição nova");
        newTask.setStatus("Pendente");
        newTask.setUserId(1L);
        newTask.setDueDate(LocalDateTime.now().plusDays(3));

        mockMvc.perform(post("/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTask)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Nova Tarefa")))
                .andExpect(jsonPath("$.userId", is(1)));
    }

    @Test
    void createTask_UserNotFound_ShouldReturn404() throws Exception {
        Task newTask = new Task();
        newTask.setTitle("Nova Tarefa");
        newTask.setDescription("Descrição nova");
        newTask.setStatus("Pendente");
        newTask.setUserId(999L);
        newTask.setDueDate(LocalDateTime.now().plusDays(3));

        when(userClient.getUserById(999L)).thenReturn(null);

        mockMvc.perform(post("/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTask)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Usuário responsável não encontrado")));
    }

    @Test
    void updateTask_ShouldReturnUpdatedTask() throws Exception {
        task.setTitle("Tarefa Atualizada");
        task.setStatus("Em Andamento");

        mockMvc.perform(put("/task/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Tarefa Atualizada")))
                .andExpect(jsonPath("$.status", is("Em Andamento")));
    }

    @Test
    void updateTask_InvalidStatus_ShouldReturn400() throws Exception {
        task.setStatus("Concluída");

        mockMvc.perform(put("/task/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Somente tarefas com status 'Pendente'")));
    }

    @Test
    void deleteTask_ShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(delete("/task/{id}", task.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tarefa deletada com sucesso.")));
    }

    @Test
    void deleteTask_TaskNotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/task/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Task não encontrada")));
    }
}
