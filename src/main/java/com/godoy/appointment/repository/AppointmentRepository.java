package com.godoy.appointment.repository;

import com.godoy.appointment.domain.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findAllByClientId(UUID clientId);

    List<Appointment> findAllByProfessionalId(UUID professionalId);

    boolean existsByProfessionalIdAndScheduledAt(UUID professionalId, LocalDateTime scheduledAt);

    boolean existsByClientIdAndScheduledAt(UUID clientId, LocalDateTime scheduledAt);

    List<Appointment> findAllByStatus(Appointment status);
}
