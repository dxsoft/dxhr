package com.dxsoft.rsgzgl.ops.security;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

class OpsPasswordServiceTest {

    @Test
    void rejectsShortPassword() {
        OpsPasswordService service = new OpsPasswordService(mock(JdbcTemplate.class), mock(PasswordEncoder.class));
        assertThrows(IllegalArgumentException.class, () -> service.changePassword(
                "ops", new OpsPasswordChangeRequest("oldpass1", "short", "short")));
    }

    @Test
    void rejectsMismatch() {
        OpsPasswordService service = new OpsPasswordService(mock(JdbcTemplate.class), mock(PasswordEncoder.class));
        assertThrows(IllegalArgumentException.class, () -> service.changePassword(
                "ops", new OpsPasswordChangeRequest("oldpass1", "newpass12", "newpass13")));
    }

    @Test
    void updatesHashWhenCurrentMatches() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(jdbc.queryForList(anyString(), eq("ops")))
                .thenReturn(List.of(Map.of("password_hash", "hashed-old")));
        when(encoder.matches("oldpass1", "hashed-old")).thenReturn(true);
        when(encoder.encode("newpass12")).thenReturn("hashed-new");
        OpsPasswordService service = new OpsPasswordService(jdbc, encoder);
        service.changePassword("ops", new OpsPasswordChangeRequest("oldpass1", "newpass12", "newpass12"));
        verify(jdbc).update(
                "UPDATE ops_admin SET password_hash = ? WHERE username = ?",
                "hashed-new",
                "ops");
    }
}
