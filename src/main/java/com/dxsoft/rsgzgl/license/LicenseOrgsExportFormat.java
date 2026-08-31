package com.dxsoft.rsgzgl.license;

/** 人事 dwbm / cyxx 导出包，供 ops 导入到独立 H2 后签发授权。 */
public final class LicenseOrgsExportFormat {

    /** @deprecated 仅单位目录，不含本地政策；保留供旧 ops 导入 */
    public static final String FORMAT = "RSGZGL_LICENSE_ORGS_V1";

    /** 单位目录 + cyxx 本地工资政策 */
    public static final String SEED_FORMAT = "RSGZGL_LICENSE_SEED_V2";

    private LicenseOrgsExportFormat() {
    }
}
