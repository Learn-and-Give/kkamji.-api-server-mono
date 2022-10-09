package com.kkamjidot.api.mono.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@ToString
@NoArgsConstructor
@Schema(name = "로그인 요청")
public class LoginRequestV2 implements Serializable {
    @Schema(description = "이메일", example = "kkamjidot@gmial.com", required = true)
    @NotNull(message = "이메일은 필수 입력 값입니다.")
    private String name;

    @Schema(description = "비밀번호", example = "", required = true)
    @NotNull(message = "비밀번호는 필수 입력 값입니다.")
    private String code;

    @Schema(description = "로그인하는 플랫폼", example = "MacIntel", required = true) private String platform;

    @Schema(description = "토큰") private String token;
}
