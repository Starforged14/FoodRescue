package com.foodrescue.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodrescue.entity.DonorProfile;

public interface DonorProfileRepository extends JpaRepository<DonorProfile, UUID> {
}
