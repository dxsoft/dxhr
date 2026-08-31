package com.dxsoft.rsgzgl.dictionary;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dictionaries")
class DictionaryController {

    private final DictionaryService dictionaryService;

    DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @GetMapping
    PageResponse<DictionaryEntry> list(
            @RequestParam(required = false) String prefix,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return dictionaryService.entriesAll(prefix, keyword);
        }
        return dictionaryService.entries(prefix, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/categories")
    List<DictionaryEntry> categories() {
        return dictionaryService.categories();
    }

    @GetMapping("/field-configs")
    List<DictionaryFieldConfig> fieldConfigs(@RequestParam(required = false) String tableName) {
        return dictionaryService.fieldConfigs(tableName);
    }

    @GetMapping("/payroll-field-configs")
    List<DictionaryFieldConfig> payrollFieldConfigs() {
        return dictionaryService.payrollFieldConfigs();
    }

    @GetMapping("/tree")
    List<DictionaryTreeNode> tree(
            @RequestParam(required = false) String prefix,
            @RequestParam(required = false) String field,
            @RequestParam(required = false) String unitCategory,
            @RequestParam(required = false) String organizationProperty,
            @RequestParam(required = false) String organizationCode) {
        if (field != null && !field.isBlank()) {
            return dictionaryService.treeFiltered(field, unitCategory, organizationProperty, organizationCode);
        }
        return dictionaryService.tree(prefix);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    DictionaryEntry create(@RequestBody DictionaryMaintenanceRequest request) {
        return dictionaryService.create(request);
    }

    @PutMapping("/{code}")
    DictionaryEntry update(@PathVariable String code, @RequestBody DictionaryMaintenanceRequest request) {
        return dictionaryService.update(code, request);
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void disable(@PathVariable String code) {
        dictionaryService.disable(code);
    }
}
