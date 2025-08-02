package com.solit.sync2sing.global.chatgpt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SoloPostResponse {
    private String overallReviewTitle;
    private String overallReviewContent;
    private String feedbackTitle;
    private String feedbackContent;
}
