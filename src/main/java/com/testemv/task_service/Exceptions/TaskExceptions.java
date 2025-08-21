package com.testemv.task_service.Exceptions;

public class TaskExceptions {

    public static class TaskNotFoundException extends RuntimeException {
        public TaskNotFoundException(Long id) {
            super("Task não encontrada com id: " + id);
        }
    }

    public static class InvalidTaskStatusException extends RuntimeException {
        public InvalidTaskStatusException(String status) {
            super("Somente tarefas com status 'Pendente' ou 'Em Andamento' podem ser editadas. Status atual: " + status);
        }
    }

    public static class UserNotFoundForTaskException extends RuntimeException {
        public UserNotFoundForTaskException(Long userId) {
            super("Usuário responsável não encontrado com id: " + userId);
        }
    }
}
