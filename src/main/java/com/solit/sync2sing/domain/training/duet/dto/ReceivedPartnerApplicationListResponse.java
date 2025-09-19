package com.solit.sync2sing.domain.training.duet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivedPartnerApplicationListResponse {

    private List<ReceivedPartnerApplicationListResponse.ApplicationDTO> applicationList;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApplicationDTO {
        private Long id;
        private Long applicantId;
        private String applicantNickname;
        private LocalDateTime requestedAt;
    }

}
