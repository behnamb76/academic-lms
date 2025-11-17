package ir.bahman.academic_lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.academic_lms.dto.CourseDTO;
import ir.bahman.academic_lms.dto.LoginRequest;
import ir.bahman.academic_lms.model.Course;
import ir.bahman.academic_lms.model.Major;
import ir.bahman.academic_lms.repository.CourseRepository;
import ir.bahman.academic_lms.repository.MajorRepository;
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
@Transactional
class CourseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private MajorRepository majorRepository;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        token = loginAndGetToken();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testCreateCourse() throws Exception {
        Major major = Major.builder()
                .name("Computer Science")
                .majorCode(UUID.randomUUID()).build();
        Major savedMajor = majorRepository.save(major);

        CourseDTO dto = CourseDTO.builder()
                .title("Advanced Java")
                .unit(3)
                .description("Advanced Java programming concepts")
                .majorName("Computer Science").build();

        String response = mockMvc.perform(post("/api/course")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Advanced Java"))
                .andExpect(jsonPath("$.majorName").value("Computer Science"))
                .andExpect(jsonPath("$.unit").value(3))
                .andReturn().getResponse().getContentAsString();

        CourseDTO responseDto = objectMapper.readValue(response, CourseDTO.class);
        Course savedCourse = courseRepository.findByTitle(responseDto.getTitle()).orElseThrow();
        assertThat(savedCourse.getTitle()).isEqualTo("Advanced Java");
        assertThat(savedCourse.getMajor().getId()).isEqualTo(savedMajor.getId());
        assertThat(savedCourse.isDeleted()).isFalse();
    }

