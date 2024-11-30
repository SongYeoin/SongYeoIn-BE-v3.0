//package com.syi.project.journal.service;
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