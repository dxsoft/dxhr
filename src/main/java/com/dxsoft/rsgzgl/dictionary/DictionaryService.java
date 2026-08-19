package com.dxsoft.rsgzgl.dictionary;

import com.dxsoft.rsgzgl.common.NotFoundException;
import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.maintenance.OperationLogService;
import com.dxsoft.rsgzgl.security.AccessControlService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DictionaryService {

    private final DictionaryRepository dictionaryRepository;
    private final AccessControlService accessControlService;
    private final OperationLogService operationLogService;

    DictionaryService(
            DictionaryRepository dictionaryRepository,
            AccessControlService accessControlService,
            OperationLogService operationLogService) {
        this.dictionaryRepository = dictionaryRepository;
        this.accessControlService = accessControlService;
        this.operationLogService = operationLogService;
    }

    private void requireSystemConfigWrite() {
        if (!accessControlService.hasPermission("SYSTEM_CONFIG")) {
            throw new IllegalStateException("当前用户没有字典维护权限。");
        }
    }

    public PageResponse<DictionaryEntry> entries(String prefix, String keyword, PageRequest pageRequest) {
        return PageResponse.of(
                dictionaryRepository.findEntries(prefix, keyword, pageRequest),
                pageRequest,
                dictionaryRepository.countEntries(prefix, keyword));
    }

    public PageResponse<DictionaryEntry> entriesAll(String prefix, String keyword) {
        List<DictionaryEntry> rows = dictionaryRepository.findEntriesAll(prefix, keyword, 5000);
        long total = dictionaryRepository.countEntries(prefix, keyword);
        return PageResponse.of(rows, new PageRequest(0, Math.max(rows.size(), 1)), total);
    }

    public List<DictionaryEntry> categories() {
        return dictionaryRepository.findCategories();
    }

    public List<DictionaryFieldConfig> fieldConfigs(String tableName) {
        return dictionaryRepository.findFieldConfigs(tableName);
    }

    public List<DictionaryTreeNode> tree(String prefix) {
        return dictionaryRepository.findTreeNodes(prefix);
    }

    public List<DictionaryTreeNode> treeFiltered(
            String fieldName,
            String unitCategory,
            String organizationProperty,
            String organizationCode) {
        String resolvedUnitCategory = unitCategory;
        if (resolvedUnitCategory == null || resolvedUnitCategory.isBlank()) {
            resolvedUnitCategory = dictionaryRepository.findOrganizationCategory(organizationCode);
        }
        DictionaryFilterSpec filter = DictionaryQueryFilter.forField(fieldName, resolvedUnitCategory, organizationProperty);
        if (filter == null) {
            throw new IllegalArgumentException("Unsupported dictionary field filter: " + fieldName);
        }
        return dictionaryRepository.findTreeNodesFiltered(filter);
    }

    public DictionaryEntry create(DictionaryMaintenanceRequest request) {
        requireSystemConfigWrite();
        if (request.code() == null || request.code().isBlank()) {
            throw new IllegalArgumentException("字典编码不能为空。");
        }
        if (dictionaryRepository.dictionaryExists(request.code().trim())) {
            throw new IllegalArgumentException("字典编码已存在：" + request.code());
        }
        dictionaryRepository.insertDictionary(request);
        DictionaryEntry created = dictionaryRepository.findDictionaryByCode(request.code().trim());
        operationLogService.record(
                "CREATE_DICTIONARY",
                "cyz",
                created.code(),
                "新增字典 " + created.code() + " " + created.name());
        return created;
    }

    public DictionaryEntry update(String code, DictionaryMaintenanceRequest request) {
        requireSystemConfigWrite();
        if (!dictionaryRepository.dictionaryExists(code)) {
            throw new NotFoundException("Dictionary entry not found: " + code);
        }
        dictionaryRepository.updateDictionary(code, request);
        DictionaryEntry updated = dictionaryRepository.findDictionaryByCode(code);
        operationLogService.record(
                "UPDATE_DICTIONARY",
                "cyz",
                code,
                "更新字典 " + code + " " + updated.name());
        return updated;
    }

    public void disable(String code) {
        requireSystemConfigWrite();
        if (!dictionaryRepository.dictionaryExists(code)) {
            throw new NotFoundException("Dictionary entry not found: " + code);
        }
        dictionaryRepository.disableDictionary(code);
        operationLogService.record(
                "DISABLE_DICTIONARY",
                "cyz",
                code,
                "停用字典 " + code);
    }
}
