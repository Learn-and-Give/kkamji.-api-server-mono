package com.kkamjidot.api.mono.repository;

import com.kkamjidot.api.mono.domain.Challenge;
import com.kkamjidot.api.mono.domain.TakeAClass;
import com.kkamjidot.api.mono.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TakeAClassRepository extends JpaRepository<TakeAClass, Long> {
    Optional<TakeAClass> findByChallAndUser(Challenge chall, User user);
}