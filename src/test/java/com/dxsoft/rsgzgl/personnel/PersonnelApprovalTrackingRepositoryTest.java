package com.dxsoft.rsgzgl.personnel;

import static org.assertj.core.api.Assertions.assertThat;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

class PersonnelApprovalTrackingRepositoryTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private PersonnelRepository repository;

    @BeforeEach
    void setUp() {
        EmbeddedDatabase database = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScript("classpath:personnel-approval-tracking-repository-test.sql")
                .build();
        jdbcTemplate = new NamedParameterJdbcTemplate(database);
        repository = new PersonnelRepository(jdbcTemplate);
    }

    @Test
    void findApprovalTracking_returnsSubmittedMainAndSubrecords() {
        seedPerson("001", "00001", "张三", 1);
        jdbcTemplate.getJdbcTemplate().update("""
                UPDATE dryjbxx SET bbz = '申报' WHERE uid = 1
                """);
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO dxl (dwbm, grbm, xl, byyx, bbz) VALUES ('001', '00001', '本科', '测试大学', '申报')
                """);
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO dryzwbh (dwbm, grbm, xrzw, bbz) VALUES ('001', '00001', '科员', '审批通过')
                """);

        List<PersonnelApprovalTrackingRecord> pending = repository.findApprovalTracking(
                OrganizationScope.unrestricted(),
                null,
                null,
                PersonnelApprovalStatuses.SUBMITTED,
                false,
                "alice",
                null,
                null,
                null,
                PageRequest.of(0, 20));
        List<PersonnelApprovalTrackingRecord> approved = repository.findApprovalTracking(
                OrganizationScope.unrestricted(),
                null,
                null,
                PersonnelApprovalStatuses.APPROVED,
                false,
                "alice",
                null,
                null,
                null,
                PageRequest.of(0, 20));

        assertThat(pending).hasSize(2);
        assertThat(pending).extracting(PersonnelApprovalTrackingRecord::recordType)
                .containsExactlyInAnyOrder("main", "education");
        assertThat(approved).hasSize(1);
        assertThat(approved.getFirst().recordType()).isEqualTo("position");
        assertThat(repository.countApprovalTracking(
                OrganizationScope.unrestricted(), null, null, PersonnelApprovalStatuses.SUBMITTED, false, "alice", null, null, null))
                .isEqualTo(2);
    }

    @Test
    void findApprovalTracking_filtersBySubmittedByMe() {
        seedPerson("001", "00001", "张三", 1);
        jdbcTemplate.getJdbcTemplate().update("""
                UPDATE dryjbxx SET bbz = '申报', tjr = 'alice', tjsj = CURRENT_TIMESTAMP WHERE uid = 1
                """);
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO dxl (dwbm, grbm, xl, bbz, tjr, tjsj) VALUES ('001', '00001', '硕士', '申报', 'bob', CURRENT_TIMESTAMP)
                """);
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO app_security_audit_log (actor_username, action, target_type, target_id, summary)
                VALUES ('alice', 'PERSONNEL_APPROVAL_SUBMIT', 'personnel', '1', '主表提交')
                """);
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO app_security_audit_log (actor_username, action, target_type, target_id, summary)
                VALUES ('bob', 'PERSONNEL_SUBRECORD_SUBMIT', 'dxl', '1', '学历提交')
                """);

        List<PersonnelApprovalTrackingRecord> mine = repository.findApprovalTracking(
                OrganizationScope.unrestricted(),
                null,
                null,
                PersonnelApprovalStatuses.SUBMITTED,
                true,
                "alice",
                null,
                null,
                null,
                PageRequest.of(0, 20));

        assertThat(mine).hasSize(1);
        assertThat(mine.getFirst().recordType()).isEqualTo("main");
        assertThat(mine.getFirst().auditTargetType()).isEqualTo("personnel");
    }

    @Test
    void findApprovalTracking_respectsOrganizationScope() {
        seedPerson("001", "00001", "张三", 1);
        seedPerson("002", "00002", "李四", 2);
        jdbcTemplate.getJdbcTemplate().update("UPDATE dryjbxx SET bbz = '申报' WHERE uid IN (1, 2)");

        List<PersonnelApprovalTrackingRecord> scoped = repository.findApprovalTracking(
                OrganizationScope.custom(java.util.Set.of("001")),
                null,
                null,
                PersonnelApprovalStatuses.SUBMITTED,
                false,
                "alice",
                null,
                null,
                null,
                PageRequest.of(0, 20));

        assertThat(scoped).hasSize(1);
        assertThat(scoped.getFirst().personName()).isEqualTo("张三");
    }

    @Test
    void findApprovalTracking_approvedWithinDays_excludesLegacyWithoutAuditLog() {
        seedPerson("001", "00001", "张三", 1);
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO dndkh (dwbm, grbm, khnd, khjg, bbz) VALUES ('001', '00001', '2010', '合格', '审批通过')
                """);
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO dndkh (dwbm, grbm, khnd, khjg, bbz, shr, shsj) VALUES ('001', '00001', '2024', '合格', '审批通过', 'auditor', CURRENT_TIMESTAMP)
                """);

        List<PersonnelApprovalTrackingRecord> recentOnly = repository.findApprovalTracking(
                OrganizationScope.unrestricted(),
                null,
                null,
                PersonnelApprovalStatuses.APPROVED,
                false,
                "alice",
                90,
                null,
                null,
                PageRequest.of(0, 20));
        List<PersonnelApprovalTrackingRecord> allApproved = repository.findApprovalTracking(
                OrganizationScope.unrestricted(),
                null,
                null,
                PersonnelApprovalStatuses.APPROVED,
                false,
                "alice",
                null,
                null,
                null,
                PageRequest.of(0, 20));

        assertThat(recentOnly).hasSize(1);
        assertThat(recentOnly.getFirst().summary()).contains("2024");
        assertThat(allApproved).hasSize(2);
    }

    @Test
    void findApprovalTracking_approvedWithinDays_ignoredForSubmittedStatus() {
        seedPerson("001", "00001", "张三", 1);
        jdbcTemplate.getJdbcTemplate().update("""
                UPDATE dryjbxx SET bbz = '申报', tjr = 'alice', tjsj = CURRENT_TIMESTAMP WHERE uid = 1
                """);

        List<PersonnelApprovalTrackingRecord> pending = repository.findApprovalTracking(
                OrganizationScope.unrestricted(),
                null,
                null,
                PersonnelApprovalStatuses.SUBMITTED,
                false,
                "alice",
                90,
                null,
                null,
                PageRequest.of(0, 20));

        assertThat(pending).hasSize(1);
    }

    @Test
    void findApprovalTracking_keywordMatchesFullPersonCode() {
        seedPerson("00601", "00018", "翁宁波", 3631);
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO dryzwbh (dwbm, grbm, xrzw, bbz, shr, shsj) VALUES ('00601', '00018', '机关高级技师', '审批通过', 'admin', CURRENT_TIMESTAMP)
                """);

        List<PersonnelApprovalTrackingRecord> byFullCode = repository.findApprovalTracking(
                OrganizationScope.unrestricted(),
                null,
                "00601-00018",
                PersonnelApprovalStatuses.APPROVED,
                false,
                "alice",
                90,
                null,
                null,
                PageRequest.of(0, 20));
        List<PersonnelApprovalTrackingRecord> byPartialCode = repository.findApprovalTracking(
                OrganizationScope.unrestricted(),
                null,
                "00018",
                PersonnelApprovalStatuses.APPROVED,
                false,
                "alice",
                90,
                null,
                null,
                PageRequest.of(0, 20));

        assertThat(byFullCode).hasSize(1);
        assertThat(byFullCode.getFirst().recordType()).isEqualTo("position");
        assertThat(byPartialCode).hasSize(1);
    }

    @Test
    void findApprovalTracking_includesPositionFieldsAndAttachmentCount() {
        seedPerson("001", "00001", "张三", 1);
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO dryzwbh (dwbm, grbm, xrzw, srny, bbz)
                VALUES ('001', '00001', '一级检察官', '2026.06', '申报')
                """);
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO app_subrecord_attachment (table_name, record_id, original_name, stored_name, file_size)
                VALUES ('dryzwbh', 1, 'test.pdf', 'stored.pdf', 1024)
                """);

        List<PersonnelApprovalTrackingRecord> rows = repository.findApprovalTracking(
                OrganizationScope.unrestricted(),
                null,
                null,
                PersonnelApprovalStatuses.SUBMITTED,
                false,
                "alice",
                null,
                null,
                null,
                PageRequest.of(0, 20));

        assertThat(rows).hasSize(1);
        PersonnelApprovalTrackingRecord row = rows.getFirst();
        assertThat(row.recordType()).isEqualTo("position");
        assertThat(row.positionName()).isEqualTo("一级检察官");
        assertThat(row.effectiveYearMonth()).isEqualTo("2026.06");
        assertThat(row.attachmentCount()).isEqualTo(1);
    }

    @Test
    void findApprovalTracking_filtersByRecordTypeAndAssessmentYear() {
        seedPerson("001", "00001", "张三", 1);
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO dndkh (dwbm, grbm, khnd, khjg, bbz) VALUES ('001', '00001', '2024', '优秀', '申报')
                """);
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO dndkh (dwbm, grbm, khnd, khjg, bbz) VALUES ('001', '00001', '2023', '称职', '申报')
                """);
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO dxl (dwbm, grbm, xl, bbz) VALUES ('001', '00001', '本科', '申报')
                """);

        List<PersonnelApprovalTrackingRecord> assessments2024 = repository.findApprovalTracking(
                OrganizationScope.unrestricted(),
                null,
                null,
                PersonnelApprovalStatuses.SUBMITTED,
                false,
                "alice",
                null,
                "assessment",
                "2024",
                PageRequest.of(0, 20));

        assertThat(assessments2024).hasSize(1);
        assertThat(assessments2024.getFirst().recordType()).isEqualTo("assessment");
        assertThat(assessments2024.getFirst().summary()).contains("2024");
    }

    @Test
    void findAssessmentApprovalStats_calculatesExcellentRatio() {
        seedPerson("001", "00001", "张三", 1);
        jdbcTemplate.getJdbcTemplate().update(
                "INSERT INTO dryjbxx (uid, dwbm, grbm, xm, bbz) VALUES (?, ?, ?, ?, ?)",
                2,
                "001",
                "00002",
                "李四",
                "草稿");
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO dndkh (dwbm, grbm, khnd, khjg, bbz) VALUES ('001', '00001', '2025', '优秀', '申报')
                """);
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO dndkh (dwbm, grbm, khnd, khjg, bbz) VALUES ('001', '00002', '2025', '称职', '审批通过')
                """);

        AssessmentApprovalStats stats = repository.findAssessmentApprovalStats(
                OrganizationScope.unrestricted(),
                "001",
                "2025",
                false).orElseThrow();

        assertThat(stats.participantCount()).isEqualTo(2);
        assertThat(stats.excellentCount()).isEqualTo(1);
        assertThat(stats.excellentRatio()).isEqualByComparingTo("50.0");
        assertThat(stats.pendingCount()).isEqualTo(1);
        assertThat(stats.approvedCount()).isEqualTo(1);
    }

    private void seedPerson(String organizationCode, String personCode, String name, int uid) {
        jdbcTemplate.getJdbcTemplate().update(
                "INSERT INTO dwbm (dwbm, dwmc) VALUES (?, ?)",
                organizationCode,
                "测试单位" + organizationCode);
        jdbcTemplate.getJdbcTemplate().update(
                "INSERT INTO dryjbxx (uid, dwbm, grbm, xm, bbz) VALUES (?, ?, ?, ?, ?)",
                uid,
                organizationCode,
                personCode,
                name,
                "草稿");
    }
}
