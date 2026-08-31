package com.dxsoft.rsgzgl.ops.security;

public record OpsPasswordChangeRequest(
        String currentPassword,
        String newPassword,
        String confirmPassword
) {
}
