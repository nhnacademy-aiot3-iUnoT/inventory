package com.nhnacademy.inventory.inventories.inventory.service;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoriesResponse;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.organizations.department.domain.Department;
import com.nhnacademy.inventory.organizations.department.domain.MemberDepartment;
import com.nhnacademy.inventory.organizations.department.repository.MemberDepartmentRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoriesSearchServiceTest {

    @Mock
    MedicineInventoryRepository medicineInventoryRepository;
    @Mock
    OrganizationMemberRepository organizationMemberRepository;
    @Mock
    MemberDepartmentRepository memberDepartmentRepository;
    @Mock
    StorageRepository storageRepository;


    @InjectMocks
    InventoriesSearchService inventoriesSearchService;

    UUID accountId;


    @BeforeEach
    void setUp(){

        accountId = UUID.randomUUID();
        UserContext.setUserUuid(accountId);

    }

    @AfterEach
    void afterSetUp(){

        UserContext.clear();
    }


    @Test
    @DisplayName("전체 재고 조회 권한 -OWNER")
    void getInventories() {


        String search = "타이레놀";
        List<Long> departmentIds = List.of(1L);
        OrganizationMember member = mock(OrganizationMember.class);

        MemberDepartment memberDepartment = mock(MemberDepartment.class);
        List<MemberDepartment> memberDepartments = List.of(memberDepartment);
        InventoriesResponse inventoriesResponse = new InventoriesResponse(
                1L,
                1L,
                "타이레놀",
                "1234",
                "10통",
                LocalDate.now(),
                "저장소",
                50
        );

       given(organizationMemberRepository.findByAccountUuid(accountId)).willReturn(Optional.of(member));
       given(member.getOrganizationRole()).willReturn(OrganizationRole.ORG_OWNER);
       given(memberDepartmentRepository.findAllByOrganizationMember(member))
               .willReturn(memberDepartments);

       given(medicineInventoryRepository.findAllInventoriesByDepartmentIds(search.trim(),1L,departmentIds, Pageable.ofSize(10)))
               .willReturn(new PageImpl<>(List.of(inventoriesResponse),Pageable.ofSize(10),1));


       given(memberDepartment.getDepartment()).willReturn(mock(Department.class));
       given(memberDepartment.getDepartment().getId()).willReturn(1L);




       Page<InventoriesResponse> result = inventoriesSearchService.getInventories(search,1L,Pageable.ofSize(10));

       verify(organizationMemberRepository).findByAccountUuid(accountId);
       verify(memberDepartmentRepository).findAllByOrganizationMember(member);
       verify(medicineInventoryRepository).findAllInventoriesByDepartmentIds(eq(search),eq(1L),anyList(),any(Pageable.class));


       InventoriesResponse response = result.getContent().getFirst();

       assertAll(

               () -> assertEquals(1L,response.storageId()),
               () -> assertEquals(1L,response.packUnitId()),
               () -> assertEquals("타이레놀",response.productName()),
               () -> assertEquals("1234",response.itemCode()),
               () -> assertEquals("10통",response.packUnit()),
               () -> assertEquals(LocalDate.now(),response.expirationDate()),
               () -> assertEquals("저장소",response.storageName()),
               () -> assertEquals(50,response.totalQuantity())


       );


    }


    @Test
    @DisplayName("전체 재고 조회 권한 BOSS")
    void InventoriesBossTest(){


        InventoriesResponse inventoriesResponse = new InventoriesResponse(
                1L,
                1L,
                "타이레놀",
                "1234",
                "10통",
                LocalDate.now(),
                "저장소",
                50
        );


        Organization organization = mock(Organization.class);

        given(organization.getId()).willReturn(1L);

        OrganizationMember member = OrganizationMember.createUser(
                organization,
                accountId,
                OrganizationRole.ORG_BOSS
        );


        List<Storage> storages = List.of(mock(Storage.class));

        given(organizationMemberRepository.findByAccountUuid(accountId)).willReturn(Optional.of(member));


        given(storageRepository.findAllByOrganizationId(anyLong())).willReturn(storages);
        given(medicineInventoryRepository.findAllInventories(isNull(),isNull(),anyList(),any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(inventoriesResponse),Pageable.ofSize(10),1));


        Page<InventoriesResponse> result = inventoriesSearchService.getInventories(null,null,Pageable.ofSize(10));

        assertEquals(1,result.getContent().size());
        assertEquals(1,result.getTotalElements());


        assertAll(

                () -> assertEquals(1L,result.getContent().getFirst().storageId()),
                () -> assertEquals(1L,result.getContent().getFirst().packUnitId()),
                () -> assertEquals("타이레놀",result.getContent().getFirst().productName()),
                () -> assertEquals("1234",result.getContent().getFirst().itemCode()),
                () -> assertEquals("10통",result.getContent().getFirst().packUnit()),
                () -> assertEquals("저장소",result.getContent().getFirst().storageName()),
                () -> assertEquals(50,result.getContent().getFirst().totalQuantity())

        );


        verify(organizationMemberRepository).findByAccountUuid(accountId);
        verify(storageRepository).findAllByOrganizationId(anyLong());
        verify(medicineInventoryRepository).findAllInventories(isNull(),isNull(),anyList(),any(Pageable.class));




    }







    @Test
    @DisplayName("멤버-부서 null")
    void nullTest(){

        OrganizationMember organizationMember = mock(OrganizationMember.class);
        List<MemberDepartment> memberDepartments = List.of();


        given(organizationMemberRepository.findByAccountUuid(accountId)).willReturn(Optional.of(organizationMember));
        given(memberDepartmentRepository.findAllByOrganizationMember(organizationMember))
                .willReturn(memberDepartments);

        inventoriesSearchService.getInventories("타이레놀",1L,Pageable.ofSize(10));

        verify(organizationMemberRepository).findByAccountUuid(any(UUID.class));
        verify(memberDepartmentRepository).findAllByOrganizationMember(any(OrganizationMember.class));


    }









}