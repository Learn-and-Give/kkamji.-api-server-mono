package com.kkamjidot.api.mono.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(name = "로그인 응답")
public class LoginResponse implements Serializable {
    @Schema(description = "회원 ID")
    private final Long userId;
}
