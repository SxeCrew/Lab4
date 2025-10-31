package com.example.userservice.controller;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void shouldCreateUser() throws Exception {
        UserRequest userRequest = new UserRequest("John Doe", "john@example.com", 30);
        UserResponse userResponse = new UserResponse(1L, "John Doe", "john@example.com", 30, LocalDateTime.now());

        when(userService.createUser(any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "name": "John Doe",
                        "email": "john@example.com",
                        "age": 30
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.age").value(30));

        verify(userService).createUser(any(UserRequest.class));
    }

    @Test
    void shouldGetUserById() throws Exception {
        UserResponse userResponse = new UserResponse(1L, "John Doe", "john@example.com", 30, LocalDateTime.now());

        when(userService.getUserById(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService).getUserById(1L);
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        List<UserResponse> users = Arrays.asList(
                new UserResponse(1L, "John Doe", "john@example.com", 30, LocalDateTime.now()),
                new UserResponse(2L, "Jane Doe", "jane@example.com", 25, LocalDateTime.now())
        );

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("Jane Doe"));

        verify(userService).getAllUsers();
    }

    @Test
    void shouldUpdateUser() throws Exception {
        UserRequest userRequest = new UserRequest("John Updated", "john.updated@example.com", 35);
        UserResponse userResponse = new UserResponse(1L, "John Updated", "john.updated@example.com", 35, LocalDateTime.now());

        when(userService.updateUser(eq(1L), any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "name": "John Updated",
                        "email": "john.updated@example.com",
                        "age": 35
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));

        verify(userService).updateUser(eq(1L), any(UserRequest.class));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void shouldHandleUserNotFound() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new RuntimeException("User not found with id: 999"));

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found with id: 999"));

        verify(userService).getUserById(999L);
    }

    @Test
    void shouldHandleEmailAlreadyExists() throws Exception {
        when(userService.createUser(any(UserRequest.class)))
                .thenThrow(new RuntimeException("Email already exists: john@example.com"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "name": "John Doe",
                        "email": "john@example.com",
                        "age": 30
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists: john@example.com"));

        verify(userService).createUser(any(UserRequest.class));
    }

    @Test
    void shouldValidateUserRequest() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "name": "",
                        "email": "invalid-email",
                        "age": -1
                    }
                    """))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(UserRequest.class));
    }

    @Test
    void shouldGetUserCount() throws Exception {
        when(userService.getUserCount()).thenReturn(5L);

        mockMvc.perform(get("/api/users/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(userService).getUserCount();
    }
}