package com.example.zerolog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class ZeroLogController {

    @GetMapping("/")
    public String welcome() {
        String message = "Welcome to Zero Log";
        log.info("logging {}", message);
        return message;
    }

    @GetMapping("/get")
    public User getUser(@RequestParam(name="userId") Long userId) {
        log.info("logging: GET User Call with ID{}", userId);        
        return new User(999L, "John Doe", "john-doe@nowhere.com", "123 North Main St.");
    }

    @PostMapping("/post")
    public User postUser(@RequestParam(name="userId") Long userId) {
        log.info("logging: POST User Call with ID{}", userId);        
        return new User(999L, "John Doe", "john-doe@nowhere.com", "123 North Main St.");
    }

    @PostMapping("/body")
    @ResponseBody
    public User postUserBody(@RequestBody User user) {
        log.info("logging: POST User Call with ID{}", user.id());        
        return new User(999L, "John Doe", "john-doe@nowhere.com", "123 North Main St.");
    }
    
}
