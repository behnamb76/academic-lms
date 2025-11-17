package ir.bahman.academic_lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.academic_lms.dto.*;
import ir.bahman.academic_lms.model.*;
import ir.bahman.academic_lms.model.answer.Answer;
import ir.bahman.academic_lms.model.answer.EssayAnswer;
import ir.bahman.academic_lms.model.answer.TestAnswer;
import ir.bahman.academic_lms.model.enums.AccountStatus;
import ir.bahman.academic_lms.model.enums.ExamInstanceStatus;
import ir.bahman.academic_lms.model.enums.ExamState;
import ir.bahman.academic_lms.model.question.EssayQuestion;
import ir.bahman.academic_lms.model.question.TestQuestion;
import ir.bahman.academic_lms.repository.*;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExamControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private OfferedCourseRepository offeredCourseRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AcademicCalenderRepository academicCalenderRepository;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private ExamInstanceRepository examInstanceRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private ExamQuestionRepository examQuestionRepository;

    private String token;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        answerRepository.deleteAll();
        examQuestionRepository.deleteAll();
        examInstanceRepository.deleteAll();
        optionRepository.deleteAll();
        examRepository.deleteAll();
        questionRepository.deleteAll();
        offeredCourseRepository.deleteAll();
        personRepository.findAll().stream()
                .filter(person -> !person.getId().equals(1L))
                .forEach(person -> personRepository.deleteById(person.getId()));
        termRepository.deleteAll();
        academicCalenderRepository.deleteAll();
        courseRepository.deleteAll();
        accountRepository.findAll().stream()
                .filter(account -> !account.getId().equals(1L))
                .forEach(account -> personRepository.deleteById(account.getId()));
    }

    private void registerPerson() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Kazemi")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .majorName("Computer")
                .username("ali_teacher")
                .password("mySecretPass123").build();

        String token1 = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/person/teacher-register")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
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
    void testCreateExam() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(person).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        LocalDateTime futureStartTime = LocalDateTime.now().plusHours(1);
        LocalDateTime futureEndTime = LocalDateTime.now().plusHours(2);

        ExamDTO dto = ExamDTO.builder()
                .title("Midterm Exam")
                .description("Midterm exam for Java Programming course.")
                .startTime(futureStartTime)
                .endTime(futureEndTime)
                .courseId(savedOfferedCourse.getId()).build();

        mockMvc.perform(post("/api/exam")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Midterm Exam"))
                .andExpect(jsonPath("$.description").value("Midterm exam for Java Programming course."))
                .andExpect(jsonPath("$.courseId").value(offeredCourse.getId()));
    }

    @Test
    void testCreateExam_shouldReturn400_validationFails() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(person).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        ExamDTO dto = ExamDTO.builder()
                .title("A")
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(1)).build();

        mockMvc.perform(post("/api/exam")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void testCreateExam_shouldReturn400_courseIdNotFound() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        ExamDTO dto = ExamDTO.builder()
                .title("Midterm Exam")
                .description("Midterm exam for Java Programming course.")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .courseId(999999L).build();

        mockMvc.perform(post("/api/exam")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateExam() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(person).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        Exam exam = Exam.builder()
                .title("Original Exam")
                .description("Original description.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.NOT_STARTED).build();
        Exam originalExam = examRepository.save(exam);
        Long examId = originalExam.getId();

        ExamDTO updateDto = ExamDTO.builder()
                .title("Updated Exam Title")
                .description("Updated description.")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .courseId(savedOfferedCourse.getId()).build();

        mockMvc.perform(put("/api/exam/" + examId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Exam Title"))
                .andExpect(jsonPath("$.description").value("Updated description."))
                .andExpect(jsonPath("$.courseId").value(offeredCourse.getId()));
    }

    @Test
    void testUpdateExam_shouldReturn404_ExamNotFound() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(person).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        ExamDTO updateDto = ExamDTO.builder()
                .title("Updated Exam Title")
                .description("Updated description.")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .courseId(savedOfferedCourse.getId()).build();

        mockMvc.perform(put("/api/exam/999999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateExam_shouldReturn400_validationFails() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(person).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        Exam exam = Exam.builder()
                .title("Another Exam")
                .description("Another description.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.NOT_STARTED).build();
        Exam originalExam = examRepository.save(exam);
        Long examId = originalExam.getId();

        ExamDTO updateDto = ExamDTO.builder()
                .title("A")
                .description("Updated description.")
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(1)).build();

        mockMvc.perform(put("/api/exam/" + examId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void testUpdateExam_shouldReturn400_courseIdNotFound() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(person).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        Exam exam = Exam.builder()
                .title("Another Exam")
                .description("Another description.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.NOT_STARTED).build();
        Exam originalExam = examRepository.save(exam);
        Long examId = originalExam.getId();

        ExamDTO updateDto = ExamDTO.builder()
                .title("Update with Invalid Course")
                .description("Updated description.")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .courseId(999999L).build();

        mockMvc.perform(put("/api/exam/" + examId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteExam() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(person).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        Exam exam = Exam.builder()
                .title("Original Exam")
                .description("Original description.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.NOT_STARTED).build();
        Exam originalExam = examRepository.save(exam);
        Long examId = originalExam.getId();
        assertThat(exam.isDeleted()).isFalse();

        mockMvc.perform(delete("/api/exam/" + examId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Exam deletedExam = examRepository.findById(examId).orElse(null);
        assertThat(deletedExam).isNotNull();
        assertThat(deletedExam.isDeleted()).isTrue();
        assertThat(deletedExam.getTitle()).isEqualTo("Original Exam");
    }

    @Test
    void testDeleteExam_shouldReturn404_examNotFound() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        mockMvc.perform(delete("/api/exam/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindExamById() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(person).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        Exam exam = Exam.builder()
                .title("Final Exam")
                .description("Final exam for the course.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.NOT_STARTED).build();
        Exam originalExam = examRepository.save(exam);
        Long examId = originalExam.getId();

        String response = mockMvc.perform(get("/api/exam/" + examId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Final Exam"))
                .andExpect(jsonPath("$.description").value("Final exam for the course."))
                .andExpect(jsonPath("$.courseId").value(offeredCourse.getId()))
                .andReturn().getResponse().getContentAsString();

        ExamDTO responseDto = objectMapper.readValue(response, ExamDTO.class);
        assertThat(responseDto.getTitle()).isEqualTo("Final Exam");
        assertThat(responseDto.getDescription()).isEqualTo("Final exam for the course.");
        assertThat(responseDto.getCourseId()).isEqualTo(offeredCourse.getId());
    }

    @Test
    void testFindExamById_shouldReturn404_examNotFound() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        mockMvc.perform(get("/api/exam/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindExamById_shouldReturn404_examIsDeleted() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(person).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        Exam exam = Exam.builder()
                .title("Deleted Exam")
                .description("This exam will be deleted.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.NOT_STARTED)
                .deleted(true).build();
        Exam originalExam = examRepository.save(exam);
        Long examId = originalExam.getId();

        mockMvc.perform(get("/api/exam/" + examId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindAllExams() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(person).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        Exam exam1 = Exam.builder()
                .title("Exam 1")
                .description("Description for exam 1.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.NOT_STARTED).build();
        examRepository.save(exam1);

        Exam exam2 = Exam.builder()
                .title("Exam 2")
                .description("Description for exam 2.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.NOT_STARTED).build();
        examRepository.save(exam2);

        Exam deletedExam = Exam.builder()
                .title("Deleted Exam")
                .description("This exam is deleted.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.NOT_STARTED)
                .deleted(true).build();
        examRepository.save(deletedExam);

        String response = mockMvc.perform(get("/api/exam")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andReturn().getResponse().getContentAsString();

        List<ExamDTO> responseDtoList = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ExamDTO.class));

        assertThat(responseDtoList).hasSize(2);
        assertThat(responseDtoList).extracting(ExamDTO::getTitle)
                .containsExactlyInAnyOrder("Exam 1", "Exam 2");
        assertThat(responseDtoList).extracting(ExamDTO::getTitle)
                .doesNotContain("Deleted Exam");
    }

    @Test
    void testFindAllExams_shouldReturnEmptyList_noExamsExist() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        String response = mockMvc.perform(get("/api/exam")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0))
                .andReturn().getResponse().getContentAsString();

        List<ExamDTO> responseDtoList = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ExamDTO.class));

        assertThat(responseDtoList).isEmpty();
    }

    @Test
    void testFindAllExamsOfACourse_asTeacher() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(person).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        Exam exam1 = Exam.builder()
                .title("Exam 1")
                .description("Description for exam 1.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.NOT_STARTED).build();
        examRepository.save(exam1);

        Exam exam2 = Exam.builder()
                .title("Exam 2")
                .description("Description for exam 2.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.NOT_STARTED).build();
        examRepository.save(exam2);

        Exam deletedExam = Exam.builder()
                .title("Deleted Exam")
                .description("This exam is deleted.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.NOT_STARTED)
                .deleted(true).build();
        examRepository.save(deletedExam);

        String response = mockMvc.perform(get("/api/exam/course-exams/" + offeredCourse.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Exam 1"))
                .andExpect(jsonPath("$[1].title").value("Exam 2"))
                .andReturn().getResponse().getContentAsString();

        List<ExamDTO> responseDtoList = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ExamDTO.class));

        assertThat(responseDtoList).hasSize(2);
        assertThat(responseDtoList).extracting(ExamDTO::getTitle)
                .containsExactlyInAnyOrder("Exam 1", "Exam 2");
        assertThat(responseDtoList).extracting(ExamDTO::getTitle)
                .doesNotContain("Deleted Exam");
    }

    @Test
    void testFindAllExamsOfACourse_asStudent() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        String token1 = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/person/student-register")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_student").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(studentRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_student", "mySecretPass123");

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(person).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        Exam exam = Exam.builder()
                .title("Student Visible Exam")
                .description("This exam is for students")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.NOT_STARTED).build();
        examRepository.save(exam);

        String response = mockMvc.perform(get("/api/exam/course-exams/" + offeredCourse.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Student Visible Exam"))
                .andReturn().getResponse().getContentAsString();

        List<ExamDTO> responseDtoList = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ExamDTO.class));

        assertThat(responseDtoList).hasSize(1);
        assertThat(responseDtoList.get(0).getTitle()).isEqualTo("Student Visible Exam");
    }

    @Test
    void testFindAllExamsOfACourse_shouldReturnEmptyList_courseHasNoExams() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm)
                .teacher(person).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        String response = mockMvc.perform(get("/api/exam/course-exams/" + offeredCourse.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0))
                .andReturn().getResponse().getContentAsString();

        List<ExamDTO> responseDtoList = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ExamDTO.class));

        assertThat(responseDtoList).isEmpty();
    }

    @Test
    void testFindAllExamsOfACourse_shouldReturn404_OfferedCourseNotFound() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        mockMvc.perform(get("/api/exam/course-exams/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

    }

    @Test
    void testStudentStartExam() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        String token1 = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/person/student-register")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();


        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        Person student = personRepository.findByAccountUsername("ali_student").orElseThrow();
        student.setOfferedCourses(List.of(savedOfferedCourse));
        personRepository.save(student);
        Account account = student.getAccount();
        account.setActiveRole(studentRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_student", "mySecretPass123");

        Exam exam = Exam.builder()
                .title("Midterm Exam")
                .description("This exam is for students")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.STARTED).build();
        examRepository.save(exam);

        mockMvc.perform(post("/api/exam/start-exam/" + exam.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        ExamInstance savedExamInstance = examInstanceRepository.findByPersonAndExam(student, exam).orElse(null);
        assertThat(savedExamInstance).isNotNull();
        assertThat(savedExamInstance.getStatus()).isEqualTo(ExamInstanceStatus.IN_PROGRESS);
        assertThat(savedExamInstance.getStartAt()).isNotNull();
    }

    @Test
    void testStudentStartExam_shouldReturn403_studentHasCompletedExam() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        String token1 = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/person/student-register")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();


        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        Person student = personRepository.findByAccountUsername("ali_student").orElseThrow();
        student.setOfferedCourses(List.of(savedOfferedCourse));
        personRepository.save(student);
        Account account = student.getAccount();
        account.setActiveRole(studentRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_student", "mySecretPass123");

        Exam exam = Exam.builder()
                .title("Completed Exam")
                .description("This exam is for students")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.STARTED).build();
        examRepository.save(exam);

        ExamInstance completedInstance = ExamInstance.builder()
                .exam(exam)
                .person(student)
                .startAt(LocalDateTime.now().minusMinutes(30))
                .totalScore(85.0)
                .status(ExamInstanceStatus.COMPLETED).build();
        examInstanceRepository.save(completedInstance);

        mockMvc.perform(post("/api/exam/start-exam/" + exam.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testStudentStartExam_shouldReturn403_studentDoesNotHaveCourse() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        String token1 = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/person/student-register")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person student = personRepository.findByAccountUsername("ali_student").orElseThrow();
        Account account = student.getAccount();
        account.setActiveRole(studentRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        token = loginAndGetToken("ali_student", "mySecretPass123");

        Exam exam = Exam.builder()
                .title("Restricted Exam")
                .description("This exam is for students")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.STARTED).build();
        examRepository.save(exam);

        mockMvc.perform(post("/api/exam/start-exam/" + exam.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testStudentStartExam_shouldReturn400_ExamNotStartedYet() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        String token1 = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/person/student-register")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();


        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        Person student = personRepository.findByAccountUsername("ali_student").orElseThrow();
        student.setOfferedCourses(List.of(savedOfferedCourse));
        personRepository.save(student);
        Account account = student.getAccount();
        account.setActiveRole(studentRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_student", "mySecretPass123");

        Exam exam = Exam.builder()
                .title("Future Exam")
                .description("This exam is for students")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.NOT_STARTED).build();
        examRepository.save(exam);

        mockMvc.perform(post("/api/exam/start-exam/" + exam.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testStudentStartExam_shouldReturn400_examIsFinished() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        String token1 = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/person/student-register")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();


        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        Person student = personRepository.findByAccountUsername("ali_student").orElseThrow();
        student.setOfferedCourses(List.of(savedOfferedCourse));
        personRepository.save(student);
        Account account = student.getAccount();
        account.setActiveRole(studentRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_student", "mySecretPass123");

        Exam exam = Exam.builder()
                .title("Past Exam")
                .description("This exam is for students")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.FINISHED).build();
        examRepository.save(exam);

        mockMvc.perform(post("/api/exam/start-exam/" + exam.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testStudentStartExam_shouldReturn404_examNotFound() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        String token1 = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/person/student-register")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow();

        Person student = personRepository.findByAccountUsername("ali_student").orElseThrow();
        Account account = student.getAccount();
        account.setActiveRole(studentRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_student", "mySecretPass123");

        mockMvc.perform(post("/api/exam/start-exam/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testStudentSubmitExam() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        String token1 = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/person/student-register")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person student = personRepository.findByAccountUsername("ali_student").orElseThrow();
        Account account = student.getAccount();
        account.setActiveRole(studentRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        token = loginAndGetToken("ali_student", "mySecretPass123");

        Exam exam = Exam.builder()
                .title("Midterm Exam")
                .description("Midterm exam for Java Programming course.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.STARTED).build();
        examRepository.save(exam);

        ExamInstance completedInstance = ExamInstance.builder()
                .exam(exam)
                .person(student)
                .startAt(LocalDateTime.now().minusMinutes(30))
                .totalScore(85.0)
                .status(ExamInstanceStatus.IN_PROGRESS).build();
        ExamInstance examInstance = examInstanceRepository.save(completedInstance);

        mockMvc.perform(post("/api/exam/submit-exam/" + exam.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        ExamInstance updatedExamInstance = examInstanceRepository.findByPersonAndExam(student, exam).orElse(null);
        assertThat(updatedExamInstance).isNotNull();
        assertThat(updatedExamInstance.getStatus()).isEqualTo(ExamInstanceStatus.COMPLETED);
        assertThat(updatedExamInstance.getEndAt()).isNotNull();
    }

    @Test
    void testStudentSubmitExam_shouldReturn403_studentHasAlreadySubmitted() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        String token1 = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/person/student-register")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person student = personRepository.findByAccountUsername("ali_student").orElseThrow();
        Account account = student.getAccount();
        account.setActiveRole(studentRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        token = loginAndGetToken("ali_student", "mySecretPass123");

        Exam exam = Exam.builder()
                .title("Midterm Exam")
                .description("Midterm exam for Java Programming course.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.STARTED).build();
        examRepository.save(exam);

        ExamInstance completedInstance = ExamInstance.builder()
                .exam(exam)
                .person(student)
                .startAt(LocalDateTime.now().minusMinutes(30))
                .totalScore(85.0)
                .status(ExamInstanceStatus.COMPLETED).build();
        ExamInstance examInstance = examInstanceRepository.save(completedInstance);

        mockMvc.perform(post("/api/exam/submit-exam/" + exam.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testStudentSubmitExam_shouldReturn404_examInstanceNotFound() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        String token1 = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/person/student-register")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person student = personRepository.findByAccountUsername("ali_student").orElseThrow();
        Account account = student.getAccount();
        account.setActiveRole(studentRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        token = loginAndGetToken("ali_student", "mySecretPass123");

        Exam exam = Exam.builder()
                .title("Midterm Exam")
                .description("Midterm exam for Java Programming course.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.STARTED).build();
        examRepository.save(exam);

        mockMvc.perform(post("/api/exam/submit-exam/" + exam.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testStudentSubmitExam_shouldReturn404_examNotFound() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        String token1 = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/person/student-register")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow();

        Person student = personRepository.findByAccountUsername("ali_student").orElseThrow();
        Account account = student.getAccount();
        account.setActiveRole(studentRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_student", "mySecretPass123");

        mockMvc.perform(post("/api/exam/submit-exam/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSubmitAnswer_testAnswer() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        String token1 = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/person/student-register")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person student = personRepository.findByAccountUsername("ali_student").orElseThrow();
        Account account = student.getAccount();
        account.setActiveRole(studentRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        token = loginAndGetToken("ali_student", "mySecretPass123");

        Exam exam = Exam.builder()
                .title("Midterm Exam")
                .description("Midterm exam for Java Programming course.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.STARTED).build();
        examRepository.save(exam);

        TestQuestion question = TestQuestion.builder()
                .title("Sample Question")
                .text("Sample test question text for Sample Question")
                .course(course)
                .defaultScore(5.0).build();

        Option optionA = Option.builder()
                .text("Option A")
                .correct(false)
                .testQuestion(question).build();

        Option optionB = Option.builder()
                .text("Option B")
                .correct(true)
                .testQuestion(question).build();

        Option optionC = Option.builder()
                .text("Option C")
                .correct(false)
                .testQuestion(question).build();

        question.setOptions(Arrays.asList(optionA, optionB, optionC));

        TestQuestion savedQuestion = (TestQuestion) questionRepository.save(question);
        optionRepository.save(optionA);
        optionRepository.save(optionB);
        optionRepository.save(optionC);

        ExamInstance completedInstance = ExamInstance.builder()
                .exam(exam)
                .person(student)
                .startAt(LocalDateTime.now().minusMinutes(30))
                .totalScore(85.0)
                .status(ExamInstanceStatus.IN_PROGRESS).build();
        ExamInstance examInstance = examInstanceRepository.save(completedInstance);

        Option correctOption = question.getOptions().stream()
                .filter(Option::isCorrect)
                .findFirst().orElseThrow();

        ExamQuestion examQuestion = ExamQuestion.builder()
                .score(5.0)
                .exam(exam)
                .question(savedQuestion).build();
        examQuestionRepository.save(examQuestion);

        AnswerDTO answerDto = AnswerDTO.builder()
                .type("test")
                .examId(exam.getId())
                .questionId(question.getId())
                .optionId(correctOption.getId()).build();

        mockMvc.perform(post("/api/exam/submit-answer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerDto)))
                .andExpect(status().isOk());

        Answer savedAnswer = answerRepository.findByExamInstanceIdAndExamQuestionId(
                examInstanceRepository.findByPersonAndExam(student, exam).orElseThrow().getId(),
                question.getId()).orElse(null);
        assertThat(savedAnswer).isNotNull();
        assertThat(savedAnswer).isInstanceOf(TestAnswer.class);
        TestAnswer testAnswer = (TestAnswer) savedAnswer;
        assertThat(testAnswer.getOption().getId()).isEqualTo(correctOption.getId());
        assertThat(testAnswer.getExamQuestion().getQuestion().getId()).isEqualTo(question.getId());
    }

    @Test
    void testSubmitAnswer_essayAnswer() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        String token1 = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/person/student-register")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person student = personRepository.findByAccountUsername("ali_student").orElseThrow();
        Account account = student.getAccount();
        account.setActiveRole(studentRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        token = loginAndGetToken("ali_student", "mySecretPass123");

        Exam exam = Exam.builder()
                .title("Midterm Exam")
                .description("Midterm exam for Java Programming course.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.STARTED).build();
        examRepository.save(exam);

        EssayQuestion question = EssayQuestion.builder()
                .title("Essay Question")
                .text("Discuss the impact of AI.")
                .course(course)
                .defaultScore(5.0).build();
        EssayQuestion savedQuestion = questionRepository.save(question);

        ExamInstance completedInstance = ExamInstance.builder()
                .exam(exam)
                .person(student)
                .startAt(LocalDateTime.now().minusMinutes(30))
                .totalScore(85.0)
                .status(ExamInstanceStatus.IN_PROGRESS).build();
        ExamInstance examInstance = examInstanceRepository.save(completedInstance);

        ExamQuestion examQuestion = ExamQuestion.builder()
                .score(5.0)
                .exam(exam)
                .question(savedQuestion).build();
        examQuestionRepository.save(examQuestion);

        AnswerDTO answerDto = AnswerDTO.builder()
                .type("essay")
                .examId(exam.getId())
                .questionId(question.getId())
                .answerText("This is my detailed essay answer discussing the impact of AI...").build();

        mockMvc.perform(post("/api/exam/submit-answer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerDto)))
                .andExpect(status().isOk());

        Answer savedAnswer = answerRepository.findByExamInstanceIdAndExamQuestionId(
                examInstanceRepository.findByPersonAndExam(student, exam).orElseThrow().getId(),
                savedQuestion.getId()).orElse(null);
        assertThat(savedAnswer).isNotNull();
        assertThat(savedAnswer).isInstanceOf(EssayAnswer.class);
        EssayAnswer essayAnswer = (EssayAnswer) savedAnswer;
        assertThat(essayAnswer.getText()).isEqualTo("This is my detailed essay answer discussing the impact of AI...");
        assertThat(essayAnswer.getExamQuestion().getQuestion().getId()).isEqualTo(savedQuestion.getId());
    }

    @Test
    void testSubmitAnswer_shouldReturn400_validationFails() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        String token1 = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/person/student-register")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow();

        Person student = personRepository.findByAccountUsername("ali_student").orElseThrow();
        Account account = student.getAccount();
        account.setActiveRole(studentRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_student", "mySecretPass123");

        AnswerDTO answerDto = new AnswerDTO();

        mockMvc.perform(post("/api/exam/submit-answer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void testGradingEssayQuestionOfExam() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();

        Person teacher = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account teacherAccount = teacher.getAccount();
        teacherAccount.setActiveRole(teacherRole);
        teacherAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(teacherAccount);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        String token1 = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/person/student-register")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person student = personRepository.findByAccountUsername("ali_student").orElseThrow();
        Account studentAccount = student.getAccount();
        studentAccount.setActiveRole(studentRole);
        studentAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(studentAccount);

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        Exam exam = Exam.builder()
                .title("Midterm Exam")
                .description("Midterm exam for Java Programming course.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.STARTED).build();
        examRepository.save(exam);

        EssayQuestion question = EssayQuestion.builder()
                .title("Essay Q1")
                .text("Discuss the impact of AI.")
                .course(course)
                .defaultScore(5.0).build();
        EssayQuestion savedQuestion = questionRepository.save(question);

        ExamInstance completedInstance = ExamInstance.builder()
                .exam(exam)
                .person(student)
                .startAt(LocalDateTime.now().minusMinutes(30))
                .totalScore(0.0)
                .status(ExamInstanceStatus.IN_PROGRESS).build();
        ExamInstance examInstance = examInstanceRepository.save(completedInstance);

        ExamQuestion examQuestion = ExamQuestion.builder()
                .score(5.0)
                .exam(exam)
                .question(savedQuestion).build();
        ExamQuestion savedExamQuestion = examQuestionRepository.save(examQuestion);

        EssayAnswer essayAnswer = EssayAnswer.builder()
                .examQuestion(savedExamQuestion)
                .examInstance(examInstance)
                .text("Initial essay answer text provided by student.")
                .build();
        answerRepository.save(essayAnswer);

        GradingDTO dto = GradingDTO.builder()
                .examId(exam.getId())
                .studentId(student.getId())
                .questionId(question.getId())
                .score(15.5).build();

        Double initialExamInstanceScore = examInstance.getTotalScore();
        assertThat(initialExamInstanceScore).isZero();

        mockMvc.perform(post("/api/exam/grading-essay")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        Answer savedAnswer = answerRepository.findByExamQuestionAndExamInstance(examQuestion, examInstance).orElse(null);
        assertThat(savedAnswer).isNotNull();
        assertThat(savedAnswer.getScore()).isEqualTo(15.5);

        ExamInstance updatedExamInstance = examInstanceRepository.findById(examInstance.getId()).orElse(null);
        assertThat(updatedExamInstance).isNotNull();
        assertThat(updatedExamInstance.getTotalScore()).isEqualTo(initialExamInstanceScore + 15.5);
    }

    @Test
    void testGradingEssayQuestionOfExam_shouldReturn400_scoreIsNegative() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();

        Person teacher = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account teacherAccount = teacher.getAccount();
        teacherAccount.setActiveRole(teacherRole);
        teacherAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(teacherAccount);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        String token1 = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/person/student-register")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person student = personRepository.findByAccountUsername("ali_student").orElseThrow();
        Account studentAccount = student.getAccount();
        studentAccount.setActiveRole(studentRole);
        studentAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(studentAccount);

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        Exam exam = Exam.builder()
                .title("Midterm Exam")
                .description("Midterm exam for Java Programming course.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.STARTED).build();
        examRepository.save(exam);

        EssayQuestion question = EssayQuestion.builder()
                .title("Essay Q1")
                .text("Discuss the impact of AI.")
                .course(course)
                .defaultScore(5.0).build();
        EssayQuestion savedQuestion = questionRepository.save(question);

        ExamInstance completedInstance = ExamInstance.builder()
                .exam(exam)
                .person(student)
                .startAt(LocalDateTime.now().minusMinutes(30))
                .totalScore(0.0)
                .status(ExamInstanceStatus.IN_PROGRESS).build();
        ExamInstance examInstance = examInstanceRepository.save(completedInstance);

        ExamQuestion examQuestion = ExamQuestion.builder()
                .score(5.0)
                .exam(exam)
                .question(savedQuestion).build();
        ExamQuestion savedExamQuestion = examQuestionRepository.save(examQuestion);

        EssayAnswer essayAnswer = EssayAnswer.builder()
                .examQuestion(savedExamQuestion)
                .examInstance(examInstance)
                .text("Initial essay answer text provided by student.")
                .build();
        answerRepository.save(essayAnswer);

        GradingDTO dto = GradingDTO.builder()
                .examId(exam.getId())
                .studentId(student.getId())
                .questionId(question.getId())
                .score(-5.0).build();

        mockMvc.perform(post("/api/exam/grading-essay")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGradingEssayQuestionOfExam_shouldReturn404_ExamNotFound() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();

        Person teacher = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account teacherAccount = teacher.getAccount();
        teacherAccount.setActiveRole(teacherRole);
        teacherAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(teacherAccount);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .nationalCode("2234567890")
                .phoneNumber("09223456789")
                .majorName("Computer")
                .username("ali_student")
                .password("mySecretPass123").build();

        String token1 = loginAndGetToken("admin", "admin");

        mockMvc.perform(post("/api/person/student-register")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person student = personRepository.findByAccountUsername("ali_student").orElseThrow();
        Account studentAccount = student.getAccount();
        studentAccount.setActiveRole(studentRole);
        studentAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(studentAccount);

        Course course = Course.builder()
                .title("Java Programming")
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

        OfferedCourse offeredCourse = OfferedCourse.builder()
                .meetingDay(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .capacity(30)
                .location("Room 202")
                .course(savedCourse)
                .term(savedTerm).build();
        OfferedCourse savedOfferedCourse = offeredCourseRepository.save(offeredCourse);

        Exam exam = Exam.builder()
                .title("Midterm Exam")
                .description("Midterm exam for Java Programming course.")
                .offeredCourse(savedOfferedCourse)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .score(100.0)
                .examState(ExamState.STARTED).build();
        examRepository.save(exam);

        EssayQuestion question = EssayQuestion.builder()
                .title("Essay Q1")
                .text("Discuss the impact of AI.")
                .course(course)
                .defaultScore(5.0).build();
        EssayQuestion savedQuestion = questionRepository.save(question);

        GradingDTO dto = GradingDTO.builder()
                .examId(999999L)
                .studentId(student.getId())
                .questionId(question.getId())
                .score(8.0).build();

        mockMvc.perform(post("/api/exam/grading-essay")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }
}
