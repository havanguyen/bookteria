package com.devteria.profile.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.devteria.profile.entity.UserProfile;

@Repository
public interface UserProfileRepository extends Neo4jRepository<UserProfile, UUID> {
    Optional<UserProfile> findByUserId(String userId);
    List<UserProfile> findByUserIdIn(List<String> userIds);
}