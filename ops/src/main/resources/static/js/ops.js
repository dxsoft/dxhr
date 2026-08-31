(() => {
    const statusEl = document.getElementById("status");
    const provLog = document.getElementById("prov-log");
    let softKeyConnected = 0;
    let busy = false;
    let lastOrgRows = [];
    let pendingIssueSelection = null;
    let monitorTimer = null;

    function setStatus(type, text) {
        statusEl.className = "message " + type;
        statusEl.textContent = text;
    }

    function log(line) {
        provLog.textContent += line + "\n";
        provLog.scrollTop = provLog.scrollHeight;
    }

    function problemMessage(payload, fallback) {
        if (!payload) return fallback;
        return payload.detail || payload.message || payload.error || payload.title
            || (payload.status ? ("请求失败：" + payload.status) : fallback);
    }

    function randomEncAlgoKey() {
        const bytes = new Uint8Array(16);
        crypto.getRandomValues(bytes);
        return Array.from(bytes, (b) => b.toString(16).padStart(2, "0")).join("").toUpperCase();
    }

    async function api(url, options = {}) {
        const response = await fetch(url, {
            credentials: "same-origin",
            headers: {
                Accept: "application/json",
                ...(options.body ? { "Content-Type": "application/json" } : {}),
                ...(options.headers || {}),
            },
            ...options,
        });
        if (response.status === 401) {
            location.href = "/login.html";
            throw new Error("登录已失效");
        }
        const contentType = response.headers.get("content-type") || "";
        if (contentType.includes("application/json")) {
            const payload = await response.json();
            if (!response.ok) {
                throw new Error(problemMessage(payload, "请求失败"));
            }
            return payload;
        }
        if (!response.ok) {
            throw new Error("请求失败：" + response.status);
        }
        return response;
    }

    function switchTab(name) {
        document.querySelectorAll(".tabs button").forEach((btn) => {
            btn.classList.toggle("active", btn.dataset.tab === name);
        });
        document.querySelectorAll(".panel").forEach((panel) => {
            panel.classList.toggle("hidden", panel.id !== "tab-" + name);
        });
        stopMonitorTimer();
        if (name === "monitor") {
            refreshMonitor().catch(showErr);
            monitorTimer = setInterval(() => refreshMonitor().catch(showErr), 15000);
        }
        if (name === "devices") refreshDevices();
        if (name === "orgs") {
            refreshOrgs();
            refreshLocalPolicyStatus().catch(showErr);
        }
        if (name === "issue") {
            refreshOrgsForIssue();
            refreshIssueLogs();
            refreshLocalPolicyStatus().catch(showErr);
        }
    }

    document.querySelectorAll("[data-tab]").forEach((btn) => {
        btn.addEventListener("click", () => switchTab(btn.dataset.tab));
    });

    function probeSoftKey() {
        if (navigator.userAgent.indexOf("MSIE") > 0 && !(navigator.userAgent.indexOf("opera") > -1)) {
            return;
        }
        try {
            const probe = new SoftKey6W();
            probe.Socket_UK.onopen = function () {
                softKeyConnected = 1;
            };
            probe.Socket_UK.onmessage = function () {};
            probe.Socket_UK.onclose = function () {};
        } catch (e) {
            // shown when provisioning
        }
    }

    async function registerDevice(payload) {
        return api("/api/ukey/devices", {
            method: "POST",
            body: JSON.stringify(payload),
        });
    }

    const DEMO_PRI = "128B2FA8BD433C6C068C8D803DFF79792A519A55171B1B650C23661D15897263";
    const DEMO_PUB_X = "D5548C7825CBB56150A3506CD57464AF8A1AE0519DFAF3C58221DC810CAF28DD";
    const DEMO_PUB_Y = "921073768FE3D59CE54E79A49445CF73FED23086537027264D168946D479533E";

    function setProvisionBusy(on) {
        busy = on;
        ["prov-run", "prov-run-demo", "prov-run-custom", "prov-run-enc"].forEach((id) => {
            const el = document.getElementById(id);
            if (el) el.disabled = on;
        });
    }

    function readProvisionForm() {
        return {
            sm2UserId: document.getElementById("prov-sm2-id").value.trim(),
            pin: document.getElementById("prov-pin").value.trim() || "123",
            username: document.getElementById("prov-username").value.trim(),
            orgCode: document.getElementById("prov-org").value.trim(),
            note: document.getElementById("prov-note").value.trim(),
            pri: document.getElementById("prov-pri").value.trim(),
            pubX: document.getElementById("prov-pub-x").value.trim(),
            pubY: document.getElementById("prov-pub-y").value.trim(),
        };
    }

    /**
     * mode: "generate" | "fixed"
     * fixedKeys: { pri, pubX, pubY } when mode === "fixed"
     */
    function provisionCurrentLock(mode, fixedKeys) {
        if (busy) return;
        const form = readProvisionForm();
        if (!form.sm2UserId) {
            setStatus("error", "请填写 SM2 用户身份");
            return;
        }
        if (!softKeyConnected) {
            setStatus("error", "未能连接 SoftKey 服务程序，请确认已安装并启动。");
            return;
        }
        if (mode === "fixed") {
            if (!fixedKeys || !fixedKeys.pri || !fixedKeys.pubX || !fixedKeys.pubY) {
                setStatus("error", "缺少私钥或公钥");
                return;
            }
        }

        setProvisionBusy(true);
        provLog.textContent = "";
        setStatus("hint", mode === "fixed" ? "正在写入指定密钥对…" : "正在锁内生成并写入…");
        log(mode === "fixed" ? "开始：写入指定/演示密钥对（跳过锁内生成）" : "开始：锁内生成密钥对");

        let DevicePath = "";
        let chipId = "";
        let PriKey = mode === "fixed" ? fixedKeys.pri : "";
        let PubKeyX = mode === "fixed" ? fixedKeys.pubX : "";
        let PubKeyY = mode === "fixed" ? fixedKeys.pubY : "";

        try {
            const s = new SoftKey6W();
            s.Socket_UK.onopen = function () {
                s.ResetOrder();
            };
            s.Socket_UK.onmessage = async function (Msg) {
                const UK_Data = JSON.parse(Msg.data);
                if (UK_Data.type !== "Process") return;
                const fail = (text) => {
                    s.Socket_UK.close();
                    setProvisionBusy(false);
                    setStatus("error", text);
                    log(text);
                };
                const finishRegister = async () => {
                    s.Socket_UK.close();
                    try {
                        const saved = await registerDevice({
                            chipId,
                            sm2UserId: form.sm2UserId,
                            pubkeyX: PubKeyX,
                            pubkeyY: PubKeyY,
                            authModes: "SM2",
                            username: form.username,
                            orgCode: form.orgCode,
                            note: form.note || (mode === "fixed" ? "指定/演示密钥对写入" : ""),
                        });
                        log("已登记台账：" + saved.chipId);
                        setStatus("success", "制锁成功并已写入台账。");
                    } catch (e) {
                        setStatus("error", e.message || "登记台账失败");
                        log(e.message || "登记台账失败");
                    } finally {
                        setProvisionBusy(false);
                    }
                };

                if (mode === "fixed") {
                    switch (UK_Data.order) {
                        case 0:
                            s.FindPort(0);
                            break;
                        case 1:
                            if (UK_Data.LastError !== 0) return fail("未发现加密锁，请插入加密锁。");
                            DevicePath = UK_Data.return_value;
                            log("锁路径：" + DevicePath);
                            s.GetChipID(DevicePath);
                            break;
                        case 2:
                            if (UK_Data.LastError !== 0) return fail("读取芯片 ID 失败：" + UK_Data.LastError);
                            chipId = UK_Data.return_value;
                            log("芯片 ID：" + chipId);
                            log("写入 SM2 密钥对与身份…");
                            s.Set_SM2_KeyPair(PriKey, PubKeyX, PubKeyY, form.sm2UserId, DevicePath);
                            break;
                        case 3:
                            if (UK_Data.LastError !== 0) {
                                return fail("写入密钥对失败：" + UK_Data.LastError
                                    + "（若 PIN 非默认，请先用厂商工具改 PIN；或锁不支持 SM2）");
                            }
                            s.GetPubKeyX(DevicePath);
                            break;
                        case 4:
                            if (UK_Data.LastError !== 0) return fail("回读公钥 X 失败：" + UK_Data.LastError);
                            PubKeyX = UK_Data.return_value;
                            s.GetPubKeyY(DevicePath);
                            break;
                        case 5:
                            if (UK_Data.LastError !== 0) return fail("回读公钥 Y 失败：" + UK_Data.LastError);
                            PubKeyY = UK_Data.return_value;
                            log("回读公钥 X：" + PubKeyX);
                            log("回读公钥 Y：" + PubKeyY);
                            await finishRegister();
                            break;
                    }
                    return;
                }

                switch (UK_Data.order) {
                    case 0:
                        s.FindPort(0);
                        break;
                    case 1:
                        if (UK_Data.LastError !== 0) return fail("未发现加密锁，请插入加密锁。");
                        DevicePath = UK_Data.return_value;
                        log("锁路径：" + DevicePath);
                        s.GetChipID(DevicePath);
                        break;
                    case 2:
                        if (UK_Data.LastError !== 0) return fail("读取芯片 ID 失败：" + UK_Data.LastError);
                        chipId = UK_Data.return_value;
                        log("芯片 ID：" + chipId);
                        s.StarGenKeyPair(DevicePath);
                        break;
                    case 3:
                        if (UK_Data.LastError !== 0) {
                            return fail("生成密钥对失败：" + UK_Data.LastError
                                + "。请改用「写入演示密钥对」。");
                        }
                        log("已生成密钥对，读取私钥…");
                        s.GenPriKey();
                        break;
                    case 4:
                        if (UK_Data.LastError !== 0) return fail("读取私钥失败：" + UK_Data.LastError);
                        PriKey = UK_Data.return_value;
                        s.GenPubKeyX();
                        break;
                    case 5:
                        if (UK_Data.LastError !== 0) return fail("读取公钥 X 失败：" + UK_Data.LastError);
                        PubKeyX = UK_Data.return_value;
                        s.GenPubKeyY();
                        break;
                    case 6:
                        if (UK_Data.LastError !== 0) return fail("读取公钥 Y 失败：" + UK_Data.LastError);
                        PubKeyY = UK_Data.return_value;
                        log("写入 SM2 密钥对与身份…");
                        s.Set_SM2_KeyPair(PriKey, PubKeyX, PubKeyY, form.sm2UserId, DevicePath);
                        PriKey = "";
                        break;
                    case 7:
                        if (UK_Data.LastError !== 0) return fail("写入密钥对失败：" + UK_Data.LastError);
                        s.GetPubKeyX(DevicePath);
                        break;
                    case 8:
                        if (UK_Data.LastError !== 0) return fail("回读公钥 X 失败：" + UK_Data.LastError);
                        PubKeyX = UK_Data.return_value;
                        s.GetPubKeyY(DevicePath);
                        break;
                    case 9:
                        if (UK_Data.LastError !== 0) return fail("回读公钥 Y 失败：" + UK_Data.LastError);
                        PubKeyY = UK_Data.return_value;
                        await finishRegister();
                        break;
                }
            };
            s.Socket_UK.onclose = function () {};
        } catch (e) {
            setProvisionBusy(false);
            setStatus("error", e.message || "无法启动 SoftKey 客户端");
        }
    }

    function runCustomProvision() {
        const form = readProvisionForm();
        const pri = form.pri || DEMO_PRI;
        const pubX = form.pubX || DEMO_PUB_X;
        const pubY = form.pubY || DEMO_PUB_Y;
        if (!form.pri && !form.pubX && !form.pubY) {
            setStatus("hint", "未填指定密钥，将使用演示密钥对写入。");
        }
        if (!form.sm2UserId) {
            setStatus("error", "请填写 SM2 用户身份");
            return;
        }
        provisionCurrentLock("fixed", { pri, pubX, pubY });
    }

    function runDemoProvision() {
        const form = readProvisionForm();
        if (!form.sm2UserId) {
            setStatus("error", "请填写 SM2 用户身份");
            return;
        }
        provisionCurrentLock("fixed", {
            pri: DEMO_PRI,
            pubX: DEMO_PUB_X,
            pubY: DEMO_PUB_Y,
        });
    }

    async function runEncProvision() {
        if (busy) return;
        const form = readProvisionForm();
        if (!softKeyConnected) {
            setStatus("error", "未能连接 SoftKey 服务程序，请确认已安装并启动。");
            return;
        }
        setProvisionBusy(true);
        provLog.textContent = "";
        setStatus("hint", "正在写入增强算法密钥…");
        log("开始：增强算法 SetCal_2");

        let encAlgoKey;
        try {
            const keyResp = await api("/api/ukey/enc-key", { method: "POST", body: "{}" });
            encAlgoKey = keyResp.encAlgoKey;
            log("已生成增强密钥（服务端）：" + encAlgoKey);
        } catch (e) {
            // 旧版 ops 无 /enc-key 时本地生成，写入锁后仍会登记到台账
            encAlgoKey = randomEncAlgoKey();
            log("服务端生成失败（" + (e.message || "未知") + "），改用本地生成：" + encAlgoKey);
        }

        let DevicePath = "";
        let chipId = "";
        try {
            const s = new SoftKey6W();
            s.Socket_UK.onopen = function () {
                s.ResetOrder();
            };
            s.Socket_UK.onmessage = async function (Msg) {
                const UK_Data = JSON.parse(Msg.data);
                if (UK_Data.type !== "Process") return;
                const fail = (text) => {
                    s.Socket_UK.close();
                    setProvisionBusy(false);
                    setStatus("error", text);
                    log(text);
                };
                switch (UK_Data.order) {
                    case 0:
                        s.FindPort(0);
                        break;
                    case 1:
                        if (UK_Data.LastError !== 0) return fail("未发现加密锁，请插入加密锁。");
                        DevicePath = UK_Data.return_value;
                        log("锁路径：" + DevicePath);
                        s.GetChipID(DevicePath);
                        break;
                    case 2:
                        if (UK_Data.LastError !== 0) return fail("读取芯片 ID 失败：" + UK_Data.LastError);
                        chipId = UK_Data.return_value;
                        log("芯片 ID：" + chipId);
                        log("写入增强算法密钥 SetCal_2…");
                        s.SetCal_2(encAlgoKey, DevicePath);
                        break;
                    case 3:
                        if (UK_Data.LastError !== 0) {
                            return fail("写入增强算法密钥失败：" + UK_Data.LastError);
                        }
                        s.Socket_UK.close();
                        try {
                            const saved = await registerDevice({
                                chipId,
                                encAlgoKey,
                                authModes: "ENC",
                                username: form.username,
                                orgCode: form.orgCode,
                                note: form.note || "增强算法密钥写入",
                            });
                            log("已登记台账：" + saved.chipId + " 模式=" + (saved.authModes || "ENC"));
                            setStatus("success", "增强算法制锁成功，请导出绑定包到人事系统。");
                        } catch (e) {
                            setStatus("error", e.message || "登记台账失败");
                            log(e.message || "登记台账失败");
                        } finally {
                            setProvisionBusy(false);
                        }
                        break;
                }
            };
            s.Socket_UK.onclose = function () {};
        } catch (e) {
            setProvisionBusy(false);
            setStatus("error", e.message || "无法启动 SoftKey 客户端");
        }
    }

    async function refreshDevices() {
        const keyword = document.getElementById("device-filter").value.trim();
        const rows = await api("/api/ukey/devices" + (keyword ? ("?keyword=" + encodeURIComponent(keyword)) : ""));
        document.getElementById("device-rows").innerHTML = rows.map((d) => `
            <tr>
                <td>${esc(d.chipId)}</td>
                <td>${esc(d.authModes || "")}</td>
                <td>${esc(d.sm2UserId || "")}</td>
                <td>${esc(d.username || "")}</td>
                <td title="${esc(d.encAlgoKey || "")}">${esc(shortHex(d.encAlgoKey || ""))}</td>
                <td title="${esc(d.pubkeyX || "")}">${esc(shortHex(d.pubkeyX || ""))}</td>
                <td>${esc(d.note || "")}</td>
                <td>${esc(d.provisionedAt || "")}</td>
            </tr>
        `).join("") || `<tr><td colspan="8">暂无记录</td></tr>`;
    }

    async function exportBindings() {
        const keyword = document.getElementById("device-filter").value.trim();
        const url = "/api/ukey/bindings/export" + (keyword ? ("?keyword=" + encodeURIComponent(keyword)) : "");
        const response = await fetch(url, { credentials: "same-origin" });
        if (!response.ok) {
            setStatus("error", "导出失败");
            return;
        }
        const blob = await response.blob();
        const a = document.createElement("a");
        a.href = URL.createObjectURL(blob);
        a.download = "ukey-bind-v1.json";
        a.click();
        URL.revokeObjectURL(a.href);
        setStatus("success", "已导出绑定包。");
    }

    async function refreshOrgs() {
        const rows = await api("/api/license/orgs");
        lastOrgRows = rows;
        document.getElementById("org-rows").innerHTML = rows.map((o) => `
            <tr>
                <td><input class="org-check" type="checkbox" value="${esc(o.organizationCode)}"
                    data-name="${esc(o.name || "")}"
                    data-level="${esc(o.organizationLevel || "")}"
                    data-city="${esc(o.city || "")}"
                    data-supervisor="${esc(o.supervisor || "")}"></td>
                <td>${esc(o.organizationCode)}</td>
                <td>${esc(o.name)}</td>
                <td>${esc(o.organizationLevel || "")}</td>
                <td>${esc(o.city || "")}</td>
                <td>${o.issued ? ("已签发" + (o.lastIssuedAt ? ("<br><span class=\"muted\">" + esc(o.lastIssuedAt) + "</span>") : "")) : "未签发"}</td>
                <td><button type="button" data-del-org="${esc(o.organizationCode)}">删除</button></td>
            </tr>
        `).join("") || `<tr><td colspan="7">暂无单位</td></tr>`;
        const checkAll = document.getElementById("org-check-all");
        if (checkAll) checkAll.checked = false;
        document.querySelectorAll("[data-del-org]").forEach((btn) => {
            btn.addEventListener("click", async () => {
                await api("/api/license/orgs/" + encodeURIComponent(btn.dataset.delOrg), { method: "DELETE" });
                setStatus("success", "已删除单位。");
                refreshOrgs();
                refreshOrgsForIssue();
            });
        });
    }

    function expandCodesWithSubordinates(rootCodes, allRows) {
        const roots = [...new Set(rootCodes.map((c) => String(c || "").trim()).filter(Boolean))];
        const selected = new Set(roots);
        for (const row of allRows) {
            const code = String(row.organizationCode || "").trim();
            if (!code) continue;
            for (const root of roots) {
                if (code === root || (code.startsWith(root) && code.length > root.length)) {
                    selected.add(code);
                    break;
                }
            }
        }
        return [...selected];
    }

    function applyPendingIssueSelection(sel) {
        if (!sel) return;
        document.getElementById("issue-subject-pick").value = sel.subjectCode || "";
        document.getElementById("issue-code").value = sel.subjectCode || "";
        document.getElementById("issue-name").value = sel.subjectName || "";
        document.getElementById("issue-level").value = sel.level || "";
        document.getElementById("issue-city").value = sel.city || "";
        document.getElementById("issue-supervisor").value = sel.supervisor || "";
        const includeSub = sel.includeSubordinates !== false;
        document.getElementById("issue-include-subordinates").checked = includeSub;
        document.getElementById("issue-include-all").checked = false;
        // 多选框只勾选根单位，下属由后端按前缀展开
        const select = document.getElementById("issue-orgs");
        const roots = sel.codes || [];
        const codeSet = new Set(roots);
        [...select.options].forEach((opt) => {
            opt.selected = codeSet.has(opt.value);
        });
        const previewCount = includeSub
            ? expandCodesWithSubordinates(roots, lastOrgRows).length
            : roots.length;
        setStatus("hint", "已带入选中单位 " + roots.length + " 个"
            + (includeSub ? ("（预计含下属共 " + previewCount + " 个）") : "")
            + "，确认后点击「签发并下载」。");
    }

    async function issueSelectedOrgs() {
        const checks = [...document.querySelectorAll(".org-check:checked")];
        if (!checks.length) {
            setStatus("error", "请先勾选要签发的单位");
            return;
        }
        const items = checks.map((c) => ({
            code: c.value,
            name: c.dataset.name || "",
            level: c.dataset.level || "",
            city: c.dataset.city || "",
            supervisor: c.dataset.supervisor || "",
        }));
        items.sort((a, b) => a.code.length - b.code.length || a.code.localeCompare(b.code, "zh"));
        const subject = items[0];
        pendingIssueSelection = {
            subjectCode: subject.code,
            subjectName: subject.name,
            level: subject.level,
            city: subject.city,
            supervisor: subject.supervisor,
            codes: items.map((i) => i.code),
            includeSubordinates: true,
        };
        switchTab("issue");
    }

    async function saveOrg() {
        const body = {
            organizationCode: document.getElementById("org-code").value.trim(),
            name: document.getElementById("org-name").value.trim(),
            shortName: document.getElementById("org-short").value.trim(),
            organizationLevel: document.getElementById("org-level").value.trim(),
            city: document.getElementById("org-city").value.trim(),
            supervisor: document.getElementById("org-supervisor").value.trim(),
            property: document.getElementById("org-property").value.trim(),
            category: document.getElementById("org-category").value.trim(),
            payrollCategory: document.getElementById("org-payroll").value.trim(),
        };
        await api("/api/license/orgs", { method: "PUT", body: JSON.stringify(body) });
        setStatus("success", "单位已保存。");
        refreshOrgs();
        refreshOrgsForIssue();
    }

    async function importCsv() {
        const csv = document.getElementById("org-csv").value;
        const result = await api("/api/license/orgs/import-csv", {
            method: "POST",
            body: JSON.stringify({ csv }),
        });
        setStatus("success", "CSV 导入完成，保存 " + result.saved + " 条。");
        refreshOrgs();
        refreshOrgsForIssue();
    }

    async function refreshLocalPolicyStatus() {
        let policy;
        try {
            policy = await api("/api/license/local-policy");
        } catch (error) {
            policy = { synced: false };
        }
        const text = policy.synced
            ? `本地工资政策已同步：奖金结余模式=${policy.bonusBalanceMode ?? "-"}，保留小数=${policy.roundingMode || "-"}，四舍五入=${policy.roundToInteger || "-"}`
            : "尚未同步本地工资政策：请先在「单位目录」导入人事系统导出的 license-seed-v2.json，否则目标租户 cyxx 政策将为空。";
        const cls = policy.synced ? "hint" : "error";
        for (const id of ["local-policy-status-orgs", "local-policy-status-issue"]) {
            const el = document.getElementById(id);
            if (el) {
                el.className = "status " + cls;
                el.textContent = text;
            }
        }
        return policy;
    }

    async function importOrgsJson() {
        const file = document.getElementById("org-json-file")?.files?.[0];
        if (!file) {
            setStatus("error", "请先选择人事导出的 license-seed-v2.json");
            return;
        }
        setStatus("hint", "正在导入签发种子（" + file.name + "）…");
        const form = new FormData();
        form.append("file", file);
        const response = await fetch("/api/license/import-seed-file", {
            method: "POST",
            credentials: "same-origin",
            body: form,
        });
        if (response.status === 401) {
            location.href = "/login.html";
            throw new Error("登录已失效");
        }
        const contentType = response.headers.get("content-type") || "";
        let payload = null;
        if (contentType.includes("application/json")) {
            payload = await response.json();
        } else {
            const text = await response.text();
            if (!response.ok) {
                throw new Error(text || ("请求失败：" + response.status));
            }
        }
        if (!response.ok) {
            throw new Error(problemMessage(payload, "导入失败（HTTP " + response.status + "）"));
        }
        const result = payload;
        const policyHint = result.localPolicySynced
            ? "，已同步本地工资政策"
            : "，未含本地政策（请使用 license-seed-v2.json）";
        setStatus("success", "签发种子导入完成，单位 " + result.organizationsSaved + " 条" + policyHint + "。");
        document.getElementById("org-json-file").value = "";
        refreshOrgs();
        refreshOrgsForIssue();
        refreshLocalPolicyStatus();
    }

    async function refreshOrgsForIssue() {
        const rows = await api("/api/license/orgs");
        lastOrgRows = rows;
        const select = document.getElementById("issue-orgs");
        select.innerHTML = rows.map((o) =>
            `<option value="${esc(o.organizationCode)}">${esc(o.organizationCode)} ${esc(o.name)}</option>`
        ).join("");
        const pick = document.getElementById("issue-subject-pick");
        const previous = pick.value;
        pick.innerHTML = `<option value="">— 手填或不选 —</option>` + rows.map((o) =>
            `<option value="${esc(o.organizationCode)}"
                data-name="${esc(o.name || "")}"
                data-level="${esc(o.organizationLevel || "")}"
                data-city="${esc(o.city || "")}"
                data-supervisor="${esc(o.supervisor || "")}">${esc(o.organizationCode)} ${esc(o.name)}</option>`
        ).join("");
        if (previous && [...pick.options].some((opt) => opt.value === previous)) {
            pick.value = previous;
        }
        if (pendingIssueSelection) {
            const sel = pendingIssueSelection;
            pendingIssueSelection = null;
            applyPendingIssueSelection(sel);
        }
    }

    function applyIssueSubjectPick() {
        const pick = document.getElementById("issue-subject-pick");
        const option = pick.selectedOptions[0];
        if (!option || !option.value) {
            return;
        }
        document.getElementById("issue-code").value = option.value;
        document.getElementById("issue-name").value = option.dataset.name || "";
        document.getElementById("issue-level").value = option.dataset.level || "";
        document.getElementById("issue-city").value = option.dataset.city || "";
        document.getElementById("issue-supervisor").value = option.dataset.supervisor || "";
    }

    async function issueLicense() {
        const selected = Array.from(document.getElementById("issue-orgs").selectedOptions).map((o) => o.value);
        const includeAll = document.getElementById("issue-include-all").checked;
        const body = {
            organizationCode: document.getElementById("issue-code").value.trim(),
            organizationName: document.getElementById("issue-name").value.trim(),
            organizationLevel: document.getElementById("issue-level").value.trim(),
            city: document.getElementById("issue-city").value.trim(),
            supervisor: document.getElementById("issue-supervisor").value.trim(),
            issuer: document.getElementById("issue-issuer").value.trim(),
            expiresAt: document.getElementById("issue-expires").value || null,
            organizationCodes: includeAll ? [] : selected,
            ukeyEnabled: document.getElementById("issue-ukey-enabled").checked,
            ukeyRequired: document.getElementById("issue-ukey-required").checked,
            includeSubordinates: document.getElementById("issue-include-subordinates").checked,
            includeAllOrganizations: includeAll,
        };
        if (!body.organizationCode || !body.organizationName) {
            throw new Error("请填写签约主体编码与名称");
        }
        if (!includeAll && selected.length === 0) {
            setStatus("hint", "未勾选包含单位：将仅签发签约主体"
                + (body.includeSubordinates ? "及其下属" : "") + "…");
        }
        const policy = await refreshLocalPolicyStatus();
        if (!policy.synced) {
            const proceed = window.confirm(
                "尚未同步本地工资政策。目标租户导入后 cyxx 中奖金结余、舍入方式等政策将为空。\n\n仍要签发吗？");
            if (!proceed) {
                return;
            }
        }
        const response = await fetch("/api/license/issue", {
            method: "POST",
            credentials: "same-origin",
            headers: { "Content-Type": "application/json", Accept: "application/json" },
            body: JSON.stringify(body),
        });
        if (!response.ok) {
            const payload = await response.json().catch(() => null);
            throw new Error(problemMessage(payload, "签发失败"));
        }
        const blob = await response.blob();
        const a = document.createElement("a");
        a.href = URL.createObjectURL(blob);
        a.download = "单位授权-" + body.organizationCode + ".rsauth.json";
        a.click();
        URL.revokeObjectURL(a.href);
        setStatus("success", "授权包已签发并下载。");
        refreshIssueLogs();
    }

    async function refreshIssueLogs() {
        const rows = await api("/api/license/issue-logs?limit=50");
        document.getElementById("issue-log-rows").innerHTML = rows.map((r) => `
            <tr>
                <td>${esc(r.createdAt || "")}</td>
                <td>${esc(r.actorUsername)}</td>
                <td>${esc(r.subjectCode)} ${esc(r.subjectName)}</td>
                <td>${esc(r.organizationCount)}</td>
                <td>${esc(r.fingerprint)}</td>
                <td>${esc(r.summary)}</td>
            </tr>
        `).join("") || `<tr><td colspan="6">暂无签发记录</td></tr>`;
    }

    function esc(value) {
        return String(value == null ? "" : value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;");
    }

    function shortHex(value) {
        const s = String(value || "");
        return s.length <= 16 ? s : s.slice(0, 8) + "…" + s.slice(-6);
    }

    function stopMonitorTimer() {
        if (monitorTimer) {
            clearInterval(monitorTimer);
            monitorTimer = null;
        }
    }

    function pct(used, total) {
        if (!total) return 0;
        return (used * 100) / total;
    }

    function fmtPct(value) {
        return (Number(value) || 0).toFixed(1) + "%";
    }

    function fmtBytes(bytes) {
        const n = Number(bytes) || 0;
        if (n >= 1024 ** 3) return (n / 1024 ** 3).toFixed(1) + " GB";
        if (n >= 1024 ** 2) return (n / 1024 ** 2).toFixed(0) + " MB";
        return n + " B";
    }

    function fmtUptime(ms) {
        const sec = Math.floor((Number(ms) || 0) / 1000);
        const d = Math.floor(sec / 86400);
        const h = Math.floor((sec % 86400) / 3600);
        const m = Math.floor((sec % 3600) / 60);
        if (d > 0) return d + " 天 " + h + " 小时";
        if (h > 0) return h + " 小时 " + m + " 分";
        return m + " 分钟";
    }

    function metricLevel(value, warn, crit) {
        if (value >= crit) return "crit";
        if (value >= warn) return "warn";
        return "ok";
    }

    function statusLabel(status) {
        if (status === "CRIT") return "严重";
        if (status === "WARN") return "警告";
        return "正常";
    }

    function statusClass(status) {
        if (status === "CRIT") return "crit";
        if (status === "WARN") return "warn";
        return "ok";
    }

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;");
    }

    function worstInode(disks) {
        let worst = 0;
        (disks || []).forEach((d) => {
            worst = Math.max(worst, pct(d.usedInodes, d.totalInodes));
        });
        return worst;
    }

    function renderMetricCards(latest, thresholds) {
        const cpu = latest.cpuPercent || 0;
        const mem = pct(latest.memoryUsedBytes, latest.memoryTotalBytes);
        const disk = pct(latest.diskUsedBytes, latest.diskTotalBytes);
        const swap = pct(latest.swapUsedBytes, latest.swapTotalBytes);
        const inode = worstInode(latest.disks);
        const t = thresholds || {};
        const swapCls = latest.swapTotalBytes
            ? metricLevel(swap, t.swapWarn ?? 50, t.swapCrit ?? 80)
            : "ok";
        const cards = [
            {
                label: "综合状态",
                value: statusLabel(latest.overall),
                sub: (latest.hostname || "") + (latest.os ? " · " + latest.os : ""),
                cls: statusClass(latest.overall),
            },
            {
                label: "CPU",
                value: fmtPct(cpu),
                sub: (latest.processors || 0) + " 核" + (latest.loadAverage >= 0 ? " · 负载 " + Number(latest.loadAverage).toFixed(2) : ""),
                cls: metricLevel(cpu, t.cpuWarn ?? 80, t.cpuCrit ?? 95),
            },
            {
                label: "内存",
                value: fmtPct(mem),
                sub: fmtBytes(latest.memoryUsedBytes) + " / " + fmtBytes(latest.memoryTotalBytes),
                cls: metricLevel(mem, t.memoryWarn ?? 85, t.memoryCrit ?? 95),
            },
            {
                label: "Swap",
                value: latest.swapTotalBytes ? fmtPct(swap) : "未配置",
                sub: latest.swapTotalBytes
                    ? (fmtBytes(latest.swapUsedBytes) + " / " + fmtBytes(latest.swapTotalBytes))
                    : "无交换分区",
                cls: swapCls,
            },
            {
                label: "磁盘",
                value: fmtPct(disk),
                sub: (latest.diskName || "主磁盘") + " · " + fmtBytes(latest.diskUsedBytes) + " / " + fmtBytes(latest.diskTotalBytes),
                cls: metricLevel(disk, t.diskWarn ?? 85, t.diskCrit ?? 95),
            },
            {
                label: "inode",
                value: inode ? fmtPct(inode) : "—",
                sub: "各盘最高占用",
                cls: inode ? metricLevel(inode, t.inodeWarn ?? 85, t.inodeCrit ?? 95) : "ok",
            },
        ];
        document.getElementById("monitor-cards").innerHTML = cards.map((c) => `
            <div class="metric-card ${c.cls}">
                <div class="label">${escapeHtml(c.label)}</div>
                <div class="value">${escapeHtml(c.value)}</div>
                <div class="sub">${escapeHtml(c.sub)}</div>
            </div>
        `).join("");
        const collected = latest.collectedAt ? String(latest.collectedAt).replace("T", " ").slice(0, 19) : "";
        document.getElementById("monitor-meta").textContent =
            (collected ? ("采集时间 " + collected + " · ") : "")
            + "运维台已运行 " + fmtUptime(latest.jvmUptimeMs);
    }

    function renderChart(history) {
        const svg = document.getElementById("monitor-chart");
        const points = (history || []).slice().reverse();
        if (points.length < 2) {
            svg.innerHTML = `<text x="16" y="76" fill="#94a3b8" font-size="13">采集两次后显示趋势</text>`;
            return;
        }
        const w = 720;
        const h = 140;
        const pad = 12;
        const series = [
            { cls: "cpu", color: "#60a5fa", values: points.map((p) => p.cpuPercent || 0) },
            { cls: "mem", color: "#34d399", values: points.map((p) => pct(p.memoryUsedBytes, p.memoryTotalBytes)) },
            { cls: "disk", color: "#fbbf24", values: points.map((p) => pct(p.diskUsedBytes, p.diskTotalBytes)) },
            { cls: "swap", color: "#c084fc", values: points.map((p) => pct(p.swapUsedBytes, p.swapTotalBytes)) },
        ];
        const toPath = (values) => values.map((v, i) => {
            const x = pad + (i * (w - pad * 2)) / (values.length - 1);
            const y = h - pad - (Math.min(100, Math.max(0, v)) * (h - pad * 2)) / 100;
            return (i === 0 ? "M" : "L") + x.toFixed(1) + " " + y.toFixed(1);
        }).join(" ");
        svg.innerHTML = series.map((s) =>
            `<path d="${toPath(s.values)}" fill="none" stroke="${s.color}" stroke-width="2" />`
        ).join("");
    }

    function probeOf(target, probes) {
        return (probes || []).find((p) => p.targetId === target.id) || null;
    }

    function renderTargets(targets, probes) {
        const rows = document.getElementById("monitor-target-rows");
        if (!targets || !targets.length) {
            rows.innerHTML = `<tr><td colspan="6" class="muted">尚未配置探测目标</td></tr>`;
            return;
        }
        rows.innerHTML = targets.map((t) => {
            const probe = probeOf(t, probes);
            const status = probe ? probe.status : (t.enabled ? "WAIT" : "OFF");
            const latency = probe ? (probe.latencyMs + " ms") : "";
            const message = probe ? (probe.message || "") : (t.enabled ? "" : "未启用");
            const label = status === "WAIT" ? "待测" : (status === "OFF" ? "停用" : statusLabel(status));
            return `<tr>
                <td>${escapeHtml(t.name)}</td>
                <td>${escapeHtml(t.url)}</td>
                <td><span class="badge ${statusClass(status)}">${escapeHtml(label)}</span></td>
                <td>${escapeHtml(latency)}</td>
                <td>${escapeHtml(message)}</td>
                <td><button type="button" class="secondary monitor-del" data-id="${t.id}">删除</button></td>
            </tr>`;
        }).join("");
        rows.querySelectorAll(".monitor-del").forEach((btn) => {
            btn.addEventListener("click", () => deleteMonitorTarget(Number(btn.dataset.id)).catch(showErr));
        });
    }

    function renderDisks(disks) {
        const rows = document.getElementById("monitor-disk-rows");
        if (!disks || !disks.length) {
            rows.innerHTML = `<tr><td colspan="4" class="muted">未采集到磁盘</td></tr>`;
            return;
        }
        rows.innerHTML = disks.map((d) => `<tr>
            <td>${escapeHtml(d.name)}</td>
            <td>${escapeHtml(d.type || "")}</td>
            <td>${fmtPct(pct(d.usedBytes, d.totalBytes))} · ${fmtBytes(d.usedBytes)} / ${fmtBytes(d.totalBytes)}</td>
            <td>${d.totalInodes ? (fmtPct(pct(d.usedInodes, d.totalInodes)) + " · " + d.usedInodes + " / " + d.totalInodes) : "—"}</td>
        </tr>`).join("");
    }

    function renderServices(services) {
        const rows = document.getElementById("monitor-service-rows");
        if (!services || !services.length) {
            rows.innerHTML = `<tr><td colspan="4" class="muted">当前环境无 systemd 或未配置单元</td></tr>`;
            return;
        }
        rows.innerHTML = services.map((s) => `<tr>
            <td>${escapeHtml(s.name)}</td>
            <td><span class="badge ${statusClass(s.status)}">${escapeHtml(s.active || statusLabel(s.status))}</span></td>
            <td>${escapeHtml(String(s.restarts ?? 0))}</td>
            <td>${escapeHtml(s.message || s.sub || "")}</td>
        </tr>`).join("");
    }

    function renderCerts(certs) {
        const rows = document.getElementById("monitor-cert-rows");
        if (!certs || !certs.length) {
            rows.innerHTML = `<tr><td colspan="4" class="muted">未配置证书主机</td></tr>`;
            return;
        }
        rows.innerHTML = certs.map((c) => `<tr>
            <td>${escapeHtml(c.host)}</td>
            <td><span class="badge ${statusClass(c.status)}">${escapeHtml(String(c.daysLeft ?? 0))}</span></td>
            <td>${escapeHtml(c.notAfter || "")}</td>
            <td>${escapeHtml(c.message || "")}</td>
        </tr>`).join("");
    }

    function renderRuntimes(runtimes) {
        const rows = document.getElementById("monitor-runtime-rows");
        if (!runtimes || !runtimes.length) {
            rows.innerHTML = `<tr><td colspan="8" class="muted">尚未抓到实例运行时（需人事实例提供 /internal/runtime）</td></tr>`;
            return;
        }
        rows.innerHTML = runtimes.map((r) => `<tr>
            <td><span class="badge ${statusClass(r.status)}">${escapeHtml(r.name || "")}</span></td>
            <td>${r.heapMaxBytes ? (fmtPct(pct(r.heapUsedBytes, r.heapMaxBytes)) + " · " + fmtBytes(r.heapUsedBytes) + " / " + fmtBytes(r.heapMaxBytes)) : "—"}</td>
            <td>${escapeHtml(String(r.gcCount ?? 0))} 次 / ${escapeHtml(String(r.gcTimeMs ?? 0))} ms</td>
            <td>${escapeHtml(String(r.threads ?? 0))}</td>
            <td>${escapeHtml((r.hikariActive ?? 0) + " 忙 / " + (r.hikariIdle ?? 0) + " 闲 / 等待 " + (r.hikariPending ?? 0) + " / 上限 " + (r.hikariMax ?? 0))}</td>
            <td>${escapeHtml((r.tomcatBusy ?? 0) + " busy / " + (r.tomcatCurrent ?? 0) + " / 上限 " + (r.tomcatMax ?? 0))}</td>
            <td>${escapeHtml(r.dbStatus || "—")}</td>
            <td>${escapeHtml(r.message || "")}</td>
        </tr>`).join("");
    }

    function renderAlerts(alerts) {
        const rows = document.getElementById("monitor-alert-rows");
        if (!alerts || !alerts.length) {
            rows.innerHTML = `<tr><td colspan="4" class="muted">暂无告警</td></tr>`;
            return;
        }
        rows.innerHTML = alerts.map((a) => `<tr>
            <td>${escapeHtml(String(a.createdAt || "").replace("T", " ").slice(0, 19))}</td>
            <td><span class="badge ${statusClass(a.level)}">${escapeHtml(statusLabel(a.level))}</span></td>
            <td>${escapeHtml(a.title)}</td>
            <td>${escapeHtml(a.message)}</td>
        </tr>`).join("");
    }

    async function refreshMonitor() {
        const data = await api("/api/monitor/overview");
        const latest = data.latest || {};
        renderMetricCards(latest, data.thresholds);
        renderChart([latest].concat(data.history || []));
        renderDisks(latest.disks || []);
        renderServices(latest.services || []);
        renderCerts(latest.certificates || []);
        renderRuntimes(latest.runtimes || []);
        renderTargets(data.targets || [], latest.probes || []);
        renderAlerts(data.alerts || []);
        setStatus(statusClass(latest.overall) === "ok" ? "success" : (latest.overall === "CRIT" ? "error" : "hint"),
            "监控 " + statusLabel(latest.overall || "OK") + "。");
    }

    async function collectNow() {
        await api("/api/monitor/collect", { method: "POST" });
        await refreshMonitor();
    }

    async function addMonitorTarget() {
        const name = document.getElementById("monitor-target-name").value.trim();
        const url = document.getElementById("monitor-target-url").value.trim();
        const timeoutMs = Number(document.getElementById("monitor-target-timeout").value) || 5000;
        if (!name || !url) {
            setStatus("error", "请填写探测名称和 URL");
            return;
        }
        await api("/api/monitor/targets", {
            method: "POST",
            body: JSON.stringify({ name, url, timeoutMs, enabled: true }),
        });
        document.getElementById("monitor-target-name").value = "";
        document.getElementById("monitor-target-url").value = "";
        await refreshMonitor();
        setStatus("success", "已添加探测目标。");
    }

    async function deleteMonitorTarget(id) {
        await api("/api/monitor/targets/" + id, { method: "DELETE" });
        await refreshMonitor();
        setStatus("success", "已删除探测目标。");
    }

    document.getElementById("prov-run-enc").addEventListener("click", () => runEncProvision().catch(showErr));
    document.getElementById("prov-run-demo").addEventListener("click", runDemoProvision);
    document.getElementById("prov-run-custom").addEventListener("click", runCustomProvision);
    document.getElementById("prov-run").addEventListener("click", () => {
        const form = readProvisionForm();
        if (!form.sm2UserId) {
            setStatus("error", "请填写 SM2 用户身份");
            return;
        }
        provisionCurrentLock("generate");
    });
    document.getElementById("device-refresh").addEventListener("click", () => refreshDevices().catch(showErr));
    document.getElementById("device-export").addEventListener("click", () => exportBindings().catch(showErr));
    document.getElementById("org-save").addEventListener("click", () => saveOrg().catch(showErr));
    document.getElementById("org-import-csv").addEventListener("click", () => importCsv().catch(showErr));
    document.getElementById("org-import-json").addEventListener("click", () => importOrgsJson().catch(showErr));
    document.getElementById("org-refresh").addEventListener("click", () => refreshOrgs().catch(showErr));
    document.getElementById("org-issue-selected").addEventListener("click", () => issueSelectedOrgs().catch(showErr));
    document.getElementById("org-check-all")?.addEventListener("change", (e) => {
        document.querySelectorAll(".org-check").forEach((c) => {
            c.checked = e.target.checked;
        });
    });
    document.getElementById("issue-run").addEventListener("click", () => issueLicense().catch(showErr));
    document.getElementById("issue-refresh-logs").addEventListener("click", () => refreshIssueLogs().catch(showErr));
    document.getElementById("issue-subject-pick").addEventListener("change", applyIssueSubjectPick);
    document.getElementById("monitor-refresh").addEventListener("click", () => collectNow().catch(showErr));
    document.getElementById("monitor-target-add").addEventListener("click", () => addMonitorTarget().catch(showErr));
    document.getElementById("pwd-save").addEventListener("click", () => changePassword().catch(showErr));

    async function changePassword() {
        const currentPassword = document.getElementById("pwd-current").value;
        const newPassword = document.getElementById("pwd-new").value;
        const confirmPassword = document.getElementById("pwd-confirm").value;
        if (!currentPassword || !newPassword || !confirmPassword) {
            setStatus("error", "请填写当前密码和新密码");
            return;
        }
        const result = await api("/api/ops/password", {
            method: "POST",
            body: JSON.stringify({ currentPassword, newPassword, confirmPassword }),
        });
        document.getElementById("password-form").reset();
        setStatus("success", result.message || "密码已更新。");
    }

    function showErr(e) {
        setStatus("error", e.message || String(e));
    }

    async function boot() {
        probeSoftKey();
        try {
            const me = await api("/api/ukey/me");
            document.getElementById("whoami").textContent = me.username ? ("当前用户：" + me.username) : "";
            setStatus("hint", "就绪。可先看「监控」，或到「制锁」写入密钥。");
            refreshMonitor().catch(showErr);
            monitorTimer = setInterval(() => refreshMonitor().catch(showErr), 15000);
        } catch (e) {
            // redirect handled
        }
    }

    boot();
})();
