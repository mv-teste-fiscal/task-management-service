package com.testemv.task_service.Feign;

import com.testemv.task_service.DTO.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user.host}")
public interface UserClient {

    @GetMapping("/user/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}
