package com.dxsoft.rsgzgl.setup;

import com.dxsoft.rsgzgl.personnel.ExcelImportPreview;
import com.dxsoft.rsgzgl.personnel.ExcelImportResult;
import com.dxsoft.rsgzgl.personnel.ExcelPersonnelImportService;
import java.io.IOException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/system-setup")
public class SystemSetupController {

    private final SystemInitializationService systemInitializationService;
    private final ExcelPersonnelImportService excelPersonnelImportService;

    SystemSetupController(
            SystemInitializationService systemInitializationService,
            ExcelPersonnelImportService excelPersonnelImportService) {
        this.systemInitializationService = systemInitializationService;
        this.excelPersonnelImportService = excelPersonnelImportService;
    }

    @GetMapping("/initialization/preview")
    SystemInitializationPreview previewInitialization(
            @RequestParam(required = false, defaultValue = "false") boolean clearOrganizationsAndLicense) {
        return systemInitializationService.preview(clearOrganizationsAndLicense);
    }

    @PostMapping("/initialization/execute")
    SystemInitializationResult executeInitialization(
            @RequestParam String confirmPhrase,
            @RequestParam(required = false, defaultValue = "false") boolean clearOrganizationsAndLicense) {
        return systemInitializationService.execute(confirmPhrase, clearOrganizationsAndLicense);
    }

    @GetMapping("/excel-import/template")
    ResponseEntity<byte[]> downloadExcelTemplate() {
        byte[] body = excelPersonnelImportService.templateWorkbook();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("personnel-import-template.xlsx")
                        .build()
                        .toString())
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(body);
    }

    @PostMapping("/excel-import/preview")
    ExcelImportPreview previewExcelImport(
            @RequestParam String organizationCode,
            @RequestParam("file") MultipartFile file) throws IOException {
        return excelPersonnelImportService.preview(organizationCode, file.getInputStream());
    }

    @PostMapping("/excel-import/execute")
    ExcelImportResult executeExcelImport(
            @RequestParam String organizationCode,
            @RequestParam("file") MultipartFile file) throws IOException {
        return excelPersonnelImportService.importPersonnel(organizationCode, file.getInputStream());
    }
}
