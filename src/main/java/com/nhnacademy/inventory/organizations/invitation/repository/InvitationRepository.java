package com.nhnacademy.inventory.organizations.invitation.repository;

import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
import com.nhnacademy.inventory.organizations.invitation.domain.InvitationStatus;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByToken(UUID token);

    boolean existsByOrganizationAndEmailAndInvitationStatus(Organization organization, String email, InvitationStatus invitationStatus);
}
