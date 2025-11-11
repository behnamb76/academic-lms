package ir.bahman.academic_lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.academic_lms.dto.LoginRequest;
import ir.bahman.academic_lms.dto.MajorDTO;
import ir.bahman.academic_lms.model.Major;
import ir.bahman.academic_lms.repository.MajorRepository;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MajorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MajorRepository majorRepository;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        token = loginAndGetToken();
    }

    @AfterEach
    void tearDown() {
        majorRepository.deleteAll();
    }

    @Test
    void testCreateMajor() throws Exception {
        MajorDTO dto = MajorDTO.builder()
                .name("Physics").build();

        String response = mockMvc.perform(post("/api/major")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Physics"))
                .andReturn().getResponse().getContentAsString();

        MajorDTO responseDto = objectMapper.readValue(response, MajorDTO.class);
        Major saved = majorRepository.findByName(responseDto.getName())
                .orElseThrow(() -> new  EntityNotFoundException("Major not found"));
        assertThat(saved.getName()).isEqualTo("Physics");
        assertThat(saved.getMajorCode()).isNotNull();
        assertThat(saved.isDeleted()).isFalse();
    }

    @Test
    void testCreateMajor_shouldReject_duplicateMajorName() throws Exception {
        Major existing = Major.builder()
                .name("Physics")
                .majorCode(UUID.randomUUID()).build();
        Major saved = majorRepository.save(existing);

        MajorDTO dto = MajorDTO.builder()
                .name("Physics").build();

        mockMvc.perform(post("/api/major")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This major already exists!"));
    }

    @Test
    void testCreateMajor_shouldReject_blankMajorName() throws Exception {
        MajorDTO dto = MajorDTO.builder()
                .name("").build();

        mockMvc.perform(post("/api/major")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateMajor() throws Exception {
        Major original = Major.builder()
                .name("Old Name")
                .majorCode(UUID.randomUUID()).build();
        Major saved = majorRepository.save(original);

        MajorDTO updateDto = MajorDTO.builder()
                .name("New Name").build();

        String response = mockMvc.perform(put("/api/major/" + saved.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andReturn().getResponse().getContentAsString();

        MajorDTO responseDto = objectMapper.readValue(response, MajorDTO.class);
        Major updated = majorRepository.findByName(responseDto.getName())
                .orElseThrow(() -> new  EntityNotFoundException("Major not found"));
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getMajorCode()).isNotNull();
    }

    @Test
    void testUpdateMajor_shouldRejectUpdate_existingMajorName() throws Exception {
        Major major1 = Major.builder()
                .name("Physics")
                .majorCode(UUID.randomUUID()).build();
        majorRepository.save(major1);

        Major major2 = Major.builder()
                .name("Chemistry")
                .majorCode(UUID.randomUUID()).build();
        Major saved2 = majorRepository.save(major2);

        MajorDTO dto = MajorDTO.builder()
                .name("Physics").build();

        mockMvc.perform(put("/api/major/" + saved2.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This major already exists!"));
    }

    @Test
    void testUpdateMajor_shouldReturn404_majorNotFound() throws Exception {
        MajorDTO dto = MajorDTO.builder()
                .name("Physics").build();

        mockMvc.perform(put("/api/major/999999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteMajor() throws Exception {
        Major major = Major.builder()
                .name("Physics")
                .majorCode(UUID.randomUUID()).build();
        Major saved = majorRepository.save(major);

        mockMvc.perform(delete("/api/major/" + saved.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Major deletedMajor = majorRepository.findById(saved.getId()).orElseThrow();
        assertThat(deletedMajor.isDeleted()).isTrue();
        assertThat(deletedMajor.getName()).isEqualTo("Physics");
    }

    @Test
    void testDeleteMajor_shouldReturn404_majorNotFound() throws Exception {
        mockMvc.perform(delete("/api/major/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetMajorById() throws Exception {
        Major major = Major.builder()
                .name("Physics")
                .majorCode(UUID.randomUUID()).build();
        Major saved = majorRepository.save(major);

        String response = mockMvc.perform(get("/api/major/" + saved.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Physics"))
                .andReturn().getResponse().getContentAsString();

        MajorDTO dto = objectMapper.readValue(response, MajorDTO.class);
        assertThat(dto.getName()).isEqualTo("Physics");
    }

    @Test
    void testGetMajorById_shouldReturn404_softDeletedMajor() throws Exception {
        Major major = Major.builder()
                .name("Physics")
                .majorCode(UUID.randomUUID())
                .deleted(true).build();
        Major saved = majorRepository.save(major);

        mockMvc.perform(get("/api/major/" + saved.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetMajorById_shouldReturn404_nonExistentMajor() throws Exception {
        mockMvc.perform(get("/api/major/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllMajors() throws Exception {
        Major active = Major.builder()
                .name("Mathematics")
                .majorCode(UUID.randomUUID()).build();
        majorRepository.save(active);

        Major deleted = Major.builder()
                .name("Physics")
                .majorCode(UUID.randomUUID())
                .deleted(true).build();
        majorRepository.save(deleted);

        String response = mockMvc.perform(get("/api/major")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<MajorDTO> majors = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, MajorDTO.class));

        assertThat(majors).hasSize(2);
        assertThat(majors).extracting(MajorDTO::getName)
                .containsExactlyInAnyOrder("Computer", "Mathematics");
    }

    @Test
    void testGetAllMajors_shouldReturnEmpty_noActiveMajors() throws Exception {
        Major major = Major.builder()
                .name("Biology")
                .majorCode(UUID.randomUUID())
                .deleted(true).build();
        majorRepository.save(major);

        Major major1 = majorRepository.findByName("Computer").orElseThrow();
        major1.setDeleted(true);
        majorRepository.save(major1);

        String response = mockMvc.perform(get("/api/major")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<MajorDTO> majors = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, MajorDTO.class));

        assertThat(majors).isEmpty();
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