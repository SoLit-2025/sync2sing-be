package com.solit.sync2sing.domain.training.common.service;

import com.solit.sync2sing.domain.training.common.dto.CurriculumListResponse;
import com.solit.sync2sing.domain.training.common.dto.GenerateCurriculumRequest;
import com.solit.sync2sing.entity.Training;
import com.solit.sync2sing.global.security.CustomUserDetails;
import com.solit.sync2sing.global.type.TrainingGrade;
import com.solit.sync2sing.repository.TrainingRepository;
import org.assertj.core.util.VisibleForTesting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.solit.sync2sing.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class TrainingServiceTest {

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private TrainingRepository trainingRepository;

    @BeforeEach
    void setUp() {
        CustomUserDetails mockUser = createForTest();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @VisibleForTesting
    public static CustomUserDetails createForTest() {
        final String USER_ROLE = "USER";

        User testUser = new User();
        testUser.setUsername("baduck@example.com");
        testUser.setPassword("testPassword");
        testUser.setRoles(Collections.singletonList(USER_ROLE));
        testUser.setNickname("테스트사용자");
        testUser.setGender("FEMALE");
        testUser.setAge(20);
        testUser.setVoiceType("SOPRANO");
        testUser.setVoiceRange("C3~G5");
        testUser.setDuetPenaltyCount(0);

        return new CustomUserDetails(testUser);
    }


    @Test
    void generateTrainingCurriculum_shouldReturnExpectedResponse() {
        // given
        Long userId = 1L;

        // 샘플 Training 데이터 삽입
        Training t1 = new Training();
        t1.setCategory("PITCH");
        t1.setTitle("음정 안정 훈련");
        t1.setGrade("MEDIUM");
        t1.setDescription("기초 음정 정확도를 높이기 위한 훈련입니다.");
        t1.setTrainingMinutes(5);

        Training t2 = new Training();
        t2.setCategory("PITCH");
        t2.setTitle("세밀한 음정 조절 연습");
        t2.setGrade("MEDIUM");
        t2.setDescription("반음 단위의 정확한 음정 조절 능력을 향상시킵니다.");
        t2.setTrainingMinutes(5);

        Training t3 = new Training();
        t3.setCategory("RHYTHM");
        t3.setTitle("리듬 감각 강화 훈련");
        t3.setGrade("HIGH");
        t3.setDescription("박자 감각을 높이는 훈련입니다.");
        t3.setTrainingMinutes(5);

        Training t4 = new Training();
        t4.setCategory("RHYTHM");
        t4.setTitle("박자 일관성 연습");
        t4.setGrade("HIGH");
        t4.setDescription("일관된 템포 유지 훈련입니다.");
        t4.setTrainingMinutes(5);

        Training t5 = new Training();
        t5.setCategory("VOCALIZATION");
        t5.setTitle("발성 기본 자세 익히기");
        t5.setGrade("LOW");
        t5.setDescription("기초 자세 훈련입니다.");
        t5.setTrainingMinutes(5);

        Training t6 = new Training();
        t6.setCategory("VOCALIZATION");
        t6.setTitle("강약 조절 발성 연습");
        t6.setGrade("LOW");
        t6.setDescription("발성 표현력 향상 훈련입니다.");
        t6.setTrainingMinutes(5);

        Training t7 = new Training();
        t7.setCategory("BREATH");
        t7.setTitle("효율적 호흡 관리 훈련");
        t7.setGrade("MEDIUM");
        t7.setDescription("호흡 조절 능력 강화 훈련입니다.");
        t7.setTrainingMinutes(5);

        Training t8 = new Training();
        t8.setCategory("BREATH");
        t8.setTitle("호흡 지속력 강화 연습");
        t8.setGrade("MEDIUM");
        t8.setDescription("호흡 유지력 강화 훈련입니다.");
        t8.setTrainingMinutes(5);

        trainingRepository.saveAll(List.of(t1, t2, t3, t4, t5, t6, t7, t8));

        // userTrainingLogRepository는 테스트할 때 trainedIds를 비워둠 (모든 훈련 미수행 상태로 가정)

        // when
        GenerateCurriculumRequest request = GenerateCurriculumRequest
                .builder()
                .pitch(TrainingGrade.MEDIUM)
                .rhythm(TrainingGrade.HIGH)
                .vocalization(TrainingGrade.LOW)
                .breath(TrainingGrade.MEDIUM)
                .trainingDays(7)
                .build();

        // 로그인 구현 전이라면 userId를 주입하는 방식으로 테스트용 메서드 수정 필요
        CurriculumListResponse response = trainingService.generateTrainingCurriculum(request);

        // then
        assertEquals(2, response.getPitch().size());
        assertEquals(2, response.getRhythm().size());
        assertEquals(2, response.getVocalization().size());
        assertEquals(2, response.getBreath().size());

        assertEquals("음정 안정 훈련", response.getPitch().get(0).getTitle());
        assertEquals("리듬 감각 강화 훈련", response.getRhythm().get(0).getTitle());
    }
}
