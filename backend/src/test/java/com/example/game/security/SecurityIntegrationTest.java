package com.example.game.security;

import com.example.game.dto.request.LoginUserRequest;
import com.example.game.dto.response.GameStateResponse;
import com.example.game.dto.response.LoginResponse;
import com.example.game.entity.User;
import com.example.game.enums.GameStatus;
import com.example.game.repository.UserRepo;
import com.example.game.service.AuthService;
import com.example.game.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private UserRepo userRepo;

    @Test
    void loginEndpointIsPublic() throws Exception {
        when(authService.authenticate(new LoginUserRequest("gabe", "password123")))
                .thenReturn(new LoginResponse("jwt-token", 3600000L));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginUserRequest("gabe", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void apiDocsEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/game/findGame"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpointRejectsInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/game/findGame")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointUsesJwtPrincipalUsername() throws Exception {
        User user = new User();
        user.setId(7L);
        user.setUsername("gabe");
        user.setPasswordHash("$2a$10$abcdefghijklmnopqrstuv");

        String token = jwtService.generateToken(user);
        when(userRepo.findByUsername("gabe")).thenReturn(Optional.of(user));
        when(gameService.getGame("gabe")).thenReturn(new GameStateResponse(
                11L,
                100,
                500,
                0,
                50,
                100,
                0,
                "San Jose",
                1,
                GameStatus.IN_PROGRESS,
                null
        ));

        mockMvc.perform(get("/game/findGame")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(11))
                .andExpect(jsonPath("$.locationName").value("San Jose"));

        verify(gameService).getGame("gabe");
    }
}
