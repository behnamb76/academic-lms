package ir.bahman.academic_lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.academic_lms.dto.AcademicCalenderDTO;
import ir.bahman.academic_lms.dto.LoginRequest;
import ir.bahman.academic_lms.dto.TermDTO;
import ir.bahman.academic_lms.model.AcademicCalender;
import ir.bahman.academic_lms.model.Term;
import ir.bahman.academic_lms.repository.AcademicCalenderRepository;
import ir.bahman.academic_lms.repository.MajorRepository;
import ir.bahman.academic_lms.repository.TermRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TermControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private AcademicCalenderRepository academicCalenderRepository;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void testCreateTerm() throws Exception {
        String token = loginAndGetToken();

        LocalDate now = LocalDate.now();
        TermDTO dto = TermDTO.builder()
                .majorName("Computer")
                .registrationStart(now.plusDays(1))
                .registrationEnd(now.plusDays(10))
                .classesStartDate(now.plusDays(11))
                .classesEndDate(now.plusDays(100)).build();

        String response = mockMvc.perform(post("/api/term")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.majorName").value("Computer"))
                .andExpect(jsonPath("$.registrationStart").value(dto.getRegistrationStart().toString()))
                .andExpect(jsonPath("$.classesEndDate").value(dto.getClassesEndDate().toString()))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void testCreateTerm_shouldReject_emptyMajorName() throws Exception {
        String token = loginAndGetToken();

        LocalDate now = LocalDate.now();
        TermDTO dto = TermDTO.builder()
                .majorName("")
                .registrationStart(now.plusDays(1))
                .registrationEnd(now.plusDays(10))
                .classesStartDate(now.plusDays(11))
                .classesEndDate(now.plusDays(100)).build();

        mockMvc.perform(post("/api/term")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field == 'majorName')]").exists());
    }

    @Test
    void testCreateTerm_shouldReject_RegistrationEndIsBeforeStart() throws Exception {
        String token = loginAndGetToken();

        LocalDate now = LocalDate.now();
        TermDTO dto = TermDTO.builder()
                .majorName("Computer")
                .registrationStart(now.plusDays(10))
                .registrationEnd(now.plusDays(5))
                .classesStartDate(now.plusDays(11))
                .classesEndDate(now.plusDays(20)).build();

        mockMvc.perform(post("/api/term")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.message == 'Course registration end date must be on or after start date')]").exists());
    }

    @Test
    void testUpdateTerm() throws Exception {
        String token = loginAndGetToken();

        LocalDate now = LocalDate.now();
        AcademicCalender calender = AcademicCalender.builder()
                .registrationStart(now.plusDays(1))
                .registrationEnd(now.plusDays(10))
                .classesStartDate(now.plusDays(11))
                .classesEndDate(now.plusDays(100)).build();
        academicCalenderRepository.save(calender);

        Term term = Term.builder()
                .major(majorRepository.findByName("Computer").orElseThrow(() -> new EntityNotFoundException("Major not found")))
                .year(now.getYear())
                .academicCalender(calender).build();
        Term savedTerm = termRepository.save(term);

        TermDTO updateDto = TermDTO.builder()
                .majorName("Computer")
                .registrationStart(now.plusDays(5))
                .registrationEnd(now.plusDays(15))
                .classesStartDate(now.plusDays(16))
                .classesEndDate(now.plusDays(120)).build();

        mockMvc.perform(put("/api/term/" + savedTerm.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationStart").value(updateDto.getRegistrationStart().toString()))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void testUpdateTerm_shouldReturn404() throws Exception {
        String token = loginAndGetToken();

        LocalDate now = LocalDate.now();
        TermDTO dto = TermDTO.builder()
                .majorName("Computer")
                .registrationStart(now.plusDays(1))
                .registrationEnd(now.plusDays(10))
                .classesStartDate(now.plusDays(11))
                .classesEndDate(now.plusDays(100)).build();

        mockMvc.perform(put("/api/term/999999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateTerm_shouldReject_InvalidUpdate() throws Exception {
        String token = loginAndGetToken();

        LocalDate now = LocalDate.now();
        AcademicCalender calender = AcademicCalender.builder()
                .registrationStart(now.plusDays(1))
                .registrationEnd(now.plusDays(10))
                .classesStartDate(now.plusDays(11))
                .classesEndDate(now.plusDays(20)).build();
        academicCalenderRepository.save(calender);

        Term term = Term.builder()
                .major(majorRepository.findByName("Computer").orElseThrow(() -> new EntityNotFoundException("Major not found")))
                .year(now.getYear())
                .academicCalender(calender).build();
        Term savedTerm = termRepository.save(term);

        TermDTO invalidDto = TermDTO.builder()
                .majorName("Computer")
                .registrationStart(now.plusDays(10))
                .registrationEnd(now.plusDays(5))
                .classesStartDate(now.plusDays(11))
                .classesEndDate(now.plusDays(20)).build();

        mockMvc.perform(put("/api/term/" + savedTerm.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void testDeleteTerm() throws Exception {
        String token = loginAndGetToken();

        LocalDate now = LocalDate.now();
        AcademicCalender calender = AcademicCalender.builder()
                .registrationStart(now.plusDays(1))
                .registrationEnd(now.plusDays(10))
                .classesStartDate(now.plusDays(11))
                .classesEndDate(now.plusDays(100)).build();
        academicCalenderRepository.save(calender);

        Term term = Term.builder()
                .major(majorRepository.findByName("Computer")
                        .orElseThrow(() -> new EntityNotFoundException("Major not found")))
                .year(now.getYear())
                .academicCalender(calender).build();
        Term savedTerm = termRepository.save(term);

        mockMvc.perform(delete("/api/term/" + savedTerm.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        assertThat(termRepository.findById(savedTerm.getId()).get().isDeleted()).isTrue();
    }

    @Test
    void testDeleteTerm_shouldReturn404_termNotFound() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(delete("/api/term/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteTerm_shouldReturn403_notAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/term/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetTermCalender() throws Exception {
        String token = loginAndGetToken();

        LocalDate now = LocalDate.now();
        AcademicCalender calender = AcademicCalender.builder()
                .registrationStart(now.plusDays(1))
                .registrationEnd(now.plusDays(10))
                .classesStartDate(now.plusDays(11))
                .classesEndDate(now.plusDays(100)).build();
        AcademicCalender savedCalender = academicCalenderRepository.save(calender);

        Term term = Term.builder()
                .major(majorRepository.findByName("Computer")
                        .orElseThrow(() -> new EntityNotFoundException("Major not found")))
                .year(now.getYear())
                .academicCalender(calender).build();
        Term savedTerm = termRepository.save(term);

        String response = mockMvc.perform(get("/api/term/term-calender/" + savedTerm.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationStart").value(calender.getRegistrationStart().toString()))
                .andExpect(jsonPath("$.classesEndDate").value(calender.getClassesEndDate().toString()))
                .andReturn().getResponse().getContentAsString();

        AcademicCalenderDTO responseCal = objectMapper.readValue(response, AcademicCalenderDTO.class);
        assertThat(responseCal.getClassesStartDate()).isEqualTo(calender.getClassesStartDate());
    }

    @Test
    void testGetTermCalender_shouldReturn404_termNotFound() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/term/term-calender/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
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