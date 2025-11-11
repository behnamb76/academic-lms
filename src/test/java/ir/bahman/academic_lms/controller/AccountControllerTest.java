package ir.bahman.academic_lms.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.academic_lms.dto.ChangePasswordRequest;
import ir.bahman.academic_lms.dto.ChangeRoleRequest;
import ir.bahman.academic_lms.dto.RegisterRequest;
import ir.bahman.academic_lms.model.Account;
import ir.bahman.academic_lms.model.Person;
import ir.bahman.academic_lms.model.Role;
import ir.bahman.academic_lms.model.enums.AccountStatus;
import ir.bahman.academic_lms.repository.AccountRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerTest {
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
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        personRepository.deleteAll();
    }

    @Test
    void testActivateAccount() throws Exception {
        createRegisterRequest();

        Person person = personRepository.findByNationalCode("1234567890")
                .orElseThrow();
        Long accountId = person.getAccount().getId();

        Account accountBefore = accountRepository.findById(accountId).orElseThrow();
        assertThat(accountBefore.getStatus()).isEqualTo(AccountStatus.PENDING);

        mockMvc.perform(put("/api/account/activate/" + accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Account accountAfter = accountRepository.findById(accountId).orElseThrow();
        assertThat(accountAfter.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void testActivateAccount_shouldReturn404() throws Exception {
        mockMvc.perform(put("/api/account/activate/999999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeactivateAccount() throws Exception {
        createRegisterRequest();

        Person person = personRepository.findByNationalCode("1234567890")
                .orElseThrow();
        Long accountId = person.getAccount().getId();

        mockMvc.perform(put("/api/account/deactivate/" + accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Account account = accountRepository.findById(accountId).orElseThrow();
        assertThat(account.getStatus()).isEqualTo(AccountStatus.INACTIVE);
    }

    @Test
    void testDeactivateAccount_shouldReturn404() throws Exception {
        mockMvc.perform(put("/api/account/deactivate/999999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testChangeRole() throws Exception {
        createRegisterRequest();

        Role adminRole = roleRepository.findByName("TEACHER")
                .orElseThrow(() -> new EntityNotFoundException("Role not found!"));

        Person person = personRepository.findByNationalCode("1234567890")
                .orElseThrow();
        person.getRoles().add(adminRole);
        personRepository.save(person);

        Account accountBefore = accountRepository.findByUsername("ali_teacher").orElseThrow();
        assertThat(accountBefore.getActiveRole().getName()).isEqualTo("USER");

        ChangeRoleRequest changeRequest = ChangeRoleRequest.builder()
                .role("TEACHER").build();

        mockMvc.perform(put("/api/account/change-role")
                        .with(user("ali_teacher"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest)))
                .andExpect(status().isOk());

        Account accountAfter = accountRepository.findByUsername("ali_teacher").orElseThrow();
        assertThat(accountAfter.getActiveRole().getName()).isEqualTo("TEACHER");
    }

    @Test
    void testChangePassword() throws Exception {
        createRegisterRequest();

        Account accountBefore = accountRepository.findByUsername("ali_teacher").orElseThrow();
        assertThat(passwordEncoder.matches("mySecretPass123", accountBefore.getPassword())).isTrue();

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("mySecretPass123")
                .newPassword("newMySecretPass123").build();

        mockMvc.perform(put("/api/account/change-password")
                        .with(user("ali_teacher"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Account accountAfter = accountRepository.findByUsername("ali_teacher").orElseThrow();
        assertThat(passwordEncoder.matches("newMySecretPass123", accountAfter.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("mySecretPass123", accountAfter.getPassword())).isFalse();
    }

    @Test
    void testChangePassword_shouldReturn401() throws Exception {
        createRegisterRequest();

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("WrongPassword")
                .newPassword("NewPass123").build();

        mockMvc.perform(put("/api/account/change-password")
                        .with(user("ali_teacher"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Current password is incorrect!"));
    }

    @Test
    void testChangePassword_shouldReturn404() throws Exception {

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("anyPassword")
                .newPassword("NewPass123").build();

        mockMvc.perform(put("/api/account/change-password")
                        .with(user("ali_teacher"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
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