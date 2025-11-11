package ir.bahman.academic_lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.academic_lms.dto.LoginRequest;
import ir.bahman.academic_lms.dto.RefreshRequest;
import ir.bahman.academic_lms.dto.RegisterRequest;
import ir.bahman.academic_lms.model.Account;
import ir.bahman.academic_lms.model.Person;
import ir.bahman.academic_lms.model.enums.AccountStatus;
import ir.bahman.academic_lms.repository.AccountRepository;
import ir.bahman.academic_lms.repository.MajorRepository;
import ir.bahman.academic_lms.repository.PersonRepository;
import ir.bahman.academic_lms.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MajorRepository majorRepository;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testLogin() throws Exception {
        createRegisterRequest();

        Person person = personRepository.findByNationalCode("1234567890")
                .orElseThrow(() -> new EntityNotFoundException("Person not found"));
        Account account = accountRepository.findById(person.getAccount().getId()).orElseThrow();
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        LoginRequest loginReq = LoginRequest.builder()
                .username("ali_teacher")
                .password("mySecretPass123").build();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        Map<String, String> tokens = objectMapper.readValue(loginResponse, Map.class);
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        assertThat(accessToken).isNotNull();
        assertThat(refreshToken).isNotNull();

        RefreshRequest refreshReq = RefreshRequest.builder()
                .refreshToken(refreshToken).build();

        String refreshResponse = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        Map<String, String> newTokens = objectMapper.readValue(refreshResponse, Map.class);
        String newAccessToken = newTokens.get("accessToken");
        String newRefreshToken = newTokens.get("refreshToken");

        assertThat(newAccessToken).isNotNull().isNotEqualTo(accessToken);
        assertThat(newRefreshToken).isNotNull().isNotEqualTo(refreshToken);
    }

    @Test
    void refresh() {
    }

    @Test
    void logout() {
    }

    private void createRegisterRequest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Kazemi")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .majorName("Computer")
                .username("ali_teacher")
                .password("mySecretPass123").build();

        mockMvc.perform(post("/api/person/teacher-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }
}