package com.github.sportbot.repository;

import com.github.sportbot.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
}
