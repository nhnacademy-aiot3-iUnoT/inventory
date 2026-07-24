package com.nhnacademy.inventory.organizations.invitation.repository;

import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
}
