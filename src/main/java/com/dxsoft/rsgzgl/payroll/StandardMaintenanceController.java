package com.dxsoft.rsgzgl.payroll;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/standards")
class StandardMaintenanceController {

    private final PayrollService payrollService;

    StandardMaintenanceController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @PostMapping("/allowances")
    @ResponseStatus(HttpStatus.CREATED)
    AllowanceStandard createAllowance(@RequestBody AllowanceStandardRequest request) {
        return payrollService.createAllowanceStandard(request);
    }

    @PutMapping("/allowances/{id}")
    AllowanceStandard updateAllowance(@PathVariable int id, @RequestBody AllowanceStandardRequest request) {
        return payrollService.updateAllowanceStandard(id, request);
    }

    @DeleteMapping("/allowances/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAllowance(@PathVariable int id) {
        payrollService.deleteAllowanceStandard(id);
    }

    @PostMapping("/ranks")
    @ResponseStatus(HttpStatus.CREATED)
    RankAllowanceStandard createRank(@RequestBody RankAllowanceStandardRequest request) {
        return payrollService.createRankAllowanceStandard(request);
    }

    @PutMapping("/ranks/{id}")
    RankAllowanceStandard updateRank(@PathVariable int id, @RequestBody RankAllowanceStandardRequest request) {
        return payrollService.updateRankAllowanceStandard(id, request);
    }

    @DeleteMapping("/ranks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteRank(@PathVariable int id) {
        payrollService.deleteRankAllowanceStandard(id);
    }

    @PostMapping("/retained")
    @ResponseStatus(HttpStatus.CREATED)
    RetainedAllowanceStandard createRetained(@RequestBody RetainedAllowanceStandardRequest request) {
        return payrollService.createRetainedAllowanceStandard(request);
    }

    @PutMapping("/retained/{positionCode}")
    RetainedAllowanceStandard updateRetained(
            @PathVariable String positionCode,
            @RequestBody RetainedAllowanceStandardRequest request) {
        return payrollService.updateRetainedAllowanceStandard(positionCode, request);
    }

    @DeleteMapping("/retained/{positionCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteRetained(@PathVariable String positionCode) {
        payrollService.deleteRetainedAllowanceStandard(positionCode);
    }

    @PostMapping("/year-allowances")
    @ResponseStatus(HttpStatus.CREATED)
    YearAllowanceStandard createYearAllowance(@RequestBody YearAllowanceStandardRequest request) {
        return payrollService.createYearAllowanceStandard(request);
    }

    @PutMapping("/year-allowances/{standardYearMonth}")
    YearAllowanceStandard updateYearAllowance(
            @PathVariable String standardYearMonth,
            @RequestBody YearAllowanceStandardRequest request) {
        return payrollService.updateYearAllowanceStandard(standardYearMonth, request);
    }

    @DeleteMapping("/year-allowances/{standardYearMonth}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteYearAllowance(@PathVariable String standardYearMonth) {
        payrollService.deleteYearAllowanceStandard(standardYearMonth);
    }

    @PostMapping("/position-salaries")
    @ResponseStatus(HttpStatus.CREATED)
    PositionSalaryStandard createPositionSalary(@RequestBody PositionSalaryStandardRequest request) {
        return payrollService.createPositionSalaryStandard(request);
    }

    @PutMapping("/position-salaries/{standardYearMonth}/{positionCode}")
    PositionSalaryStandard updatePositionSalary(
            @PathVariable String standardYearMonth,
            @PathVariable String positionCode,
            @RequestBody PositionSalaryStandardRequest request) {
        return payrollService.updatePositionSalaryStandard(standardYearMonth, positionCode, request);
    }

    @DeleteMapping("/position-salaries/{standardYearMonth}/{positionCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePositionSalary(
            @PathVariable String standardYearMonth,
            @PathVariable String positionCode) {
        payrollService.deletePositionSalaryStandard(standardYearMonth, positionCode);
    }

    @PostMapping("/grade-salaries")
    @ResponseStatus(HttpStatus.CREATED)
    GradeSalaryStandard createGradeSalary(@RequestBody GradeSalaryStandardRequest request) {
        return payrollService.createGradeSalaryStandard(request);
    }

    @PutMapping("/grade-salaries/{standardYearMonth}/{gradeLevel}")
    GradeSalaryStandard updateGradeSalary(
            @PathVariable String standardYearMonth,
            @PathVariable String gradeLevel,
            @RequestBody GradeSalaryStandardRequest request) {
        return payrollService.updateGradeSalaryStandard(standardYearMonth, gradeLevel, request);
    }

