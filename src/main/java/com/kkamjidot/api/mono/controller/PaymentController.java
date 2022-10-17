package com.kkamjidot.api.mono.controller;

import com.kkamjidot.api.mono.domain.User;
import com.kkamjidot.api.mono.dto.response.PointResponse;
import com.kkamjidot.api.mono.service.AuthService;
import com.kkamjidot.api.mono.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "결제", description = "포인트 관련 작업들")
@RequiredArgsConstructor
@RestController
public class PaymentController {
    private final AuthService authService;
    private final PaymentService paymentService;

    @Operation(summary = "포인트 조회 API", description = "현재 보유 포인트를 조회한다.")
    @GetMapping("v1/my/point")
    public ResponseEntity<PointResponse> readQuiz(@RequestHeader String jwt) {
        User user = authService.authenticate(jwt);

        return ResponseEntity.ok(PointResponse.builder().userId(user.getId()).point(paymentService.countOfAllQuizzesSolvedByUser(user.getId())).build());
    }
}