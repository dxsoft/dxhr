package com.dxsoft.rsgzgl.exchange.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dxsoft.rsgzgl.dataexchange.PersonnelExportRecord;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.AppUserPrincipal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExchangeNotificationServiceTest {

    private ExchangeNotificationRepository repository;
    private ExchangeNotificationService service;

    @BeforeEach
    void setUp() {
        repository = mock(ExchangeNotificationRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.hasPermission("DATA_EXCHANGE_READ")).thenReturn(true);
        when(accessControlService.currentUser()).thenReturn(new AppUserPrincipal(
                1L,
                "tester",
                "hash",
                "测试员",
                true,
                Set.of(),
                Set.of("DATA_EXCHANGE_READ"),
                false,
                Set.of("001"),
                "001",
                null,
                null));
        service = new ExchangeNotificationService(
                repository,
                new ExchangeDeploymentProperties(),
                accessControlService);
    }

    @Test
    void onSubmissionExportedStandaloneCreatesOutboundNotification() {
        when(repository.insert(any())).thenReturn(1L);

        service.onSubmissionExported(List.of(sample("001", "00001", "张三")), "batch-1");

        verify(repository).insert(any());
    }

    @Test
    void sharedDatabaseUsesInternalAudienceForSubmissionPending() {
        ExchangeDeploymentProperties properties = new ExchangeDeploymentProperties();
        properties.setDeploymentMode("SHARED");
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.hasPermission("DATA_EXCHANGE_READ")).thenReturn(true);
        when(accessControlService.currentUser()).thenReturn(new AppUserPrincipal(
                1L,
                "approver",
                "hash",
                "审批员",
                true,
                Set.of(),
                Set.of("DATA_EXCHANGE_READ"),
                true,
                Set.of(),
                null,
                null,
                null));
        ExchangeNotificationService sharedService = new ExchangeNotificationService(
                repository,
                properties,
                accessControlService);

        sharedService.onSubmissionExported(List.of(sample("001", "00001", "张三")), "batch-2");

        verify(repository).insert(any());
    }

    @Test
    void unreadCountRequiresPermissionAndDelegatesToRepository() {
        when(repository.countUnread()).thenReturn(3L);

        assertThat(service.unreadCount()).isEqualTo(3L);

        verify(repository).countUnread();
    }

    @Test
    void markReadUsesCurrentUsername() {
        when(repository.markRead(42L, "tester")).thenReturn(true);

        assertThat(service.markRead(42L)).isTrue();

        verify(repository).markRead(42L, "tester");
    }

    @Test
    void markAllReadUsesCurrentUsername() {
        when(repository.markAllRead("tester")).thenReturn(5);

        assertThat(service.markAllRead()).isEqualTo(5);

        verify(repository).markAllRead("tester");
    }

    private static PersonnelExportRecord sample(String org, String person, String name) {
        return new PersonnelExportRecord(
                org, "单位", person, name, null,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null);
    }
}
