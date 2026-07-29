package com.nhnacademy.inventory.organizations.department.domain;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "member_departments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberDepartment {

    @Id
    @Column(name = "member_department")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberDepartmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_member_id")
    private OrganizationMember organizationMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "joined_date", nullable = false)
    private LocalDate joinedDate;

    @Builder
    private MemberDepartment(OrganizationMember organizationMember, Department department) {
        this.organizationMember = organizationMember;
        this.department = department;
    }

    @PrePersist
    protected void onCreate() {
        this.joinedDate = LocalDate.now();
    }
}
