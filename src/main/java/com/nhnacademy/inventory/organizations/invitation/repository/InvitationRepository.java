package com.nhnacademy.inventory.organizations.invitation.repository;

import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
import com.nhnacademy.inventory.organizations.invitation.domain.InvitationStatus;
import com.nhnacademy.inventory.organizations.invitation.domain.InvitationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    void deleteByOrganizationId(@Param("organizationId") Long organizationId);

    Optional<Invitation> findFirstByOrganizationIdAndInvitationTypeOrderByCreatedAtDesc(
            Long organizationId,
            InvitationType invitationType
    );

    @Query("""
            select (count(i) > 0)
            from Invitation i
            where i.organization.id = :organizationId
              and i.email = :email
              and i.invitationType = :invitationType
              and i.invitationStatus = 'ACTIVE'
              and i.expiredAt > :now
        """)
    boolean existsActiveInvitation(Long organizationId, String email, InvitationType invitationType, LocalDateTime now);

    @Modifying
    @Query("""
            update Invitation i
               set i.invitationStatus = :expiredStatus
             where i.invitationStatus = :activeStatus
               and i.expiredAt < :now
        """)
    int expireInvitations(@Param("activeStatus") InvitationStatus activeStatus,
                          @Param("expiredStatus") InvitationStatus expiredStatus,
                          @Param("now") LocalDateTime now
    );
}
