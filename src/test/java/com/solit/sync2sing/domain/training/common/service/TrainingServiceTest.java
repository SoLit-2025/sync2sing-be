package com.solit.sync2sing.domain.training.common.service;

import com.solit.sync2sing.domain.training.common.dto.CurriculumListResponse;
import com.solit.sync2sing.domain.training.common.dto.GenerateCurriculumRequest;
import com.solit.sync2sing.entity.Training;
import com.solit.sync2sing.global.security.CustomUserDetails;
import com.solit.sync2sing.global.type.Gender;
import com.solit.sync2sing.global.type.TrainingCategory;
import com.solit.sync2sing.global.type.TrainingGrade;
import com.solit.sync2sing.global.type.VoiceType;
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

    CustomUserDetails mockUser;

    @BeforeEach
    void setUp() {
        mockUser = createForTest();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @VisibleForTesting
    public static CustomUserDetails createForTest() {
        final String USER_ROLE = "USER";

        User testUser = User.builder()
                .username("baduck@example.com")
                .password("testPassword")
                .roles(Collections.singletonList(USER_ROLE))
                .nickname("테스트사용자")
                .gender(Gender.FEMALE)
                .age(20)
                .voiceType(VoiceType.SOPRANO)
                .pitchNoteMin("C3")
                .pitchNoteMax("G5")
                .duetPenaltyCount(0)
                .build();

        return new CustomUserDetails(testUser);
    }


    @Test
    void generateTrainingCurriculum_shouldReturnExpectedResponse() {
        // given
        Long userId = 1L;

        // 샘플 Training 데이터 삽입
        Training t1 = Training.builder()
                .category(TrainingCategory.PITCH)
                .title("음정 안정 훈련")
                .grade(TrainingGrade.MEDIUM)
                .description("기초 음정 정확도를 높이기 위한 훈련입니다.")
                .trainingMinutes(5)
                .build();

        Training t2 = Training.builder()
                .category(TrainingCategory.PITCH)
                .title("세밀한 음정 조절 연습")
                .grade(TrainingGrade.MEDIUM)
                .description("반음 단위의 정확한 음정 조절 능력을 향상시킵니다.")
                .trainingMinutes(5)
                .build();

        Training t3 = Training.builder()
                .category(TrainingCategory.RHYTHM)
                .title("리듬 감각 강화 훈련")
                .grade(TrainingGrade.HIGH)
                .description("박자 감각을 높이는 훈련입니다.")
                .trainingMinutes(5)
                .build();

        Training t4 = Training.builder()
                .category(TrainingCategory.RHYTHM)
                .title("박자 일관성 연습")
                .grade(TrainingGrade.HIGH)
                .description("일관된 템포 유지 훈련입니다.")
                .trainingMinutes(5)
                .build();

        Training t5 = Training.builder()
                .category(TrainingCategory.VOCALIZATION)
                .title("발성 기본 자세 익히기")
                .grade(TrainingGrade.LOW)
                .description("기초 자세 훈련입니다.")
                .trainingMinutes(5)
                .build();

        Training t6 = Training.builder()
                .category(TrainingCategory.VOCALIZATION)
                .title("강약 조절 발성 연습")
                .grade(TrainingGrade.LOW)
                .description("발성 표현력 향상 훈련입니다.")
                .trainingMinutes(5)
                .build();

        Training t7 = Training.builder()
                .category(TrainingCategory.BREATH)
                .title("효율적 호흡 관리 훈련")
                .grade(TrainingGrade.MEDIUM)
                .description("호흡 조절 능력 강화 훈련입니다.")
                .trainingMinutes(5)
                .build();

        Training t8 = Training.builder()
                .category(TrainingCategory.BREATH)
                .title("호흡 지속력 강화 연습")
                .grade(TrainingGrade.MEDIUM)
                .description("호흡 유지력 강화 훈련입니다.")
                .trainingMinutes(5)
                .build();

        trainingRepository.saveAll(List.of(t1, t2, t3, t4, t5, t6, t7, t8));

        // userTrainingLogRepository는 테스트할 때 trainedIds를 비워둠 (모든 훈련 미수행 상태로 가정)

        // when
        GenerateCurriculumRequest request = GenerateCurriculumRequest
                .builder()
                .pitch(String.valueOf(TrainingGrade.MEDIUM))
                .rhythm(String.valueOf(TrainingGrade.HIGH))
                .vocalization(String.valueOf(TrainingGrade.LOW))
                .breath(String.valueOf(TrainingGrade.MEDIUM))
                .trainingDays(7)
                .build();

        // 로그인 구현 전이라면 userId를 주입하는 방식으로 테스트용 메서드 수정 필요
        CurriculumListResponse response = trainingService.generateTrainingCurriculum(mockUser, request);

        // then
        assertEquals(2, response.getPitch().size());
        assertEquals(2, response.getRhythm().size());
        assertEquals(2, response.getVocalization().size());
        assertEquals(2, response.getBreath().size());

        assertEquals("음정 안정 훈련", response.getPitch().get(0).getTitle());
        assertEquals("리듬 감각 강화 훈련", response.getRhythm().get(0).getTitle());
    }
}