    @DeleteMapping("/grade-salaries/{standardYearMonth}/{gradeLevel}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteGradeSalary(@PathVariable String standardYearMonth, @PathVariable String gradeLevel) {
        payrollService.deleteGradeSalaryStandard(standardYearMonth, gradeLevel);
    }

    @PostMapping("/police-grade-salaries")
    @ResponseStatus(HttpStatus.CREATED)
    GradeSalaryStandard createPoliceGradeSalary(@RequestBody GradeSalaryStandardRequest request) {
        return payrollService.createPoliceGradeSalaryStandard(request);
    }

    @PutMapping("/police-grade-salaries/{standardYearMonth}/{gradeLevel}")
    GradeSalaryStandard updatePoliceGradeSalary(
            @PathVariable String standardYearMonth,
            @PathVariable String gradeLevel,
            @RequestBody GradeSalaryStandardRequest request) {
        return payrollService.updatePoliceGradeSalaryStandard(standardYearMonth, gradeLevel, request);
    }

    @DeleteMapping("/police-grade-salaries/{standardYearMonth}/{gradeLevel}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePoliceGradeSalary(@PathVariable String standardYearMonth, @PathVariable String gradeLevel) {
        payrollService.deletePoliceGradeSalaryStandard(standardYearMonth, gradeLevel);
    }

    @PostMapping("/position-grade-salaries")
    @ResponseStatus(HttpStatus.CREATED)
    PositionGradeSalaryStandard createPositionGradeSalary(@RequestBody PositionGradeSalaryStandardRequest request) {
        return payrollService.createPositionGradeSalaryStandard(request);
    }

    @PutMapping("/position-grade-salaries/{standardYearMonth}/{positionCode}")
    PositionGradeSalaryStandard updatePositionGradeSalary(
            @PathVariable String standardYearMonth,
            @PathVariable String positionCode,
            @RequestBody PositionGradeSalaryStandardRequest request) {
        return payrollService.updatePositionGradeSalaryStandard(standardYearMonth, positionCode, request);
    }

    @DeleteMapping("/position-grade-salaries/{standardYearMonth}/{positionCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePositionGradeSalary(
            @PathVariable String standardYearMonth,
            @PathVariable String positionCode) {
        payrollService.deletePositionGradeSalaryStandard(standardYearMonth, positionCode);
    }

    @PostMapping("/judicial-position-grade-salaries")
    @ResponseStatus(HttpStatus.CREATED)
    JudicialPositionGradeSalaryStandard createJudicialPositionGradeSalary(
            @RequestBody JudicialPositionGradeSalaryStandardRequest request) {
        return payrollService.createJudicialPositionGradeSalaryStandard(request);
    }

    @PutMapping("/judicial-position-grade-salaries/{standardYearMonth}/{positionCode}")
    JudicialPositionGradeSalaryStandard updateJudicialPositionGradeSalary(
            @PathVariable String standardYearMonth,
            @PathVariable String positionCode,
            @RequestBody JudicialPositionGradeSalaryStandardRequest request) {
        return payrollService.updateJudicialPositionGradeSalaryStandard(standardYearMonth, positionCode, request);
    }

    @DeleteMapping("/judicial-position-grade-salaries/{standardYearMonth}/{positionCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteJudicialPositionGradeSalary(
            @PathVariable String standardYearMonth,
            @PathVariable String positionCode) {
        payrollService.deleteJudicialPositionGradeSalaryStandard(standardYearMonth, positionCode);
    }

    @PostMapping("/salary-levels")
    @ResponseStatus(HttpStatus.CREATED)
    SalaryLevelStandard createSalaryLevel(@RequestBody SalaryLevelStandardRequest request) {
        return payrollService.createSalaryLevelStandard(request);
    }

    @PutMapping("/salary-levels/{standardYearMonth}/{jobCategoryCode}/{salaryLevel}")
    SalaryLevelStandard updateSalaryLevel(
            @PathVariable String standardYearMonth,
            @PathVariable String jobCategoryCode,
            @PathVariable String salaryLevel,
            @RequestBody SalaryLevelStandardRequest request) {
        return payrollService.updateSalaryLevelStandard(standardYearMonth, jobCategoryCode, salaryLevel, request);
    }

