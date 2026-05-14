package com.dxsoft.rsgzgl.dataexchange;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data-exchange")
class DataExchangeController {

    private final DataExchangeService dataExchangeService;

    DataExchangeController(DataExchangeService dataExchangeService) {
        this.dataExchangeService = dataExchangeService;
    }

    @GetMapping("/personnel")
    PageResponse<PersonnelExportRecord> exportPersonnel(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return dataExchangeService.exportPersonnel(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/personnel/download")
    ResponseEntity<byte[]> downloadPersonnelCsv(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword) {
        return dataExchangeService.downloadPersonnelCsv(organizationCode, keyword);
    }

    @PostMapping("/dispatch/personnel")
    ResponseEntity<byte[]> dispatchPersonnelPackage(@RequestBody PersonnelDispatchRequest request) {
        return dataExchangeService.dispatchPersonnelPackage(request);
    }

    @PostMapping("/receive/preview")
    ReceivePreviewResponse previewReceive(@RequestBody ReceiveRequest request) {
        return dataExchangeService.previewReceive(request);
    }

    @PostMapping("/receive/apply")
    ReceiveApplyResponse applyReceive(@RequestBody ReceiveRequest request) {
        return dataExchangeService.applyReceive(request);
    }

    @GetMapping("/annual-report")
    PageResponse<AnnualReportRecord> exportAnnualReport(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return dataExchangeService.exportAnnualReport(organizationCode, period, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/annual-report/download")
    ResponseEntity<byte[]> downloadAnnualReportCsv(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String keyword) {
        return dataExchangeService.downloadAnnualReportCsv(organizationCode, period, keyword);
    }

    record PersonnelDispatchRequest(
            List<String> organizationCodes,
            boolean includeDescendants,
            List<PersonKey> selectedPersonnel) {
    }

    record PersonKey(String organizationCode, String personCode) {
    }

    record ReceiveRequest(
            String packageJson,
            String mode,
            String targetOrganizationCode,
            List<PersonKey> selectedPersonnel) {
    }

    record ReceivePreviewResponse(int totalRecords, List<PersonnelExportRecord> rows, List<String> sampleErrors, String message) {
    }

    record ReceiveApplyResponse(int receivedRecords, String message) {
    }
}
