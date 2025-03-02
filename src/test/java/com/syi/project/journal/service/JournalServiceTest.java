package com.syi.project.journal.service;
/*
//
//import com.syi.project.auth.entity.Member;
//import com.syi.project.auth.repository.MemberRepository;
//import com.syi.project.common.entity.Criteria;
//import com.syi.project.common.enums.Role;
//import com.syi.project.course.entity.Course;
//import com.syi.project.journal.dto.JournalResponseDTO;
//import com.syi.project.journal.entity.Journal;
//import com.syi.project.journal.repository.JournalRepository;
//import java.time.LocalDate;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.lang.reflect.Constructor;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.when;
//
//class JournalServiceTest {
//
//  @Mock
//  private JournalRepository journalRepository;
//
//  @Mock
//  private MemberRepository memberRepository;
//
//  @InjectMocks
//  private JournalService journalService;
//
//  private Member testMember;
//  private Course testCourse;
//  private Criteria criteria;
//
//  private <T> T createInstance(Class<T> clazz) {
//    try {
//      Constructor<T> constructor = clazz.getDeclaredConstructor();
//      constructor.setAccessible(true);
//      return constructor.newInstance();
//    } catch (Exception e) {
//      throw new RuntimeException("Failed to create instance", e);
//    }
//  }
//
//  @BeforeEach
//  void setUp() {
//    MockitoAnnotations.openMocks(this);
//
//    // Course 인스턴스 생성
//    testCourse = createInstance(Course.class);
//    ReflectionTestUtils.setField(testCourse, "id", 1L);
//
//    // Member 인스턴스 생성
//    testMember = createInstance(Member.class);
//    ReflectionTestUtils.setField(testMember, "id", 1L);
//    ReflectionTestUtils.setField(testMember, "username", "testuser");
//    ReflectionTestUtils.setField(testMember, "name", "홍길동");
//    ReflectionTestUtils.setField(testMember, "role", Role.STUDENT);
//
//    // 기본 검색 조건 설정
//    criteria = new Criteria();
//    criteria.setPageNum(1);
//    criteria.setAmount(20);
//  }
//
//  @Test
//  @DisplayName("기본 페이징 처리 검증")
//  void verifyBasicPaging() {
//    // given
//    List<Journal> journals = new ArrayList<>();
//    for(int i = 0; i < 20; i++) {
//      Journal journal = createInstance(Journal.class);
//      ReflectionTestUtils.setField(journal, "id", (long)i);
//      ReflectionTestUtils.setField(journal, "title", "교육일지 " + i);
//      ReflectionTestUtils.setField(journal, "content", "내용 " + i);
//      ReflectionTestUtils.setField(journal, "member", testMember);
//      ReflectionTestUtils.setField(journal, "course", testCourse);
//      journals.add(journal);
//    }
//
//    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
//    when(journalRepository.findAllWithConditions(any(), anyLong(), any(), any()))
//        .thenReturn(new PageImpl<>(journals, criteria.getPageable(), 50));
//
//    // when
//    Page<JournalResponseDTO> result = journalService.searchJournals(
//        criteria,
//        testMember.getId(),
//        null,
//        null
//    );
//
//    // then
//    assertEquals(1, result.getNumber() + 1);
//    assertEquals(20, result.getSize());
//    assertEquals(50, result.getTotalElements());
//    assertEquals(3, result.getTotalPages());
//    assertTrue(result.hasNext());
//    assertFalse(result.hasPrevious());
//    assertEquals(20, result.getContent().size());
//  }
//
//  // JournalServiceTest.java에 다음 테스트 메소드들을 추가
//
//  @Test
//  @DisplayName("다음 페이지 조회 검증")
//  void verifyNextPage() {
//    // given
//    criteria.setPageNum(2);  // 2페이지 요청
//
//    List<Journal> journals = new ArrayList<>();
//    for(int i = 20; i < 40; i++) {  // 21~40번 데이터
//      Journal journal = createInstance(Journal.class);
//      ReflectionTestUtils.setField(journal, "id", (long)i);
//      ReflectionTestUtils.setField(journal, "title", "교육일지 " + i);
//      ReflectionTestUtils.setField(journal, "content", "내용 " + i);
//      ReflectionTestUtils.setField(journal, "member", testMember);
//      ReflectionTestUtils.setField(journal, "course", testCourse);
//      journals.add(journal);
//    }
//
//    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
//    when(journalRepository.findAllWithConditions(any(), anyLong(), any(), any()))
//        .thenReturn(new PageImpl<>(journals, criteria.getPageable(), 50));
//
//    // when
//    Page<JournalResponseDTO> result = journalService.searchJournals(
//        criteria,
//        testMember.getId(),
//        null,
//        null
//    );
//
//    // then
//    assertEquals(2, result.getNumber() + 1);  // 현재 페이지 번호
//    assertEquals(20, result.getSize());       // 페이지 크기
//    assertTrue(result.hasNext());             // 다음 페이지 존재
//    assertTrue(result.hasPrevious());         // 이전 페이지 존재
//  }
//
//  @Test
//  @DisplayName("작성자 이름으로 검색")
//  void searchByWriterName() {
//    // given
//    criteria.setType("W");
//    criteria.setKeyword("홍길동");
//
//    List<Journal> journals = new ArrayList<>();
//    Journal journal = createInstance(Journal.class);
//    ReflectionTestUtils.setField(journal, "id", 1L);
//    ReflectionTestUtils.setField(journal, "title", "테스트 교육일지");
//    ReflectionTestUtils.setField(journal, "content", "테스트 내용");
//    ReflectionTestUtils.setField(journal, "member", testMember);
//    ReflectionTestUtils.setField(journal, "course", testCourse);
//    journals.add(journal);
//
//    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
//    when(journalRepository.findAllWithConditions(any(), anyLong(), any(), any()))
//        .thenReturn(new PageImpl<>(journals));
//
//    // when
//    Page<JournalResponseDTO> result = journalService.searchJournals(
//        criteria,
//        testMember.getId(),
//        null,
//        null
//    );
//
//    // then
//    assertEquals(1, result.getTotalElements());
//    assertEquals("테스트 교육일지", result.getContent().get(0).getTitle());
//    assertEquals("홍길동", result.getContent().get(0).getMemberName());
//  }
//
//  @Test
//  @DisplayName("날짜 범위로 검색")
//  void searchByDateRange() {
//    // given
//    LocalDate startDate = LocalDate.now().minusDays(7);
//    LocalDate endDate = LocalDate.now();
//
//    List<Journal> journals = new ArrayList<>();
//    Journal journal = createInstance(Journal.class);
//    ReflectionTestUtils.setField(journal, "id", 1L);
//    ReflectionTestUtils.setField(journal, "title", "날짜 테스트 교육일지");
//    ReflectionTestUtils.setField(journal, "content", "테스트 내용");
//    ReflectionTestUtils.setField(journal, "member", testMember);
//    ReflectionTestUtils.setField(journal, "course", testCourse);
//    journals.add(journal);
//
//    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
//    when(journalRepository.findAllWithConditions(any(), anyLong(), any(), any()))
//        .thenReturn(new PageImpl<>(journals));
//
//    // when
//    Page<JournalResponseDTO> result = journalService.searchJournals(
//        criteria,
//        testMember.getId(),
//        startDate,
//        endDate
//    );
//
//    // then
//    assertEquals(1, result.getTotalElements());
//    assertEquals("날짜 테스트 교육일지", result.getContent().get(0).getTitle());
//  }
//
//  @Test
//  @DisplayName("잘못된 날짜 범위로 검색 시 예외 발생")
//  void searchWithInvalidDateRange() {
//    // given
//    LocalDate startDate = LocalDate.now();
//    LocalDate endDate = LocalDate.now().minusDays(7);  // 시작일이 종료일보다 늦음
//
//    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
//
//    // when & then
//    assertThrows(IllegalArgumentException.class, () ->
//        journalService.searchJournals(
//            criteria,
//            testMember.getId(),
//            startDate,
//            endDate
//        )
//    );
//  }
//
//  @Test
//  @DisplayName("존재하지 않는 회원으로 검색 시 예외 발생")
//  void searchWithNonExistentMember() {
//    // given
//    when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());
//
//    // when & then
//    assertThrows(IllegalArgumentException.class, () ->
//        journalService.searchJournals(
//            criteria,
//            999L,
//            null,
//            null
//        )
//    );
//  }
//}

import static org.junit.jupiter.api.Assertions.*;

import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.common.entity.Criteria;
import com.syi.project.common.enums.CourseStatus;
import com.syi.project.common.enums.Role;
import com.syi.project.course.entity.Course;
import com.syi.project.course.repository.CourseRepository;
import com.syi.project.enroll.entity.Enroll;
import com.syi.project.enroll.repository.EnrollRepository;
import com.syi.project.file.entity.File;
import com.syi.project.file.service.FileService;
import com.syi.project.journal.dto.JournalRequestDTO;
import com.syi.project.journal.dto.JournalResponseDTO;
import com.syi.project.journal.entity.Journal;
import com.syi.project.journal.entity.JournalFile;
import com.syi.project.journal.repository.JournalRepository;
import com.syi.project.journal.service.JournalService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@Transactional
class JournalServiceTest {
  @Autowired
  private JournalService journalService;
  @Autowired
  private JournalRepository journalRepository;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private CourseRepository courseRepository;
  @Autowired
  private EnrollRepository enrollRepository;
  @Autowired
  private FileService fileService;

  private Member testMember;
  private Course testCourse;
  private MockMultipartFile testFile;

  @BeforeEach
  void setUp() {
    // 테스트 수강생 생성
    testMember = new Member(
        "test_student",      // username
        "password123",       // password
        "테스트 수강생",        // name
        "2000-01-01",       // birthday
        "test@test.com",    // email
        Role.STUDENT        // role
    );
    testMember = memberRepository.save(testMember);

    // 테스트 과정 생성
    testCourse = new Course(
        "테스트 과정",          // name
        "테스트 과정 설명",      // description
        "테스트 관리자",        // adminName
        "테스트 강사",         // teacherName
        LocalDate.of(2024, 1, 1),  // startDate
        LocalDate.of(2024, 12, 31), // endDate
        "301",               // roomName
        LocalDate.now(),     // enrollDate
        LocalDate.now(),     // modifiedDate
        CourseStatus.Y,      // status
        null,               // deletedBy
        1L                  // adminId
    );
    testCourse = courseRepository.save(testCourse);

    // 테스트 파일 생성
    testFile = new MockMultipartFile(
        "file",
        "test.docx",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "테스트 파일 내용".getBytes()
    );

    // 수강 신청 생성
    Enroll enroll = new Enroll(testCourse.getId(), testMember.getId());
    enrollRepository.save(enroll);
  }

  @Test
  @DisplayName("교육일지 생성 - 정상 케이스")
  void createJournal_Success() {
    // given
    LocalDate validEducationDate = LocalDate.of(2024, 3, 15);
    JournalRequestDTO.Create requestDTO = JournalRequestDTO.Create.builder()
        .courseId(testCourse.getId())
        .title("테스트 교육일지")
        .content("테스트 내용")
        .educationDate(validEducationDate)
        .file(testFile)
        .build();

    // when
    JournalResponseDTO responseDTO = journalService.createJournal(testMember.getId(), requestDTO);

    // then
    assertNotNull(responseDTO);
    assertEquals("테스트 교육일지", responseDTO.getTitle());
    assertEquals(validEducationDate, responseDTO.getEducationDate());
  }

  @Test
  @DisplayName("교육일지 생성 실패 - 과정 기간 외 날짜")
  void createJournal_InvalidDate() {
    // given
    LocalDate invalidDate = LocalDate.of(2023, 12, 31); // 과정 시작일 이전
    JournalRequestDTO.Create requestDTO = JournalRequestDTO.Create.builder()
        .courseId(testCourse.getId())
        .title("테스트 교육일지")
        .content("테스트 내용")
        .educationDate(invalidDate)
        .file(testFile)
        .build();

    // when & then
    assertThrows(IllegalArgumentException.class, () ->
        journalService.createJournal(testMember.getId(), requestDTO));
  }

  @Test
  @DisplayName("교육일지 검색 - 교육일자 기준")
  void searchJournals_ByEducationDate() {
    // given
    LocalDate searchStartDate = LocalDate.of(2024, 3, 1);
    LocalDate searchEndDate = LocalDate.of(2024, 3, 31);

    // 테스트용 교육일지 3개 생성 (3월 데이터)
    createTestJournal(LocalDate.of(2024, 3, 10));
    createTestJournal(LocalDate.of(2024, 3, 15));
    createTestJournal(LocalDate.of(2024, 3, 20));
    // 범위 밖 데이터 1개 생성
    createTestJournal(LocalDate.of(2024, 4, 1));

    // when
    Page<JournalResponseDTO> results = journalService.getStudentJournals(
        testMember.getId(),
        testCourse.getId(),
        searchStartDate,
        searchEndDate,
        new Criteria()
    );

    // then
    assertEquals(3, results.getTotalElements());
  }

  @Test
  @DisplayName("교육일지 수정 - 교육일자 수정")
  void updateJournal_EducationDate() {
    // given
    Journal journal = createTestJournal(LocalDate.of(2024, 3, 1));
    LocalDate newEducationDate = LocalDate.of(2024, 3, 15);

    JournalRequestDTO.Update requestDTO = JournalRequestDTO.Update.builder()
        .title("수정된 제목")
        .content("수정된 내용")
        .educationDate(newEducationDate)
        .build();

    // when
    JournalResponseDTO updatedJournal = journalService.updateJournal(
        testMember.getId(),
        journal.getId(),
        requestDTO
    );

    // then
    assertEquals(newEducationDate, updatedJournal.getEducationDate());
  }

  @Test
  @DisplayName("교육일지 수정 실패 - 중복된 교육일자")
  void updateJournal_DuplicateDate() {
    // given
    createTestJournal(LocalDate.of(2024, 3, 1)); // 첫 번째 교육일지
    Journal secondJournal = createTestJournal(LocalDate.of(2024, 3, 2)); // 두 번째 교육일지

    JournalRequestDTO.Update requestDTO = JournalRequestDTO.Update.builder()
        .title("수정된 제목")
        .content("수정된 내용")
        .educationDate(LocalDate.of(2024, 3, 1)) // 첫 번째 교육일지와 같은 날짜로 수정 시도
        .build();

    // when & then
    assertThrows(IllegalArgumentException.class, () ->
        journalService.updateJournal(testMember.getId(), secondJournal.getId(), requestDTO));
  }

  private Journal createTestJournal(LocalDate educationDate) {
    // 테스트 파일 생성
    MultipartFile testFile = new MockMultipartFile(
        "file",
        "test.docx",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "테스트 파일 내용".getBytes()
    );

    Journal journal = Journal.builder()
        .member(testMember)
        .course(testCourse)
        .title("테스트 교육일지")
        .content("테스트 내용")
        .educationDate(educationDate)
        .build();

    File savedFile = fileService.uploadFile(testFile, "journals", testMember);
    journal.setFile(JournalFile.builder()
        .journal(journal)
        .file(savedFile)
        .build());

    return journalRepository.save(journal);
  }
}
*/