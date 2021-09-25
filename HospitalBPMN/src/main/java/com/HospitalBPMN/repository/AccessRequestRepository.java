package com.HospitalBPMN.repository;

import com.HospitalBPMN.domain.AccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessRequestRepository extends JpaRepository<AccessRequest, Long> {
    Optional<AccessRequest> findOneByEntityId(Long entityId);

}
