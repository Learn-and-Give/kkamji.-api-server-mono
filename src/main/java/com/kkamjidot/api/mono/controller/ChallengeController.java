package com.kkamjidot.api.mono.controller;

import com.kkamjidot.api.mono.domain.Challenge;
import com.kkamjidot.api.mono.domain.TakeAClass;
import com.kkamjidot.api.mono.domain.User;
import com.kkamjidot.api.mono.domain.enumerate.ApplicationStatus;
import com.kkamjidot.api.mono.dto.response.ChallengeResponse;
import com.kkamjidot.api.mono.dto.response.ThisWeekResponse;
import com.kkamjidot.api.mono.dto.response.WeekResponse;
import com.kkamjidot.api.mono.service.ChallengeService;
import com.kkamjidot.api.mono.service.ReadableService;
import com.kkamjidot.api.mono.service.TakeAClassService;
import com.kkamjidot.api.mono.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.chrono.ChronoPeriod;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Tag(name = "챌린지", description = "챌린지 관련 작업들")
@RequiredArgsConstructor
@RestController
public class ChallengeController {
    private final Logger LOGGER = LoggerFactory.getLogger(ChallengeController.class);
    private final UserService userService;
    private final ChallengeService challengeService;
    private final TakeAClassService takeAClassService;
    private final ReadableService readableService;

    @Operation(summary = "챌린지 목록 조회 API", description = "모든 챌린지를 조회한다.")
    @GetMapping("v1/challenges")
    public ResponseEntity<List<ChallengeResponse>> readChallenges(@Parameter(description = "로그인한 회원 코드", example = "1234") @RequestHeader String code) {
        User user = userService.authorization(code);  // 회원 인증

        // 챌린지 목록 조회
        List<Challenge> challenges = challengeService.findAll();
        List<ChallengeResponse> challengeResponses = new ArrayList<>(challenges.size());
        for (Challenge challenge : challenges) {
            ChallengeResponse challengeResponse = ChallengeResponse.of(challenge);
            takeAClassService.findApplicationStatus(challenge, user).ifPresent(challengeResponse::setApplicationStatus);     // 신청상태
            challengeResponses.add(challengeResponse);
        }

        return ResponseEntity.ok(challengeResponses);
    }

    @Operation(summary = "챌린지 조회 API", description = "한 챌린지 정보를 조회한다.")
    @GetMapping("v1/challenges/{challengeId}")
    public ResponseEntity<ChallengeResponse> readChallenge(@Parameter(description = "로그인한 회원 코드", example = "1234") @RequestHeader String code,
                                                           @PathVariable Long challengeId) {
        User user = userService.authorization(code);  // 회원 인증

        // 챌린지 조회
        Challenge challenge = challengeService.findOne(challengeId);
        ChallengeResponse challengeResponse = ChallengeResponse.of(challenge);
        takeAClassService.findApplicationStatus(challenge, user).ifPresent(challengeResponse::setApplicationStatus);     // 신청상태

        return ResponseEntity.ok(challengeResponse);
    }

    @Operation(summary = "내가 참여한 챌린지 목록 조회 API", description = "내가 참여한 챌린지 목록을 조회한다.")
    @GetMapping("v1/my/challenges")
    public ResponseEntity<List<ChallengeResponse>> readMyChallenges(@Parameter(description = "로그인한 회원 코드", example = "1234") @RequestHeader String code) {
        User user = userService.authorization(code);  // 회원 인증

        List<TakeAClass> takes = takeAClassService.findAllByUser(user);
        List<ChallengeResponse> challengeResponses = new ArrayList<>(takes.size());
        for (TakeAClass take: takes) {
            ChallengeResponse challengeResponse = ChallengeResponse.of(take.getChall());
            challengeResponse.setApplicationStatus(take.getTcApplicationstatus());
            challengeResponses.add(challengeResponse);
        }
        return ResponseEntity.ok(challengeResponses);
    }

    @Operation(summary = "챌린지 주차 정보 목록 조회 API", description = "한 챌린지의 주차별 열람가능 여부 정보 목록을 반환한다.")
    @GetMapping("v1/challenges/{challengeId}/weeks")
    public ResponseEntity<WeekResponse> readWeeks(@Parameter(description = "로그인한 회원 코드", example = "1234") @RequestHeader String code,
                                                  @PathVariable Long challengeId) {
        LOGGER.info("API: v1/challenges/{challengeId}/weeks [path: challengeId = {}]", challengeId);

        User user = userService.authorization(code);  // 회원 인증
        LOGGER.info("User: {}", user.getUserName());

        Challenge challenge = challengeService.findOne(challengeId);
        List<Integer> readableWeeks = readableService.findReadableWeeksByUser(user, challenge);            // 열람가능 주차 조회
        Integer challTotalWeeks = challenge.getChallTotalWeeks();   // 총 주차 조회

        // 열람 가능한 주차 true로 변경
        boolean[] weeks = new boolean[challTotalWeeks];
        for (int i = 1; i < challenge.getNowWeek(); ++i) if (readableWeeks.contains(i)) weeks[i - 1] = true;

        return ResponseEntity.ok(WeekResponse.builder()
                .challengeId(challengeId)
                .totalWeeks(challTotalWeeks)
                .weeks(weeks)
                .build());
    }

    @Operation(summary = "현재 주자 반환 API", description = "현재 일시와 현재 챌린지에서의 주차를 반환한다.")
    @GetMapping("v1/challenges/{challengeId}/now")
    public ResponseEntity<ThisWeekResponse> now(@Parameter(description = "로그인한 회원 코드", example = "1234") @RequestHeader String code,
                                                @PathVariable Long challengeId) {
        LOGGER.info("API: v1/challenges/{challengeId}/now [path: challengeId = {}]", challengeId);

        User user = userService.authorization(code);  // 회원 인증
        LOGGER.info("User: {}", user.getUserName());

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        Challenge challenge = challengeService.findOne(challengeId);
        LocalDateTime challStartDate = challenge.getChallStartDate();
        return ResponseEntity.ok(ThisWeekResponse.builder()
                        .week(challenge.getNowWeek())
                        .challengeId(challengeId)
                        .challStartDate(challStartDate)
                        .now(now)
                        .build());
    }
}
