package io.agx.bookmyseat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.agx.bookmyseat.dto.request.CreateUserRequest;
import io.agx.bookmyseat.dto.response.UserResponse;
import io.agx.bookmyseat.entity.User;
import io.agx.bookmyseat.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// 1. NEW SPRING BOOT 4 IMPORT FOR WebMvcTest
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

// 2. NEW SPRING BOOT 4 IMPORT FOR MockitoBean (Replaces @MockBean)
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private UserService userService;

    private UUID userId;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        userResponse = UserResponse.builder()
                .id(userId)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();
    }

    @Test
    void createUser_shouldReturnCreatedStatusAndUserResponse() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("SecurePass123!")
                .build();

        Mockito.when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(userResponse);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void getUserById_shouldReturnOkStatusAndUserResponse() throws Exception {
        Mockito.when(userService.getUserById(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/{id}", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void getUserByEmail_shouldReturnOkStatusAndUserResponse() throws Exception {
        String email = "john.doe@example.com";

        User userEntity = User.builder()
                .id(userId)
                .name("John Doe")
                .email(email)
                .build();

        Mockito.when(userService.getUserByEmail(email)).thenReturn(userEntity);

        mockMvc.perform(get("/api/users")
                        .param("email", email)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void createUser_shouldReturnBadRequest_whenValidationFails() throws Exception {
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                .name("")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        Mockito.verify(userService, Mockito.never()).createUser(any());
    }
}