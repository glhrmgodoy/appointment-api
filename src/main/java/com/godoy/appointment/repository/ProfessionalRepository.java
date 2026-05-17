package com.godoy.appointment.repository;

import com.godoy.appointment.domain.entity.Professional;
import com.godoy.appointment.domain.enums.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProfessionalRepository extends JpaRepository<Professional, UUID> {

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    List<Professional> findAllBySpecialty(Specialty specialty);
}
