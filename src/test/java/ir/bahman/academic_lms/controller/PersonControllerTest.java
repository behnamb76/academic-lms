package ir.bahman.academic_lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.academic_lms.dto.AssignRoleRequest;
import ir.bahman.academic_lms.dto.LoginRequest;
import ir.bahman.academic_lms.dto.PersonDTO;
import ir.bahman.academic_lms.dto.RegisterRequest;
import ir.bahman.academic_lms.model.Account;
import ir.bahman.academic_lms.model.Person;
import ir.bahman.academic_lms.model.Role;
import ir.bahman.academic_lms.repository.AccountRepository;
import ir.bahman.academic_lms.repository.PersonRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PersonControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        personRepository.findAll().stream()
                .filter(person -> !person.getId().equals(1L))
                .forEach(person -> personRepository.deleteById(person.getId()));
        accountRepository.findAll().stream()
                .filter(account -> !account.getId().equals(1L))
                .forEach(account -> personRepository.deleteById(account.getId()));
    }

    @Test
    void testStudentRegister() throws Exception {

        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        String response = mockMvc.perform(post("/api/person/student-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Ali"))
                .andExpect(jsonPath("$.nationalCode").value("2234567890"))
                .andReturn().getResponse().getContentAsString();

        PersonDTO responseDto = objectMapper.readValue(response, PersonDTO.class);

        Person savedPerson = personRepository.findByNationalCode((responseDto.getNationalCode())).orElseThrow();
        assertThat(savedPerson.getNationalCode()).isEqualTo("2234567890");
        assertThat(savedPerson.getPhoneNumber()).isEqualTo("09223456789");

        Account savedAccount = savedPerson.getAccount();
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getUsername()).isEqualTo("ali_student");
        assertThat(passwordEncoder.matches("mySecretPass123", savedAccount.getPassword())).isTrue();
        assertThat(savedAccount.getStatus().name()).isEqualTo("PENDING");

        List<String> roleNames = savedPerson.getRoles().stream()
                .map(Role::getName)
                .toList();

        assertThat(roleNames).containsExactlyInAnyOrder("USER", "STUDENT");
    }

    @Test
    void testStudentRegister_shouldRejectDuplicate() throws Exception {
        RegisterRequest firstRequest = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        mockMvc.perform(post("/api/person/student-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        RegisterRequest secondRequest = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        mockMvc.perform(post("/api/person/student-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This person already exists!"));
    }

    @Test
    void testTeacherRegister() throws Exception {

        RegisterRequest request = registerPerson();

        token = loginAndGetToken();

        String response = mockMvc.perform(post("/api/person/teacher-register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Ali"))
                .andExpect(jsonPath("$.nationalCode").value("1234567890"))
                .andReturn().getResponse().getContentAsString();

        PersonDTO responseDto = objectMapper.readValue(response, PersonDTO.class);

        Person savedPerson = personRepository.findByNationalCode((responseDto.getNationalCode())).orElseThrow();
        assertThat(savedPerson.getNationalCode()).isEqualTo("1234567890");
        assertThat(savedPerson.getPhoneNumber()).isEqualTo("09123456789");

        Account savedAccount = savedPerson.getAccount();
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getUsername()).isEqualTo("ali_teacher");
        assertThat(passwordEncoder.matches("mySecretPass123", savedAccount.getPassword())).isTrue();
        assertThat(savedAccount.getStatus().name()).isEqualTo("PENDING");

        List<String> roleNames = savedPerson.getRoles().stream()
                .map(Role::getName)
                .toList();

        assertThat(roleNames).containsExactlyInAnyOrder("USER", "TEACHER");
    }

    @Test
    void testTeacherRegister_shouldRejectDuplicate() throws Exception {
        RegisterRequest firstRequest = registerPerson();

        token = loginAndGetToken();

        mockMvc.perform(post("/api/person/teacher-register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        RegisterRequest secondRequest = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Kazemi")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .majorName("Computer")
                .username("ali_teacher")
                .password("mySecretPass123").build();

        mockMvc.perform(post("/api/person/teacher-register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This person already exists!"));
    }

    @Test
    void testAssignRoleToPerson() throws Exception {
        RegisterRequest request = registerPerson();

        token = loginAndGetToken();

        String registerResponse = mockMvc.perform(post("/api/person/teacher-register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PersonDTO registeredPerson = objectMapper.readValue(registerResponse, PersonDTO.class);

        Person person = personRepository.findByNationalCode(registeredPerson.getNationalCode()).orElseThrow();

        AssignRoleRequest assignRequest = AssignRoleRequest.builder()
                .role("ADMIN")
                .personId(person.getId()).build();

        mockMvc.perform(post("/api/person/add-role")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk());

        Person personWithRoles = personRepository.findByNationalCode(registeredPerson.getNationalCode()).orElseThrow();
        List<String> roleNames = personWithRoles.getRoles().stream()
                .map(Role::getName)
                .toList();

        assertThat(roleNames).containsExactlyInAnyOrder("USER", "TEACHER", "ADMIN");
    }

    @Test
    void testUpdateProfile() throws Exception {
        RegisterRequest request = registerPerson();

        token = loginAndGetToken();

        mockMvc.perform(post("/api/person/teacher-register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Person originalPerson = personRepository.findByNationalCode("1234567890").orElseThrow();

        PersonDTO updateDto = PersonDTO.builder()
                .firstName("NewName")
                .lastName("NewSurname")
                .nationalCode("1122334455")
                .phoneNumber("09121112233")
                .majorName("Computer").build();

        mockMvc.perform(put("/api/person/" + originalPerson.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        Person updatedPerson = personRepository.findById(originalPerson.getId()).orElseThrow();
        assertThat(updatedPerson.getFirstName()).isEqualTo("NewName");
        assertThat(updatedPerson.getLastName()).isEqualTo("NewSurname");
        assertThat(updatedPerson.getNationalCode()).isEqualTo("1122334455");
        assertThat(updatedPerson.getPhoneNumber()).isEqualTo("09121112233");
    }

    @Test
    void testUpdateProfile_shouldReturn404() throws Exception {
        token = loginAndGetToken();

        PersonDTO dto = PersonDTO.builder()
                .firstName("Test")
                .lastName("User")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .majorName("Computer").build();

        mockMvc.perform(put("/api/person/999999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person not found!"));
    }

    @Test
    void testSearchPeople() throws Exception {
        String token = loginAndGetToken();

        RegisterRequest request = registerPerson();

        mockMvc.perform(post("/api/person/teacher-register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        RegisterRequest request1 = RegisterRequest.builder()
                .firstName("Sara")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("sara_a")
                .password("mySecretPass123").build();

        mockMvc.perform(post("/api/person/teacher-register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String response = mockMvc.perform(get("/api/person/search/Ali")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<PersonDTO> results = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, PersonDTO.class));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("Ali");

        mockMvc.perform(get("/api/person/search/sara_a")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Sara"));
    }

    private RegisterRequest registerPerson(){
        return RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Kazemi")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .majorName("Computer")
                .username("ali_teacher")
                .password("mySecretPass123").build();
    }

    private String loginAndGetToken() throws Exception {
        LoginRequest loginReq = LoginRequest.builder()
                .username("admin")
                .password("admin").build();

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString())
                .get("accessToken").asText();
    }
}