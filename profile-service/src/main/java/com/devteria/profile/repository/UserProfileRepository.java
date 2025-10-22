package com.devteria.profile.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.devteria.profile.entity.UserProfile;

import java.util.UUID;

@Repository // khong yeu cau , tuy nhien can de biet cai nay dung de lam gi
public interface UserProfileRepository extends Neo4jRepository<UserProfile, UUID> {}
