package com.genai.apigateway.repository;

import com.genai.apigateway.entity.RequestTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestTrackingRepository extends JpaRepository<RequestTracking, String> {
}
