package com.dxsoft.rsgzgl.backup;

import java.util.List;

public record BackupInspectResult(
        BackupFormat format,
        String formatLabel,
        String markerFile,
        String legacyVersion,
        List<String> tableFiles,
        List<BackupTableScopes.ScopeMatch> availableScopes,
        String organizationHint,
        String message
) {
}
