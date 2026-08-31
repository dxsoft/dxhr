package com.dxsoft.rsgzgl.personnel;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

class MobileAttachmentUploadSessionRepositoryTest {

    private MobileAttachmentUploadSessionRepository repository;

    @BeforeEach
    void setUp() {
        EmbeddedDatabase database = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScript("classpath:mobile-attachment-upload-repository-test.sql")
                .build();
        repository = new MobileAttachmentUploadSessionRepository(new NamedParameterJdbcTemplate(database));
    }

    @Test
    void insertAndFindSessionRoundTrip() {
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.SECONDS);
        MobileAttachmentUploadSession session = new MobileAttachmentUploadSession(
                "abc123",
                "jx",
                42,
                null,
                "https://renshi.dxsoft.cn",
                expiresAt,
                List.of());
        repository.insertSession(session);

        MobileAttachmentUploadSession loaded = repository.findByToken("abc123").orElseThrow();
        assertThat(loaded.type()).isEqualTo("jx");
        assertThat(loaded.uid()).isEqualTo(42);
        assertThat(loaded.publicBaseUrl()).isEqualTo("https://renshi.dxsoft.cn");
        assertThat(loaded.expiresAt()).isEqualTo(expiresAt);
        assertThat(loaded.files()).isEmpty();
    }

    @Test
    void insertFileAndMarkConsumed() {
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.MINUTES);
        repository.insertSession(new MobileAttachmentUploadSession(
                "abc123", "jx", 1, null, "https://pq.dxsoft.cn", expiresAt, List.of()));
        MobileAttachmentUploadFile file = new MobileAttachmentUploadFile(
                "file1",
                "stored.jpg",
                "手机拍照.jpg",
                "image/jpeg",
                128,
                Instant.now(),
                false);
        repository.insertFile("abc123", file);

        MobileAttachmentUploadSession loaded = repository.findByToken("abc123").orElseThrow();
        assertThat(loaded.files()).hasSize(1);
        assertThat(loaded.files().getFirst().consumed()).isFalse();

        repository.markFileConsumed("abc123", "file1");
        loaded = repository.findByToken("abc123").orElseThrow();
        assertThat(loaded.files().getFirst().consumed()).isTrue();
    }

    @Test
    void markFileConsumedIfPendingIsIdempotent() {
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.MINUTES);
        repository.insertSession(new MobileAttachmentUploadSession(
                "abc123", "jx", 1, null, "https://pq.dxsoft.cn", expiresAt, List.of()));
        repository.insertFile("abc123", new MobileAttachmentUploadFile(
                "file1", "stored.jpg", "手机拍照.jpg", "image/jpeg", 128, Instant.now(), false));

        assertThat(repository.markFileConsumedIfPending("abc123", "file1")).isTrue();
        assertThat(repository.markFileConsumedIfPending("abc123", "file1")).isFalse();
        assertThat(repository.findByToken("abc123").orElseThrow().files().getFirst().consumed()).isTrue();
    }

    @Test
    void deleteSessionRemovesFiles() {
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.MINUTES);
        repository.insertSession(new MobileAttachmentUploadSession(
                "abc123", "jx", 1, null, "https://pq.dxsoft.cn", expiresAt, List.of()));
        repository.insertFile("abc123", new MobileAttachmentUploadFile(
                "file1", "stored.jpg", "手机拍照.jpg", "image/jpeg", 128, Instant.now(), false));

        List<MobileAttachmentUploadFile> deleted = repository.deleteSession("abc123");

        assertThat(deleted).hasSize(1);
        assertThat(repository.findByToken("abc123")).isEmpty();
    }
}
