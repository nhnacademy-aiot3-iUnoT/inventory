package com.nhnacademy.inventory.organizations.invitation.repository;

import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository extends JpaRepository<Invitation, Long>, InvitationRepositoryCustom {
    Optional<Invitation> findByToken(UUID token);

    void deleteByOrganizationId(Long organizationId);

    Page<Invitation> findByOrganizationId(Long organizationId, Pageable pageable);

    @Modifying
    @Query("""
        update Invitation i
        set i.invitationStatus = 'CANCELED'
        where i.organization.id = :organizationId and i.invitationStatus = 'ACTIVE'
    """)
    void cancelByOrganizationId(Long organizationId);
}
