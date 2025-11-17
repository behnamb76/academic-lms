package ir.bahman.academic_lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.academic_lms.dto.*;
import ir.bahman.academic_lms.mapper.QuestionMapper;
import ir.bahman.academic_lms.model.*;
import ir.bahman.academic_lms.model.enums.AccountStatus;
import ir.bahman.academic_lms.model.enums.ExamState;
import ir.bahman.academic_lms.model.question.Question;
import ir.bahman.academic_lms.repository.*;
import ir.bahman.academic_lms.service.QuestionService;
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
import org.springframework.web.context.WebApplicationContext;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuestionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private AcademicCalenderRepository academicCalenderRepository;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private ExamQuestionRepository examQuestionRepository;

    @Autowired
    private OfferedCourseRepository offeredCourseRepository;

    @Autowired
    private QuestionRepository questionRepository;

    private String token;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        examQuestionRepository.deleteAll();
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

    @Test
    void testCreateQuestion_testQuestion() throws Exception {
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

        OptionDTO option1 = OptionDTO.builder()
                .text("Option A")
                .correct(false).build();

        OptionDTO option2 = OptionDTO.builder()
                .text("Option B")
                .correct(true).build();

        OptionDTO option3 = OptionDTO.builder()
                .text("Option C")
                .correct(false).build();

        QuestionDTO dto = QuestionDTO.builder()
                .questionType("test")
                .title("Sample Question")
                .text("What is the correct answer?")
                .defaultScore(2.5)
                .courseName("Java Programming")
                .majorName("Computer")
                .options(Arrays.asList(option1, option2, option3)).build();

        String response = mockMvc.perform(post("/api/question")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Sample Question"))
                .andExpect(jsonPath("$.text").value("What is the correct answer?"))
                .andExpect(jsonPath("$.defaultScore").value(2.5))
                .andExpect(jsonPath("$.courseName").value("Java Programming"))
                .andExpect(jsonPath("$.majorName").value("Computer"))
                .andExpect(jsonPath("$.questionType").value("TEST"))
                .andExpect(jsonPath("$.options").isArray())
                .andExpect(jsonPath("$.options").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        QuestionDTO responseDto = objectMapper.readValue(response, QuestionDTO.class);
        assertThat(responseDto.getOptions()).hasSize(3);
        assertThat(responseDto.getOptions()).extracting(OptionDTO::getText)
                .containsExactlyInAnyOrder("Option A", "Option B", "Option C");
    }

    @Test
    void testCreateQuestion_essayQuestion() throws Exception {
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

        QuestionDTO dto = QuestionDTO.builder()
                .questionType("essay")
                .title("Essay Question")
                .text("Why is Java a platform independent language?")
                .defaultScore(5.0)
                .courseName("Java Programming")
                .majorName("Computer")
                .build();

        String response = mockMvc.perform(post("/api/question")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Essay Question"))
                .andExpect(jsonPath("$.questionType").value("ESSAY"))
                .andExpect(jsonPath("$.options").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        QuestionDTO responseDto = objectMapper.readValue(response, QuestionDTO.class);
        assertThat(responseDto.getQuestionType()).isEqualTo("ESSAY");
    }

    @Test
    void testCreateQuestion_shouldReject_testQuestionWithoutCorrectOption() throws Exception {
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

        OptionDTO option1 = OptionDTO.builder()
                .text("Option A")
                .correct(false).build();

        OptionDTO option2 = OptionDTO.builder()
                .text("Option B")
                .correct(false).build();

        QuestionDTO dto = QuestionDTO.builder()
                .questionType("test")
                .title("Invalid Question")
                .text("Question without correct answer")
                .defaultScore(2.0)
                .courseName("Java Programming")
                .majorName("Computer")
                .options(Arrays.asList(option1, option2)).build();

        mockMvc.perform(post("/api/question")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void testCreateQuestion_shouldReject_courseOrMajorNotFound() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        QuestionDTO dto = QuestionDTO.builder()
                .questionType("essay")
                .title("Question")
                .text("Question text")
                .defaultScore(3.0)
                .courseName("NonExistent Course")
                .majorName("NonExistent Major").build();

        mockMvc.perform(post("/api/question")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateQuestion_shouldReject_InvalidData() throws Exception {
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

        QuestionDTO dto = QuestionDTO.builder()
                .questionType("essay")
                .title("AB")
                .text("Question text")
                .defaultScore(-1.0)
                .courseName("Java Programming")
                .majorName("Computer").build();

        mockMvc.perform(post("/api/question")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

    }

    @Test
    void testCreateQuestion_shouldReturn403_nonTeacherUser() throws Exception {
        token = loginAndGetToken("admin", "admin");

        QuestionDTO dto = QuestionDTO.builder()
        .questionType("essay")
        .title("Test")
        .text("Question text")
        .defaultScore(2.0)
        .courseName("Any Course")
        .majorName("Any Major").build();

        mockMvc.perform(post("/api/question")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAssignQuestionToExam() throws Exception {
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

        Exam exam = createExam("Midterm Exam", savedOfferedCourse);
        Question question = createQuestion("Sample Question for Assignment", course);

        ExamQuestionDTO dto = ExamQuestionDTO.builder()
                .examId(exam.getId())
                .questionId(question.getId())
                .score(7.5).build();

        mockMvc.perform(post("/api/question/assign-exam")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        ExamQuestion savedExamQuestion = examQuestionRepository.findByExamIdAndQuestionId(exam.getId(), question.getId()).orElse(null);
        assertThat(savedExamQuestion).isNotNull();
        assertThat(savedExamQuestion.getScore()).isEqualTo(7.5);

        Exam updatedExam = examRepository.findById(exam.getId()).orElse(null);
        assertThat(updatedExam).isNotNull();
    }

    @Test
    void testAssignQuestionToExam_shouldReturn404_examNotFound() throws Exception {
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

        Question question = createQuestion("Question for Missing Exam", course);

        ExamQuestionDTO dto = ExamQuestionDTO.builder()
                .examId(9999L)
                .questionId(question.getId())
                .score(5.0).build();

        mockMvc.perform(post("/api/question/assign-exam")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAssignQuestionToExam_shouldReturn404_questionNotFound() throws Exception {
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

        Exam exam = createExam("Exam for Missing Question", savedOfferedCourse);

        ExamQuestionDTO dto = ExamQuestionDTO.builder()
                .examId(exam.getId())
                .questionId(999999L)
                .score(5.0).build();

        mockMvc.perform(post("/api/question/assign-exam")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAssignQuestionToExam_shouldReturn400_scoreIsNegative() throws Exception {
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

        Exam exam = createExam("Negative Score Exam", savedOfferedCourse);
        Question question = createQuestion("Question for Negative Score", course);

        ExamQuestionDTO dto = ExamQuestionDTO.builder()
                .examId(exam.getId())
                .questionId(question.getId())
                .score(-1.0).build();

        mockMvc.perform(post("/api/question/assign-exam")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllQuestionsOfAExam() throws Exception {
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

        Exam exam = createExam("Midterm Exam", savedOfferedCourse);

        Question question1 = createQuestion("Question1", course);
        Question question2 = createQuestion("Question2", course);

        assignQuestionToExam(exam.getId(), question1.getId(), 10.0);
        assignQuestionToExam(exam.getId(), question2.getId(), 15.0);

        String response = mockMvc.perform(get("/api/question/exam-questions/" + exam.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Question1"))
                .andExpect(jsonPath("$[1].title").value("Question2"))
                .andExpect(jsonPath("$[0].text").value("Sample question text for Question1"))
                .andExpect(jsonPath("$[1].text").value("Sample question text for Question2"))
                .andExpect(jsonPath("$[0].defaultScore").value(5.0)) // Default score from creation
                .andExpect(jsonPath("$[1].defaultScore").value(5.0))
                .andExpect(jsonPath("$[0].courseName").value("Java Programming"))
                .andExpect(jsonPath("$[1].courseName").value("Java Programming"))
                .andExpect(jsonPath("$[0].majorName").value("Computer"))
                .andExpect(jsonPath("$[1].majorName").value("Computer"))
                .andReturn().getResponse().getContentAsString();

        List<QuestionDTO> responseDtoList = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionDTO.class));

        assertThat(responseDtoList).hasSize(2);
        assertThat(responseDtoList).extracting(QuestionDTO::getTitle)
                .containsExactlyInAnyOrder("Question1", "Question2");
        assertThat(responseDtoList).extracting(QuestionDTO::getText)
                .containsExactlyInAnyOrder("Sample question text for Question1", "Sample question text for Question2");
    }

    @Test
    void testGetAllQuestionsOfAExam_shouldReturnEmptyList_examHasNoQuestions() throws Exception {
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

        Exam exam = createExam("Empty Exam", savedOfferedCourse);

        String response = mockMvc.perform(get("/api/question/exam-questions/" + exam.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0))
                .andReturn().getResponse().getContentAsString();

        List<QuestionDTO> responseDtoList = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionDTO.class));

        assertThat(responseDtoList).isEmpty();
    }

    @Test
    void testGetAllQuestionsOfAExam_shouldReturn404_examNotFound() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();
        Major cMajor = majorRepository.findByName("Computer").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        mockMvc.perform(get("/api/question/exam-questions/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllQuestionsOfAExam_shouldReturn404_examIsDeleted() throws Exception {
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

        Exam exam = createExam("Deleted Exam", savedOfferedCourse);
        exam.setDeleted(true);
        examRepository.save(exam);

        mockMvc.perform(get("/api/question/exam-questions/" + exam.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllQuestionsOfACourse() throws Exception {
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

        Exam exam = createExam("Midterm Exam", savedOfferedCourse);

        Question question1 = createQuestion("Question1", course);
        Question question2 = createQuestion("Question2", course);

        assignQuestionToExam(exam.getId(), question1.getId(), 10.0);
        assignQuestionToExam(exam.getId(), question2.getId(), 15.0);

        String response = mockMvc.perform(get("/api/question/course-questions/" + course.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Question1"))
                .andExpect(jsonPath("$[1].title").value("Question2"))
                .andExpect(jsonPath("$[0].text").value("Sample question text for Question1"))
                .andExpect(jsonPath("$[1].text").value("Sample question text for Question2"))
                .andExpect(jsonPath("$[0].defaultScore").value(5.0))
                .andExpect(jsonPath("$[1].defaultScore").value(5.0))
                .andExpect(jsonPath("$[0].courseName").value("Java Programming"))
                .andExpect(jsonPath("$[1].courseName").value("Java Programming"))
                .andExpect(jsonPath("$[0].majorName").value("Computer"))
                .andExpect(jsonPath("$[1].majorName").value("Computer"))
                .andReturn().getResponse().getContentAsString();

        List<QuestionDTO> responseDtoList = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionDTO.class));

        assertThat(responseDtoList).hasSize(2);
        assertThat(responseDtoList).extracting(QuestionDTO::getTitle)
                .containsExactlyInAnyOrder("Question1", "Question2");
        assertThat(responseDtoList).extracting(QuestionDTO::getText)
                .containsExactlyInAnyOrder("Sample question text for Question1", "Sample question text for Question2");
        assertThat(responseDtoList).extracting(QuestionDTO::getDefaultScore)
                .containsExactlyInAnyOrder(5.0, 5.0);
    }

    @Test
    void testGetAllQuestionsOfACourse_shouldReturnEmptyList_courseHasNoQuestions() throws Exception {
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

        String response = mockMvc.perform(get("/api/question/course-questions/" + course.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0))
                .andReturn().getResponse().getContentAsString();

        List<QuestionDTO> responseDtoList = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionDTO.class));

        assertThat(responseDtoList).isEmpty();
    }

    @Test
    void testGetAllQuestionsOfACourse_shouldReturn404_CourseNotFound() throws Exception {
        registerPerson();
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow();

        Person person = personRepository.findByAccountUsername("ali_teacher").orElseThrow();
        Account account = person.getAccount();
        account.setActiveRole(teacherRole);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        token = loginAndGetToken("ali_teacher", "mySecretPass123");

        mockMvc.perform(get("/api/question/course-questions/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
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

    private Exam createExam(String title, OfferedCourse course) {
        Exam exam = Exam.builder()
                .title(title)
                .startTime(LocalDateTime.now().plusDays(5))
                .endTime(LocalDateTime.now().plusDays(6))
                .score(5.0)
                .examState(ExamState.NOT_STARTED)
                .offeredCourse(course).build();
        return examRepository.save(exam);
    }

    private Question createQuestion(String title, Course course) {
        QuestionService questionService = webApplicationContext.getBean(ir.bahman.academic_lms.service.QuestionService.class);
        QuestionDTO dto = QuestionDTO.builder()
                .questionType("essay")
                .title(title)
                .text("Sample question text for " + title)
                .defaultScore(5.0)
                .courseName(course.getTitle())
                .majorName(course.getMajor().getName()).build();

        return questionService.create("essay", questionMapper.toEntity(dto), List.of());
    }

    private void assignQuestionToExam(Long examId, Long questionId, Double score) {
        QuestionService questionService = webApplicationContext.getBean(ir.bahman.academic_lms.service.QuestionService.class);
        questionService.assignQuestionToExam(examId, questionId, score);
    }
}