    @DeleteMapping("/salary-levels/{standardYearMonth}/{jobCategoryCode}/{salaryLevel}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteSalaryLevel(
            @PathVariable String standardYearMonth,
            @PathVariable String jobCategoryCode,
            @PathVariable String salaryLevel) {
        payrollService.deleteSalaryLevelStandard(standardYearMonth, jobCategoryCode, salaryLevel);
    }

    @PostMapping("/intern-salaries")
    @ResponseStatus(HttpStatus.CREATED)
    InternSalaryStandard createInternSalary(@RequestBody InternSalaryStandardRequest request) {
        return payrollService.createInternSalaryStandard(request);
    }

    @PutMapping("/intern-salaries/{standardYearMonth}/{educationCode}/{regularPositionCode}")
    InternSalaryStandard updateInternSalary(
            @PathVariable String standardYearMonth,
            @PathVariable String educationCode,
            @PathVariable String regularPositionCode,
            @RequestBody InternSalaryStandardRequest request) {
        return payrollService.updateInternSalaryStandard(
                standardYearMonth, educationCode, regularPositionCode, request);
    }

    @DeleteMapping("/intern-salaries/{standardYearMonth}/{educationCode}/{regularPositionCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteInternSalary(
            @PathVariable String standardYearMonth,
            @PathVariable String educationCode,
            @PathVariable String regularPositionCode) {
        payrollService.deleteInternSalaryStandard(standardYearMonth, educationCode, regularPositionCode);
    }

    @PostMapping("/wage-reforms")
    @ResponseStatus(HttpStatus.CREATED)
    WageReformStandard createWageReform(@RequestBody WageReformStandardRequest request) {
        return payrollService.createWageReformStandard(request);
    }

    @PutMapping("/wage-reforms/{positionCode}/{appointmentYearsLower}/{appointmentYearsUpper}/{reformYearsLower}/{reformYearsUpper}")
    WageReformStandard updateWageReform(
            @PathVariable String positionCode,
            @PathVariable int appointmentYearsLower,
            @PathVariable int appointmentYearsUpper,
            @PathVariable int reformYearsLower,
            @PathVariable int reformYearsUpper,
            @RequestBody WageReformStandardRequest request) {
        return payrollService.updateWageReformStandard(
                positionCode,
                appointmentYearsLower,
                appointmentYearsUpper,
                reformYearsLower,
                reformYearsUpper,
                request);
    }

    @DeleteMapping("/wage-reforms/{positionCode}/{appointmentYearsLower}/{appointmentYearsUpper}/{reformYearsLower}/{reformYearsUpper}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteWageReform(
            @PathVariable String positionCode,
            @PathVariable int appointmentYearsLower,
            @PathVariable int appointmentYearsUpper,
            @PathVariable int reformYearsLower,
            @PathVariable int reformYearsUpper) {
        payrollService.deleteWageReformStandard(
                positionCode,
                appointmentYearsLower,
                appointmentYearsUpper,
                reformYearsLower,
                reformYearsUpper);
    }

    @PostMapping("/other-allowances")
    @ResponseStatus(HttpStatus.CREATED)
    OtherAllowanceStandard createOtherAllowance(@RequestBody OtherAllowanceStandardRequest request) {
        return payrollService.createOtherAllowanceStandard(request);
    }

    @PutMapping("/other-allowances/{standardType}/{standardYearMonth}/{code}")
    OtherAllowanceStandard updateOtherAllowanceWithYear(
            @PathVariable String standardType,
            @PathVariable String standardYearMonth,
            @PathVariable String code,
            @RequestBody OtherAllowanceStandardRequest request) {
        return payrollService.updateOtherAllowanceStandard(standardType, standardYearMonth, code, request);
    }

    @PutMapping("/other-allowances/{standardType}/{code}")
    OtherAllowanceStandard updateOtherAllowanceWithoutYear(
            @PathVariable String standardType,
            @PathVariable String code,
            @RequestBody OtherAllowanceStandardRequest request) {
        return payrollService.updateOtherAllowanceStandard(standardType, null, code, request);
    }

    @DeleteMapping("/other-allowances/{standardType}/{standardYearMonth}/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteOtherAllowanceWithYear(
            @PathVariable String standardType,
            @PathVariable String standardYearMonth,
            @PathVariable String code) {
        payrollService.deleteOtherAllowanceStandard(standardType, standardYearMonth, code);
    }

    @DeleteMapping("/other-allowances/{standardType}/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteOtherAllowanceWithoutYear(
            @PathVariable String standardType,
            @PathVariable String code) {
        payrollService.deleteOtherAllowanceStandard(standardType, null, code);
    }
}
