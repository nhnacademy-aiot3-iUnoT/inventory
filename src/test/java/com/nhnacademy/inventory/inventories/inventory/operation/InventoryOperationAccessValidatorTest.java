package com.nhnacademy.inventory.inventories.inventory.operation;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryOperationAccessValidatorTest {

    @Mock
    private StorageService storageService;

    @Mock
    private OrganizationAccessService organizationAccessService;

    @InjectMocks
    private InventoryOperationAccessValidator accessValidator;

    @Test
    void 일반_의약품은_저장소_권한만_검증한다() {
        Storage storage = mock(Storage.class);
        Medicine medicine = mock(Medicine.class);

        when(storage.getId()).thenReturn(1L);
        when(medicine.requiresNarcoticHandlingPermission()).thenReturn(false);

        accessValidator.validate(storage, medicine);

        verify(storageService).checkStoragePermission(1L);
        verifyNoInteractions(organizationAccessService);
    }

    @Test
    void 마약류는_OWNER_BOSS_권한까지_검증한다() {
        Storage storage = mock(Storage.class);
        Organization organization = mock(Organization.class);
        Medicine medicine = mock(Medicine.class);

        when(storage.getId()).thenReturn(1L);
        when(storage.getOrganization()).thenReturn(organization);
        when(organization.getId()).thenReturn(10L);
        when(medicine.requiresNarcoticHandlingPermission()).thenReturn(true);

        accessValidator.validate(storage, medicine);

        verify(storageService).checkStoragePermission(1L);
        verify(organizationAccessService).requireOwnerOrBossOf(10L);
    }

    @Test
    void 마약류_MEMBER는_작업할_수_없다() {
        Storage storage = mock(Storage.class);
        Organization organization = mock(Organization.class);
        Medicine medicine = mock(Medicine.class);

        when(storage.getId()).thenReturn(1L);
        when(storage.getOrganization()).thenReturn(organization);
        when(organization.getId()).thenReturn(10L);
        when(medicine.requiresNarcoticHandlingPermission()).thenReturn(true);

        doThrow(new ForbiddenException())
                .when(organizationAccessService)
                .requireOwnerOrBossOf(10L);

        assertThrowsExactly(
                ForbiddenException.class,
                () -> accessValidator.validate(storage, medicine)
        );

        verify(storageService).checkStoragePermission(1L);
        verify(organizationAccessService).requireOwnerOrBossOf(10L);
    }
}
