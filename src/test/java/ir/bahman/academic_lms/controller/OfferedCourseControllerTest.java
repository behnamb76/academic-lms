package ir.bahman.academic_lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.academic_lms.dto.LoginRequest;
import ir.bahman.academic_lms.dto.OfferedCourseDTO;
import ir.bahman.academic_lms.dto.OfferedCourseResponseDTO;
import ir.bahman.academic_lms.dto.RegisterRequest;
import ir.bahman.academic_lms.model.*;
import ir.bahman.academic_lms.model.enums.AccountStatus;
import ir.bahman.academic_lms.repository.*;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
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
class OfferedCourseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OfferedCourseRepository offeredCourseRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AcademicCalenderRepository academicCalenderRepository;

    @Autowired
    private AccountRepository accountRepository;

    private String token;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        LoginRequest loginReq = LoginRequest.builder()
                .username(username)
                .password(password).build();

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    @Test
    void testCreateOfferedCourse() throws Exception {
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person teacher = Person.builder()
                .firstName("Ali")
                .lastName("Rezaei")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .major(cMajor)
                .roles(List.of(teacherRole)).build();
        Person savedTeacher = personRepository.save(teacher);

        Course course = Course.builder()
                .title("Advanced Java")
                .unit(3)
                .major(cMajor).build();
        Course savedCourse = courseRepository.save(course);

        AcademicCalender calender = AcademicCalender.builder()
                .registrationStart(LocalDate.now())
                .registrationEnd(LocalDate.now().plusDays(2))
                .classesStartDate(LocalDate.now().plusDays(10))
                .classesEndDate(LocalDate.now().plusDays(110)).build();
        AcademicCalender savedCalender = academicCalenderRepository.save(calender);

        Term term = Term.builder()
                .year(2025)
                .major(cMajor)
                .academicCalender(savedCalender).build();
        Term savedTerm = termRepository.save(term);

        OfferedCourseDTO dto = OfferedCourseDTO.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .capacity(30)
                .location("Room 101")
                .courseId(savedCourse.getId())
                .termId(savedTerm.getId())
                .teacherId(savedTeacher.getId()).build();

        token = loginAndGetToken("admin", "admin");

        String response = mockMvc.perform(post("/api/offered-course")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseTitle").value("Advanced Java"))
                .andExpect(jsonPath("$.teacherName").value("Ali Rezaei"))
                .andExpect(jsonPath("$.majorName").value("Computer"))
                .andExpect(jsonPath("$.termId").value(savedTerm.getId()))
                .andReturn().getResponse().getContentAsString();

        OfferedCourseResponseDTO responseDto = objectMapper.readValue(response, OfferedCourseResponseDTO.class);
        OfferedCourse savedOffered = offeredCourseRepository.findByCourse_Title(responseDto.getCourseTitle());
        assertThat(savedOffered.getCapacity()).isEqualTo(30);
        assertThat(savedOffered.getLocation()).isEqualTo("Room 101");
        assertThat(savedOffered.getTeacher().getId()).isEqualTo(savedTeacher.getId());
        assertThat(savedOffered.getCourse().getId()).isEqualTo(savedCourse.getId());
        assertThat(savedOffered.getTerm().getId()).isEqualTo(savedTerm.getId());
    }

    @Test
    void testCreateOfferedCourse_shouldReject_EndTimeBeforeStartTime() throws Exception {
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person teacher = Person.builder()
                .firstName("Sara")
                .lastName("Ahmadi")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .major(cMajor)
                .roles(List.of(teacherRole)).build();
        Person savedTeacher = personRepository.save(teacher);

        Course course = Course.builder()
                .title("Math")
                .unit(3)
                .major(cMajor).build();
        Course savedCourse = courseRepository.save(course);

        AcademicCalender calender = AcademicCalender.builder()
                .registrationStart(LocalDate.now())
                .registrationEnd(LocalDate.now().plusDays(2))
                .classesStartDate(LocalDate.now().plusDays(10))
                .classesEndDate(LocalDate.now().plusDays(110)).build();
        AcademicCalender savedCalender = academicCalenderRepository.save(calender);

        Term term = Term.builder()
                .year(2025)
                .major(cMajor)
                .academicCalender(savedCalender).build();
        Term savedTerm = termRepository.save(term);

        OfferedCourseDTO dto = OfferedCourseDTO.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .courseId(savedCourse.getId())
                .termId(savedTerm.getId())
                .teacherId(savedTeacher.getId()).build();

        token = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/offered-course")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void testCreateOfferedCourse_shouldReject_teacherMajorDoesNotMatchTermMajor() throws Exception {
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();
        Major major = Major.builder()
                .name("Mathematics")
                .majorCode(UUID.randomUUID()).build();
        Major mathMajor = majorRepository.save(major);

        Person teacher = Person.builder()
                .firstName("Sara")
                .lastName("Ahmadi")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .major(mathMajor)
                .roles(List.of(teacherRole)).build();
        Person savedTeacher = personRepository.save(teacher);

        Course course = Course.builder()
                .title("CS Course")
                .unit(3)
                .major(cMajor).build();
        Course savedCourse = courseRepository.save(course);

        AcademicCalender calender = AcademicCalender.builder()
                .registrationStart(LocalDate.now())
                .registrationEnd(LocalDate.now().plusDays(2))
                .classesStartDate(LocalDate.now().plusDays(10))
                .classesEndDate(LocalDate.now().plusDays(110)).build();
        AcademicCalender savedCalender = academicCalenderRepository.save(calender);

        Term term = Term.builder()
                .year(2025)
                .major(cMajor)
                .academicCalender(savedCalender).build();
        Term savedTerm = termRepository.save(term);

        OfferedCourseDTO dto = OfferedCourseDTO.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .capacity(20)
                .location("Room 303")
                .courseId(savedCourse.getId())
                .termId(savedTerm.getId())
                .teacherId(savedTeacher.getId()).build();

        token = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/offered-course")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateOfferedCourse_shouldReject_courseNotFound() throws Exception {
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();
        Person teacher = Person.builder()
                .firstName("Sara")
                .lastName("Ahmadi")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .major(cMajor)
                .roles(List.of(teacherRole)).build();
        Person savedTeacher = personRepository.save(teacher);

        AcademicCalender calender = AcademicCalender.builder()
                .registrationStart(LocalDate.now())
                .registrationEnd(LocalDate.now().plusDays(2))
                .classesStartDate(LocalDate.now().plusDays(10))
                .classesEndDate(LocalDate.now().plusDays(110)).build();
        AcademicCalender savedCalender = academicCalenderRepository.save(calender);

        Term term = Term.builder()
                .year(2025)
                .major(cMajor)
                .academicCalender(savedCalender).build();
        Term savedTerm = termRepository.save(term);

        OfferedCourseDTO dto = OfferedCourseDTO.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .capacity(20)
                .location("Room X")
                .courseId(999999L)
                .termId(savedTerm.getId())
                .teacherId(savedTeacher.getId()).build();

        token = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/offered-course")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateOfferedCourse() throws Exception {
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        AcademicCalender calender = AcademicCalender.builder()
                .registrationStart(LocalDate.now().plusDays(10))
                .registrationEnd(LocalDate.now().plusDays(20))
                .classesStartDate(LocalDate.now().plusDays(25))
                .classesEndDate(LocalDate.now().plusDays(100)).build();
        AcademicCalender savedCalender = academicCalenderRepository.save(calender);

        Term term = Term.builder()
                .year(2025)
                .major(cMajor)
                .academicCalender(savedCalender).build();
        Term savedTerm = termRepository.save(term);

        Course course = Course.builder()
                .title("Java Programming")
                .unit(3)
                .major(cMajor).build();
        Course savedCourse = courseRepository.save(course);

        Person teacher = Person.builder()
                .firstName("Ali")
                .lastName("Rezaei")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .major(cMajor)
                .roles(List.of(teacherRole)).build();
        Person savedTeacher = personRepository.save(teacher);

        OfferedCourse offered = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .capacity(20)
                .location("Room A")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(savedTeacher).build();
        OfferedCourse savedOffered = offeredCourseRepository.save(offered);

        OfferedCourseDTO updateDto = OfferedCourseDTO.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .capacity(30)
                .location("Room B")
                .courseId(savedCourse.getId())
                .termId(savedTerm.getId())
                .teacherId(savedTeacher.getId()).build();

        token = loginAndGetToken("admin", "admin");

        String response = mockMvc.perform(put("/api/offered-course/" + savedOffered.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseTitle").value("Java Programming"))
                .andReturn().getResponse().getContentAsString();

        OfferedCourseResponseDTO responseDto = objectMapper.readValue(response,
                ir.bahman.academic_lms.dto.OfferedCourseResponseDTO.class);
        OfferedCourse updated = offeredCourseRepository.findByCourse_Title(responseDto.getCourseTitle());

        assertThat(updated.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(updated.getEndTime()).isEqualTo(LocalTime.of(12, 0));
        assertThat(updated.getCapacity()).isEqualTo(30);
        assertThat(updated.getMeetingDay()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(updated.getLocation()).isEqualTo("Room A");
    }

    @Test
    void testUpdateOfferedCourse_shouldReject_afterTermStartDate() throws Exception {
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        AcademicCalender pastCalender = AcademicCalender.builder()
                .registrationStart(LocalDate.now().minusDays(5))
                .registrationEnd(LocalDate.now().minusDays(1))
                .classesStartDate(LocalDate.now().plusDays(5))
                .classesEndDate(LocalDate.now().plusDays(80)).build();
        AcademicCalender savedCalender = academicCalenderRepository.save(pastCalender);

        Term term = Term.builder()
                .year(2025)
                .major(cMajor)
                .academicCalender(savedCalender).build();
        Term savedTerm = termRepository.save(term);

        Course course = Course.builder()
                .title("Java Programming")
                .unit(3)
                .major(cMajor).build();
        Course savedCourse = courseRepository.save(course);

        Person teacher = Person.builder()
                .firstName("Ali")
                .lastName("Rezaei")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .major(cMajor)
                .roles(List.of(teacherRole)).build();
        Person savedTeacher = personRepository.save(teacher);

        OfferedCourse offered = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .capacity(25)
                .location("Room C")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(savedTeacher).build();
        OfferedCourse savedOffered = offeredCourseRepository.save(offered);

        OfferedCourseDTO updateDto = OfferedCourseDTO.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .capacity(35)
                .location("Room D")
                .courseId(savedCourse.getId())
                .termId(savedTerm.getId())
                .teacherId(savedTeacher.getId()).build();

        token = loginAndGetToken("admin", "admin");

        mockMvc.perform(put("/api/offered-course/" + savedOffered.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot update offered course after the term start date"));
    }

    @Test
    void testUpdateOfferedCourse_shouldReturn404_offeredCourseNotFound() throws Exception {
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person teacher = Person.builder()
                .firstName("Ali")
                .lastName("Rezaei")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .major(cMajor)
                .roles(List.of(teacherRole)).build();
        Person savedTeacher = personRepository.save(teacher);

        Course course = Course.builder()
                .title("Advanced Java")
                .unit(3)
                .major(cMajor).build();
        Course savedCourse = courseRepository.save(course);

        AcademicCalender calender = AcademicCalender.builder()
                .registrationStart(LocalDate.now())
                .registrationEnd(LocalDate.now().plusDays(2))
                .classesStartDate(LocalDate.now().plusDays(10))
                .classesEndDate(LocalDate.now().plusDays(110)).build();
        AcademicCalender savedCalender = academicCalenderRepository.save(calender);

        Term term = Term.builder()
                .year(2025)
                .major(cMajor)
                .academicCalender(savedCalender).build();
        Term savedTerm = termRepository.save(term);

        OfferedCourseDTO dto = OfferedCourseDTO.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .capacity(30)
                .location("Room 101")
                .courseId(savedCourse.getId())
                .termId(savedTerm.getId())
                .teacherId(savedTeacher.getId()).build();

        token = loginAndGetToken("admin", "admin");

        mockMvc.perform(put("/api/offered-course/999999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateOfferedCourse_shouldReject_InvalidUpdateData() throws Exception {
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person teacher = Person.builder()
                .firstName("Ali")
                .lastName("Rezaei")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .major(cMajor)
                .roles(List.of(teacherRole)).build();
        Person savedTeacher = personRepository.save(teacher);

        Course course = Course.builder()
                .title("Advanced Java")
                .unit(3)
                .major(cMajor).build();
        Course savedCourse = courseRepository.save(course);

        AcademicCalender calender = AcademicCalender.builder()
                .registrationStart(LocalDate.now().plusDays(10))
                .registrationEnd(LocalDate.now().plusDays(20))
                .classesStartDate(LocalDate.now().plusDays(25))
                .classesEndDate(LocalDate.now().plusDays(100)).build();
        AcademicCalender savedCalender = academicCalenderRepository.save(calender);

        Term term = Term.builder()
                .year(2025)
                .major(cMajor)
                .academicCalender(savedCalender).build();
        Term savedTerm = termRepository.save(term);

        OfferedCourse offered = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .capacity(20)
                .location("Room E")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(savedTeacher).build();
        OfferedCourse savedOffered = offeredCourseRepository.save(offered);

        OfferedCourseDTO invalidDto = OfferedCourseDTO.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(11, 0))
                .capacity(25)
                .location("Room F")
                .courseId(savedCourse.getId())
                .termId(savedTerm.getId())
                .teacherId(savedTeacher.getId()).build();

        token = loginAndGetToken("admin", "admin");

        mockMvc.perform(put("/api/offered-course/" + savedOffered.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void testDeleteOfferedCourse() throws Exception {
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person teacher = Person.builder()
                .firstName("Ali")
                .lastName("Rezaei")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .major(cMajor)
                .roles(List.of(teacherRole)).build();
        Person savedTeacher = personRepository.save(teacher);

        Course course = Course.builder()
                .title("Java Programming")
                .unit(3)
                .major(cMajor).build();
        Course savedCourse = courseRepository.save(course);

        AcademicCalender calender = AcademicCalender.builder()
                .registrationStart(LocalDate.now().plusDays(10))
                .registrationEnd(LocalDate.now().plusDays(20))
                .classesStartDate(LocalDate.now().plusDays(25))
                .classesEndDate(LocalDate.now().plusDays(100)).build();
        AcademicCalender savedCalender = academicCalenderRepository.save(calender);

        Term term = Term.builder()
                .year(2025)
                .major(cMajor)
                .academicCalender(savedCalender).build();
        Term savedTerm = termRepository.save(term);

        OfferedCourse offered = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .capacity(20)
                .location("Room A")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(savedTeacher).build();
        OfferedCourse savedOffered = offeredCourseRepository.save(offered);

        token = loginAndGetToken("admin", "admin");

        mockMvc.perform(delete("/api/offered-course/" + savedOffered.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteOfferedCourse_shouldReturn404_offeredCourseNotFound() throws Exception {
        token = loginAndGetToken("admin", "admin");

        mockMvc.perform(delete("/api/offered-course/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

    }

    @Test
    void testFindOfferedCourseById() throws Exception {
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person teacher = Person.builder()
                .firstName("Ali")
                .lastName("Rezaei")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .major(cMajor)
                .roles(List.of(teacherRole)).build();
        Person savedTeacher = personRepository.save(teacher);

        Course course = Course.builder()
                .title("Java Programming")
                .unit(3)
                .major(cMajor).build();
        Course savedCourse = courseRepository.save(course);

        AcademicCalender calender = AcademicCalender.builder()
                .registrationStart(LocalDate.now().plusDays(10))
                .registrationEnd(LocalDate.now().plusDays(20))
                .classesStartDate(LocalDate.now().plusDays(25))
                .classesEndDate(LocalDate.now().plusDays(100)).build();
        AcademicCalender savedCalender = academicCalenderRepository.save(calender);

        Term term = Term.builder()
                .year(2025)
                .major(cMajor)
                .academicCalender(savedCalender).build();
        Term savedTerm = termRepository.save(term);

        OfferedCourse offered = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .capacity(20)
                .location("Room A")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(savedTeacher).build();
        OfferedCourse savedOffered = offeredCourseRepository.save(offered);

        token = loginAndGetToken("admin", "admin");

        String response = mockMvc.perform(get("/api/offered-course/" + savedOffered.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseTitle").value("Java Programming"))
                .andExpect(jsonPath("$.teacherName").value("Ali Rezaei"))
                .andExpect(jsonPath("$.majorName").value("Computer"))
                .andExpect(jsonPath("$.capacity").value(20))
                .andReturn().getResponse().getContentAsString();

        OfferedCourseResponseDTO responseDto = objectMapper.readValue(response, OfferedCourseResponseDTO.class);
        assertThat(responseDto.getTermId()).isEqualTo(savedTerm.getId());
        assertThat(responseDto.getCapacity()).isEqualTo(20);
        assertThat(responseDto.getLocation()).isEqualTo("Room A");
    }

    @Test
    void testFindOfferedCourseById_shouldReturn404_afterDelete() throws Exception {
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person teacher = Person.builder()
                .firstName("Ali")
                .lastName("Rezaei")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .major(cMajor)
                .roles(List.of(teacherRole)).build();
        Person savedTeacher = personRepository.save(teacher);

        Course course = Course.builder()
                .title("Java Programming")
                .unit(3)
                .major(cMajor).build();
        Course savedCourse = courseRepository.save(course);

        AcademicCalender calender = AcademicCalender.builder()
                .registrationStart(LocalDate.now().plusDays(10)).build();
        AcademicCalender savedCalender = academicCalenderRepository.save(calender);

        Term term = Term.builder()
                .year(2025)
                .major(cMajor)
                .academicCalender(savedCalender).build();
        Term savedTerm = termRepository.save(term);

        OfferedCourse offered = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .capacity(25)
                .location("Room B")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(savedTeacher).build();
        OfferedCourse savedOffered = offeredCourseRepository.save(offered);

        token = loginAndGetToken("admin", "admin");

        mockMvc.perform(get("/api/offered-course/" + savedOffered.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/offered-course/" + savedOffered.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/offered-course/" + savedOffered.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindOfferedCourseById_shouldReturn404_offeredCourseDoesNotExist() throws Exception {
        token = loginAndGetToken("admin", "admin");

        mockMvc.perform(get("/api/offered-course/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindAllOfferedCourses() throws Exception {
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person teacher = Person.builder()
                .firstName("Ali")
                .lastName("Rezaei")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .major(cMajor)
                .roles(List.of(teacherRole)).build();
        Person savedTeacher = personRepository.save(teacher);

        Course course1 = Course.builder()
                .title("Java Programming")
                .unit(3)
                .major(cMajor).build();
        Course savedCourse1 = courseRepository.save(course1);

        Course course2 = Course.builder()
                .title("Mathematics")
                .unit(4)
                .major(cMajor).build();
        Course savedCourse2 = courseRepository.save(course2);

        AcademicCalender calender = AcademicCalender.builder()
                .registrationStart(LocalDate.now().plusDays(10))
                .registrationEnd(LocalDate.now().plusDays(20))
                .classesStartDate(LocalDate.now().plusDays(25))
                .classesEndDate(LocalDate.now().plusDays(100)).build();
        AcademicCalender savedCalender = academicCalenderRepository.save(calender);

        Term term = Term.builder()
                .year(2025)
                .major(cMajor)
                .academicCalender(savedCalender).build();
        Term savedTerm = termRepository.save(term);

        OfferedCourse offered1 = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .capacity(20)
                .location("Room A")
                .course(savedCourse1)
                .term(savedTerm)
                .teacher(savedTeacher).build();
        OfferedCourse savedOffered1 = offeredCourseRepository.save(offered1);

        OfferedCourse offered2 = OfferedCourse.builder()
                .meetingDay(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .capacity(25)
                .location("Room B")
                .course(savedCourse2)
                .term(savedTerm)
                .teacher(savedTeacher).build();
        OfferedCourse savedOffered2 = offeredCourseRepository.save(offered2);

        token = loginAndGetToken("admin", "admin");

        String response = mockMvc.perform(get("/api/offered-course")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<OfferedCourseResponseDTO> courses = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, OfferedCourseResponseDTO.class));

        assertThat(courses).hasSize(2);
        assertThat(courses).extracting(OfferedCourseResponseDTO::getCourseTitle)
                .containsExactlyInAnyOrder("Java Programming", "Mathematics");
        assertThat(courses).extracting(OfferedCourseResponseDTO::getTeacherName)
                .containsOnly("Ali Rezaei");
        assertThat(courses).extracting(OfferedCourseResponseDTO::getCapacity)
                .containsExactlyInAnyOrder(20, 25);
    }

    @Test
    void testFindAllOfferedCourses_shouldReturnEmptyList_noOfferedCourses() throws Exception {
        token = loginAndGetToken("admin", "admin");

        String response = mockMvc.perform(get("/api/offered-course")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<OfferedCourseResponseDTO> courses = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, OfferedCourseResponseDTO.class));

        assertThat(courses).isEmpty();
    }

    @Test
    void testFindAllTeacherCourses() throws Exception {
        token = loginAndGetToken("admin", "admin");

        RegisterRequest req1 = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .majorName("Computer")
                .username("teacher_john")
                .password("mySecretPass123").build();

        mockMvc.perform(post("/api/person/teacher-register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        RegisterRequest req2 = RegisterRequest.builder()
                .firstName("Sara")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("teacher_sara")
                .password("mySecretPass123").build();

        mockMvc.perform(post("/api/person/teacher-register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated());

        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person johnPerson = personRepository.findByAccountUsername("teacher_john").orElseThrow();
        Account johnPersonAccount = johnPerson.getAccount();
        johnPersonAccount.setActiveRole(teacherRole);
        johnPersonAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(johnPersonAccount);

        Person saraPerson = personRepository.findByAccountUsername("teacher_sara").orElseThrow();
        Account saraPersonAccount = saraPerson.getAccount();
        saraPersonAccount.setActiveRole(teacherRole);
        saraPersonAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(saraPersonAccount);

        Course course1 = Course.builder()
                .title("Java Programming")
                .unit(3)
                .major(cMajor).build();
        Course savedCourse1 = courseRepository.save(course1);

        Course course2 = Course.builder()
                .title("Advanced Java")
                .unit(4)
                .major(cMajor).build();
        Course savedCourse2 = courseRepository.save(course2);

        AcademicCalender calender = AcademicCalender.builder()
                .registrationStart(LocalDate.now().plusDays(10))
                .registrationEnd(LocalDate.now().plusDays(20))
                .classesStartDate(LocalDate.now().plusDays(25))
                .classesEndDate(LocalDate.now().plusDays(100)).build();
        AcademicCalender savedCalender = academicCalenderRepository.save(calender);

        Term term = Term.builder()
                .year(2025)
                .major(cMajor)
                .academicCalender(savedCalender).build();
        Term savedTerm = termRepository.save(term);

        OfferedCourse johnCourse1 = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .capacity(20)
                .location("Room A")
                .course(savedCourse1)
                .term(savedTerm)
                .teacher(johnPerson).build();
        offeredCourseRepository.save(johnCourse1);

        OfferedCourse johnCourse2 = OfferedCourse.builder()
                .meetingDay(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .capacity(25)
                .location("Room B")
                .course(savedCourse2)
                .term(savedTerm)
                .teacher(johnPerson).build();
        offeredCourseRepository.save(johnCourse2);

        OfferedCourse saraCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .capacity(25)
                .location("Room B")
                .course(savedCourse2)
                .term(savedTerm)
                .teacher(saraPerson).build();
        offeredCourseRepository.save(saraCourse);

        token = loginAndGetToken("teacher_john", "mySecretPass123");

        String response = mockMvc.perform(get("/api/offered-course/teacher-courses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<OfferedCourseResponseDTO> johnCourses = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, OfferedCourseResponseDTO.class));

        assertThat(johnCourses).hasSize(2);
        assertThat(johnCourses).extracting(OfferedCourseResponseDTO::getCourseTitle)
                .containsExactlyInAnyOrder("Java Programming", "Advanced Java");
        assertThat(johnCourses).extracting(OfferedCourseResponseDTO::getTeacherName)
                .containsOnly("John Doe");
    }

    @Test
    void testFindAllTeacherCourses_shouldReturnEmptyList_teacherHasNoCourses() throws Exception {
        token = loginAndGetToken("admin", "admin");

        RegisterRequest req1 = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .majorName("Computer")
                .username("teacher_john")
                .password("mySecretPass123").build();

        mockMvc.perform(post("/api/person/teacher-register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();

        Person johnPerson = personRepository.findByAccountUsername("teacher_john").orElseThrow();
        Account johnPersonAccount = johnPerson.getAccount();
        johnPersonAccount.setActiveRole(teacherRole);
        johnPersonAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(johnPersonAccount);

        token = loginAndGetToken("teacher_john", "mySecretPass123");

        String response = mockMvc.perform(get("/api/offered-course/teacher-courses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<OfferedCourseResponseDTO> courses = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, OfferedCourseResponseDTO.class));

        assertThat(courses).isEmpty();
    }

    @Test
    void findAllStudentCourses() {
    }

    @Test
    void findAllTermCourses() {
    }
}