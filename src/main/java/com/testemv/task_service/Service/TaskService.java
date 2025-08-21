package com.testemv.task_service.Service;

import com.testemv.task_service.Entities.Task;
import com.testemv.task_service.Repository.TaskRepository;
import com.testemv.task_service.Feign.UserClient;
import com.testemv.task_service.DTO.UserDTO;
import com.testemv.task_service.Exceptions.TaskExceptions.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserClient userClient;

    public TaskService(TaskRepository taskRepository, UserClient userClient) {
        this.taskRepository = taskRepository;
        this.userClient = userClient;
    }

    public Task createTask(Task task) {
        UserDTO user = userClient.getUserById(task.getUserId());
        if (user == null) {
            throw new UserNotFoundForTaskException(task.getUserId());
        }
        return taskRepository.save(task);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getTasksByUserId(Long userId) {
        return taskRepository.findByUserId(userId);
    }

    public Task updateTask(Long id, Task taskUpdated) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (!taskUpdated.getStatus().equals("Pendente") && !taskUpdated.getStatus().equals("Em Andamento")) {
            throw new InvalidTaskStatusException(task.getStatus());
        }

        task.setTitle(taskUpdated.getTitle());
        task.setDescription(taskUpdated.getDescription());
        task.setStatus(taskUpdated.getStatus());
        task.setDueDate(taskUpdated.getDueDate());
        task.setUserId(taskUpdated.getUserId());

        UserDTO user = userClient.getUserById(task.getUserId());
        if (user == null) {
            throw new UserNotFoundForTaskException(task.getUserId());
        }

        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        taskRepository.delete(task);
    }
}
