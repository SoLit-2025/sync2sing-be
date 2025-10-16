package com.solit.sync2sing.domain.user.service.impl;

import com.solit.sync2sing.domain.user.dto.request.SignupRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.SignupResponseDTO;
import com.solit.sync2sing.entity.User;
import com.solit.sync2sing.repository.UserRepository;
import com.solit.sync2sing.domain.user.service.UserSignupService;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.type.Gender;
import com.solit.sync2sing.global.type.VoiceType;
import com.solit.sync2sing.repository.VocalAnalysisReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SignupServiceImplementation implements UserSignupService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VocalAnalysisReportRepository vocalAnalysisReportRepository;

    @Autowired
    public SignupServiceImplementation(UserRepository userRepository, PasswordEncoder passwordEncoder, VocalAnalysisReportRepository vocalAnalysisReportRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.vocalAnalysisReportRepository = vocalAnalysisReportRepository;
    }

    @Override
    @Transactional
    public SignupResponseDTO signUp(SignupRequestDTO requestDTO) {
        validateRequest(requestDTO);

        String encodedPassword = passwordEncoder.encode(requestDTO.getPassword());

        User userEntity = User.builder()
                .username(requestDTO.getUsername())
                .password(encodedPassword)
                .nickname(requestDTO.getNickname())
                .gender(requestDTO.getGender())
                .age(requestDTO.getAge())
                .pitchNoteMin(requestDTO.getPitchNoteMin())
                .pitchNoteMax(requestDTO.getPitchNoteMax())
                .voiceType(requestDTO.getVoiceType())
                .duetPenaltyCount(0)
                .duetPenaltyUntil(null)
                .roles(List.of("USER"))
                .build();

        try {
            userRepository.save(userEntity);

            Long reportId = requestDTO.getReportId();
            if (reportId != null){
                vocalAnalysisReportRepository.findById(reportId)
                        .ifPresent(report ->{
                            if(report.getUser() == null) {
                                report.setUser(userEntity);
                            }
                        });
            }
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    ResponseCode.DUPLICATE_USERNAME.getStatus(),
                    ResponseCode.DUPLICATE_USERNAME.getMessage()
            );
        }

        return SignupResponseDTO.builder()
                .username(userEntity.getUsername())
                .nickname(userEntity.getNickname())
                .gender(userEntity.getGender())
                .age(userEntity.getAge())
                .pitchNoteMin(userEntity.getPitchNoteMin())
                .pitchNoteMax(userEntity.getPitchNoteMax())
                .voiceType(userEntity.getVoiceType())
                .duetPenaltyCount(userEntity.getDuetPenaltyCount())
                .duetPenaltyUntil(userEntity.getDuetPenaltyUntil())
                .build();
    }


    private void validateRequest(SignupRequestDTO requestDTO) {
        String username = requestDTO.getUsername();
        if (requestDTO.getUsername() == null || requestDTO.getPassword() == null || requestDTO.getNickname() == null) {
            throw new ResponseStatusException(
                    ResponseCode.SIGNUP_REQUIRED_FIELDS.getStatus(),
                    ResponseCode.SIGNUP_REQUIRED_FIELDS.getMessage()
            );
        }
        if (username.length() < 6 || username.length() >= 15 || !username.matches("^[a-z0-9]+$")) {
            throw new ResponseStatusException(
                    ResponseCode.INVALID_USERNAME_FORMAT.getStatus(), // INVALID_USERNAME_FORMAT 등 새 코드 추가 권장
                    ResponseCode.INVALID_USERNAME_FORMAT.getMessage()
            );
        }
        if (userRepository.existsByUsername(requestDTO.getUsername())) {
            throw new ResponseStatusException(
                    ResponseCode.DUPLICATE_USERNAME.getStatus(),
                    ResponseCode.DUPLICATE_USERNAME.getMessage()
            );
        }
    }
}