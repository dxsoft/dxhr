package com.dxsoft.rsgzgl.ops.monitor;

import java.util.List;

public record MonitorOverview(
        MonitorSnapshotView latest,
        List<MonitorSnapshotView> history,
        List<MonitorTargetView> targets,
        List<MonitorAlertView> alerts,
        MonitorThresholds thresholds
) {
}
