package com.dxsoft.rsgzgl.dictionary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceRankCategoryTest {

    @Mock
    private DictionaryRepository dictionaryRepository;

    @Mock
    private com.dxsoft.rsgzgl.security.AccessControlService accessControlService;

    @Mock
    private com.dxsoft.rsgzgl.maintenance.OperationLogService operationLogService;

    @InjectMocks
    private DictionaryService dictionaryService;

    @Test
    void resolveRankCategoryUsesExistingLbWhenPresent() {
        assertThat(dictionaryService.resolveRankCategory("二级警督", "jx")).isEqualTo("jx");
    }

    @Test
    void resolveRankCategoryLooksUpDictionaryCodeWhenLbMissing() {
        when(dictionaryRepository.findDictionaryCodeByName("二级警督")).thenReturn("0230107");
        assertThat(dictionaryService.resolveRankCategory("二级警督", "")).isEqualTo("jx");
    }

    @Test
    void rankLevelTreeUsesFldgzPrefix() {
        when(dictionaryRepository.findPayrollFieldDictionaryPrefix("JX")).thenReturn("023");
        when(dictionaryRepository.findTreeNodes("023")).thenReturn(java.util.List.of());
        dictionaryService.rankLevelTree();
        org.mockito.Mockito.verify(dictionaryRepository).findTreeNodes("023");
    }
}