    @Test
    void testCreateCourse_shouldReject_duplicateCourse() throws Exception {
        Major major = Major.builder()
                .name("Mathematics")
                .majorCode(UUID.randomUUID()).build();
        Major savedMajor = majorRepository.save(major);

        Course existing = Course.builder()
                .title("Calculus I")
                .unit(4)
                .major(savedMajor).build();
        courseRepository.save(existing);

        CourseDTO dto = CourseDTO.builder()
                .title("Calculus I")
                .unit(4)
                .majorName("Mathematics").build();

        mockMvc.perform(post("/api/course")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This course already exists in this major!"));
    }

    @Test
    void testCreateCourse_shouldReject_majorNotFound() throws Exception {
        CourseDTO dto = CourseDTO.builder()
                .title("Physics")
                .unit(3)
                .majorName("NonExistentMajor").build();

        mockMvc.perform(post("/api/course")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Major with name NonExistentMajor not found"));
    }

    @Test
    void testCreateCourse_shouldReject_majorIsDeleted() throws Exception {
        Major major = Major.builder()
                .name("Deleted Major")
                .majorCode(UUID.randomUUID())
                .deleted(true).build();
        majorRepository.save(major);

        CourseDTO dto = CourseDTO.builder()
                .title("Course for Deleted Major")
                .unit(3)
                .majorName("Deleted Major").build();

        mockMvc.perform(post("/api/course")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateCourse_shouldReject_shortTitle() throws Exception {
        Major major = Major.builder()
                .name("Physics")
                .majorCode(UUID.randomUUID()).build();
        majorRepository.save(major);

        CourseDTO dto = CourseDTO.builder()
                .title("AB")
                .unit(3)
                .majorName("Physics").build();

        mockMvc.perform(post("/api/course")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCourse_shouldReject_invalidUnit() throws Exception {
        Major major = Major.builder()
                .name("Physics")
                .majorCode(UUID.randomUUID()).build();
        majorRepository.save(major);

        CourseDTO dto = CourseDTO.builder()
                .title("Physics I")
                .unit(0)
                .majorName("Physics").build();

        mockMvc.perform(post("/api/course")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateCourse() throws Exception {
        Major major = Major.builder()
                .name("Computer Science")
                .majorCode(UUID.randomUUID()).build();
        Major savedMajor = majorRepository.save(major);

        Course course = Course.builder()
                .title("Old Title")
                .unit(3)
                .major(savedMajor)
                .description("Old Description").build();
        Course savedCourse = courseRepository.save(course);

        CourseDTO updateDto = CourseDTO.builder()
                .title("New Title")
                .unit(4)
                .majorName("Computer Science")
                .description("New Description").build();

        String response = mockMvc.perform(put("/api/course/" + savedCourse.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.unit").value(4))
                .andReturn().getResponse().getContentAsString();

        CourseDTO responseDto = objectMapper.readValue(response, CourseDTO.class);
        Course updatedCourse = courseRepository.findByTitle(responseDto.getTitle()).orElseThrow();
        assertThat(updatedCourse.getTitle()).isEqualTo("New Title");
        assertThat(updatedCourse.getUnit()).isEqualTo(4);
        assertThat(updatedCourse.getDescription()).isEqualTo("New Description");
        assertThat(updatedCourse.getMajor().getId()).isEqualTo(savedMajor.getId());
    }

    @Test
    void testUpdateCourse_shouldReject_softDeletedCourse() throws Exception {
        Major major = Major.builder()
                .name("Mathematics")
                .majorCode(UUID.randomUUID()).build();
        Major savedMajor = majorRepository.save(major);

        Course course = Course.builder()
                .title("Deleted Course")
                .unit(3)
                .major(savedMajor)
                .deleted(true).build();
        Course savedCourse = courseRepository.save(course);

        CourseDTO dto = CourseDTO.builder()
                .title("Attempted Update")
                .unit(4)
                .majorName("Mathematics").build();

        mockMvc.perform(put("/api/course/" + savedCourse.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateCourse_shouldReturn404_courseNotFound() throws Exception {
        CourseDTO dto = CourseDTO.builder()
                .title("NonExistent")
                .unit(3)
                .majorName("Any Major").build();

        mockMvc.perform(put("/api/course/999999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateCourse_shouldReject_invalidData() throws Exception {
        Major major = Major.builder()
                .name("Physics")
                .majorCode(UUID.randomUUID()).build();
        Major savedMajor = majorRepository.save(major);

        Course course = Course.builder()
                .title("Valid Course")
                .unit(3)
                .major(savedMajor).build();
        Course saved = courseRepository.save(course);

        CourseDTO invalidDto = CourseDTO.builder()
                .title("AB")
                .unit(0)
                .majorName("Physics").build();

        mockMvc.perform(put("/api/course/" + saved.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteCourse() throws Exception {
        Major major = Major.builder()
                .name("Computer Science")
                .majorCode(UUID.randomUUID()).build();
        Major savedMajor = majorRepository.save(major);

        Course course = Course.builder()
                .title("Java Programming")
                .unit(3)
                .major(savedMajor).build();
        Course savedCourse = courseRepository.save(course);

        mockMvc.perform(delete("/api/course/" + savedCourse.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Course deletedCourse = courseRepository.findById(savedCourse.getId()).orElseThrow();
        assertThat(deletedCourse.isDeleted()).isTrue();
        assertThat(deletedCourse.getTitle()).isEqualTo("Java Programming");
        assertThat(deletedCourse.getMajor().getId()).isEqualTo(savedMajor.getId());
    }

    @Test
    void testDeleteCourse_shouldReturn404_courseNotFound() throws Exception {
        mockMvc.perform(delete("/api/course/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteCourse_shouldAllow_AlreadyDeletedCourse() throws Exception {
        Major major = Major.builder()
                .name("Mathematics")
                .majorCode(UUID.randomUUID()).build();
        Major savedMajor = majorRepository.save(major);

        Course course = Course.builder()
                .title("Calculus")
                .unit(4)
                .major(savedMajor)
                .deleted(true).build();
        Course savedCourse = courseRepository.save(course);

        mockMvc.perform(delete("/api/course/" + savedCourse.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Course reDeleted = courseRepository.findById(savedCourse.getId()).orElseThrow();
        assertThat(reDeleted.isDeleted()).isTrue();
    }

    @Test
    void testGetCourseById() throws Exception {
        Major major = Major.builder()
                .name("Computer Science")
                .majorCode(UUID.randomUUID()).build();
        Major savedMajor = majorRepository.save(major);

        Course course = Course.builder()
                .title("Advanced Java")
                .unit(3)
                .major(savedMajor)
                .description("Advanced concepts in Java").build();
        Course savedCourse = courseRepository.save(course);

        String response = mockMvc.perform(get("/api/course/" + savedCourse.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Advanced Java"))
                .andExpect(jsonPath("$.unit").value(3))
                .andExpect(jsonPath("$.majorName").value("Computer Science"))
                .andExpect(jsonPath("$.description").value("Advanced concepts in Java"))
                .andReturn().getResponse().getContentAsString();

        CourseDTO responseDto = objectMapper.readValue(response, CourseDTO.class);
        assertThat(responseDto.getTitle()).isEqualTo("Advanced Java");
        assertThat(responseDto.getMajorName()).isEqualTo("Computer Science");
    }

    @Test
    void testGetCourseById_shouldReturn404_softDeletedCourse() throws Exception {
        Major major = Major.builder()
                .name("Mathematics")
                .majorCode(UUID.randomUUID()).build();
        Major savedMajor = majorRepository.save(major);

        Course course = Course.builder()
                .title("Deleted Course")
                .unit(4)
                .major(savedMajor)
                .deleted(true).build();
        Course savedDeleted = courseRepository.save(course);

        mockMvc.perform(get("/api/course/" + savedDeleted.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCourseById_shouldReturn404_courseDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/course/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllCourses() throws Exception {
        Major major = Major.builder()
                .name("Computer Science")
                .majorCode(UUID.randomUUID()).build();
        Major savedMajor = majorRepository.save(major);

        Major mathMajor = Major.builder()
                .name("Mathematics")
                .majorCode(UUID.randomUUID()).build();
        Major savedMathMajor = majorRepository.save(mathMajor);

        Course active1 = Course.builder()
                .title("Advanced Java")
                .unit(3)
                .major(savedMajor).build();
        courseRepository.save(active1);

        Course active2 = Course.builder()
                .title("Calculus I")
                .unit(4)
                .major(savedMathMajor).build();
        courseRepository.save(active2);

        Course deleted = Course.builder()
                .title("Deleted Course")
                .unit(2)
                .major(savedMajor)
                .deleted(true).build();
        courseRepository.save(deleted);

        String response = mockMvc.perform(get("/api/course")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CourseDTO> courses = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, CourseDTO.class));

        assertThat(courses).hasSize(2);
        assertThat(courses).extracting(CourseDTO::getTitle)
                .containsExactlyInAnyOrder("Advanced Java", "Calculus I");
        assertThat(courses).extracting(CourseDTO::getMajorName)
                .containsOnly("Computer Science", "Mathematics");
    }

    @Test
    void testGetAllCourses_shouldReturnEmptyList_noActiveCourses() throws Exception {
        Major major = Major.builder()
                .name("Physics")
                .majorCode(UUID.randomUUID()).build();
        Major savedMajor = majorRepository.save(major);

        Course active1 = Course.builder()
                .title("Deleted Physics")
                .unit(3)
                .major(savedMajor)
                .deleted(true).build();
        courseRepository.save(active1);

        String response = mockMvc.perform(get("/api/course")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CourseDTO> courses = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, CourseDTO.class));

        assertThat(courses).isEmpty();
    }

    @Test
    void testGetAllMajorCourses() throws Exception {
        Major major = Major.builder()
                .name("Computer Science")
                .majorCode(UUID.randomUUID()).build();
        Major savedMajor = majorRepository.save(major);

        Course active1 = Course.builder()
                .title("Advanced Java")
                .unit(3)
                .major(savedMajor).build();
        courseRepository.save(active1);

        Course active2 = Course.builder()
                .title("Data Structures")
                .unit(4)
                .major(savedMajor).build();
        courseRepository.save(active2);

        Course deleted = Course.builder()
                .title("Deleted Course")
                .unit(2)
                .major(savedMajor)
                .deleted(true).build();
        courseRepository.save(deleted);

        String response = mockMvc.perform(get("/api/course/major-courses?majorName=Computer Science")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CourseDTO> courses = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, CourseDTO.class));

        assertThat(courses).hasSize(2);
        assertThat(courses).extracting(CourseDTO::getTitle)
                .containsExactlyInAnyOrder("Advanced Java", "Data Structures");
    }

    @Test
    void testGetAllMajorCourses_shouldReturn404_majorNotFound() throws Exception {
        mockMvc.perform(get("/api/course/major-courses?majorName=NonExistentMajor")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllMajorCourses_shouldReturn404_majorIsDeleted() throws Exception {
        Major deletedMajor = Major.builder()
                .name("Deleted Major")
                .majorCode(UUID.randomUUID())
                .deleted(true).build();
        majorRepository.save(deletedMajor);

        mockMvc.perform(get("/api/course/major-courses?majorName=Deleted Major")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllMajorCourses_shouldReturnEmptyList_noCoursesInMajor() throws Exception {
        Major major = Major.builder()
                .name("Mathematics")
                .majorCode(UUID.randomUUID()).build();
        majorRepository.save(major);

        String response = mockMvc.perform(get("/api/course/major-courses?majorName=Mathematics")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CourseDTO> courses = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, CourseDTO.class));

        assertThat(courses).isEmpty();
    }

    @Test
    void testGetAllMajorCourses_shouldReturnOnlyCoursesFromSpecifiedMajor() throws Exception {
        Major csMajor = Major.builder()
                .name("Computer Science")
                .majorCode(UUID.randomUUID()).build();
        Major savedCs = majorRepository.save(csMajor);

        Major mathMajor = Major.builder()
                .name("Mathematics")
                .majorCode(UUID.randomUUID()).build();
        Major savedMath = majorRepository.save(mathMajor);

        Course csCourse = Course.builder()
                .title("Java Programming")
                .unit(3)
                .major(savedCs).build();
        courseRepository.save(csCourse);

        Course mathCourse = Course.builder()
                .title("Calculus I")
                .unit(4)
                .major(savedMath).build();
        courseRepository.save(mathCourse);

        String response = mockMvc.perform(get("/api/course/major-courses?majorName=Computer Science")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CourseDTO> courses = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, CourseDTO.class));

        assertThat(courses).hasSize(1);
        assertThat(courses.get(0).getTitle()).isEqualTo("Java Programming");
    }

    @Test
    void testGetAllMajorCourses_shouldReturn400_majorNameMissing() throws Exception {
        mockMvc.perform(get("/api/course/major-courses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
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