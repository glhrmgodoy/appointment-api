package com.godoy.appointment.repository;

import com.godoy.appointment.domain.entity.ServiceOffered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceOfferedRepository extends JpaRepository<ServiceOffered, UUID> {

    List<ServiceOffered> findAllByProfessionalId(UUID professionalId);

    boolean existsByProfessionalIdAndActiveTrue(UUID professionalId);
}
