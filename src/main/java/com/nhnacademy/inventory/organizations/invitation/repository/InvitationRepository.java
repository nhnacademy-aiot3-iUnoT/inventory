package com.nhnacademy.inventory.organizations.invitation.repository;

import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository extends JpaRepository<Invitation, Long>, InvitationRepositoryCustom {
    Optional<Invitation> findByToken(UUID token);

    @Modifying
    @Query("""
                delete from Invitation i
                where i.organization.id = :organizationId
            """)
    void deleteByOrganizationId(Long organizationId);

    void deleteByOrganizationIdAndEmail(Long organizationId, String email);

    Optional<Invitation> findFirstByOrganizationIdAndInvitedByAdminTrueOrderByCreatedAtDesc(Long organizationId);

    @Query("""
            select (count(i) > 0)
            from Invitation i
            where i.organization.id = :organizationId
              and i.email = :email
              and (i.invitationStatus = 'ACTIVE' or i.invitationStatus ='USED')
              and i.expiredAt > :now
        """)
    boolean existsInvitationBy(Long organizationId, String email, LocalDateTime now);

    @Modifying
    @Query("""
           update Invitation i
           set i.invitationStatus = 'REISSUED'
           where i.organization.id = :organizationId
             and i.email = :email
             and i.invitationStatus = 'CANCELED'
    """)
    void updateInvitationReissued(Long organizationId, String email);

}
