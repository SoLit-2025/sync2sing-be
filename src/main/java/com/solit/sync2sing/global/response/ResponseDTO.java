package com.solit.sync2sing.global.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

import java.util.Collections;

@Getter
@JsonPropertyOrder({"status", "message", "data"})
public class ResponseDTO<T> {

    private final Integer status;
    private final String message;
    private final T data;

    public ResponseDTO(ResponseCode responseCode, T data) {
        this.status = responseCode.getStatus().value();
        this.message = responseCode.getMessage();
        this.data = data;
    }

    public ResponseDTO(ResponseCode responseCode) {
        this(responseCode, (T) Collections.emptyMap());  // 빈 Map을 data로 전달
    }
}