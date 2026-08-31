/**
 * Lightweight year-month picker: year dropdown + 12-month grid.
 * Input value stays YYYY-MM; callers may convert to YYYY.MM for APIs.
 */
(function (global) {
    "use strict";

    const YEAR_MIN = 1940;
    const POPUP_ID = "month-picker-popup";

    let activeInput = null;
    let popupEl = null;
    let yearSelect = null;
    let monthGrid = null;
    let docBound = false;

    function yearMax() {
        return new Date().getFullYear() + 5;
    }

    function pad2(n) {
        return String(n).padStart(2, "0");
    }

    function normalizeToYm(raw) {
        let s = String(raw || "").trim();
        if (!s) {
            return "";
        }
        s = s.replace(/[./]/g, "-");
        if (/^\d{6}$/.test(s)) {
            return s.slice(0, 4) + "-" + s.slice(4, 6);
        }
        const m = s.match(/^(\d{4})-(\d{1,2})$/);
        if (!m) {
            return "";
        }
        const month = Number(m[2]);
        if (month < 1 || month > 12) {
            return "";
        }
        return m[1] + "-" + pad2(month);
    }

    function parseYm(raw) {
        const ym = normalizeToYm(raw);
        if (!ym) {
            return null;
        }
        return {
            year: Number(ym.slice(0, 4)),
            month: Number(ym.slice(5, 7)),
        };
    }

    function ensurePopup() {
        if (popupEl) {
            return popupEl;
        }
        popupEl = document.createElement("div");
        popupEl.id = POPUP_ID;
        popupEl.className = "month-picker-popup hidden";
        popupEl.setAttribute("role", "dialog");
        popupEl.setAttribute("aria-label", "选择年月");
        popupEl.innerHTML = `
            <div class="month-picker-header">
                <button type="button" class="month-picker-nav" data-year-step="-10" title="上十年">«</button>
                <button type="button" class="month-picker-nav" data-year-step="-1" title="上一年">‹</button>
                <select class="month-picker-year" aria-label="年份"></select>
                <button type="button" class="month-picker-nav" data-year-step="1" title="下一年">›</button>
                <button type="button" class="month-picker-nav" data-year-step="10" title="下十年">»</button>
            </div>
            <div class="month-picker-months" role="listbox" aria-label="月份"></div>
            <div class="month-picker-footer">
                <button type="button" class="month-picker-clear" data-action="clear">清空</button>
                <button type="button" class="month-picker-today" data-action="today">本月</button>
            </div>
        `;
        document.body.appendChild(popupEl);

        yearSelect = popupEl.querySelector(".month-picker-year");
        monthGrid = popupEl.querySelector(".month-picker-months");

        const max = yearMax();
        for (let y = max; y >= YEAR_MIN; y -= 1) {
            const opt = document.createElement("option");
            opt.value = String(y);
            opt.textContent = String(y) + " 年";
            yearSelect.appendChild(opt);
        }

        for (let m = 1; m <= 12; m += 1) {
            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = "month-picker-month";
            btn.dataset.month = String(m);
            btn.textContent = m + "月";
            btn.setAttribute("role", "option");
            monthGrid.appendChild(btn);
        }

        popupEl.addEventListener("click", (event) => {
            event.stopPropagation();
            const nav = event.target.closest("[data-year-step]");
            if (nav) {
                const step = Number(nav.dataset.yearStep) || 0;
                const next = Number(yearSelect.value) + step;
                const clamped = Math.min(yearMax(), Math.max(YEAR_MIN, next));
                yearSelect.value = String(clamped);
                refreshMonthHighlight();
                return;
            }
            const monthBtn = event.target.closest(".month-picker-month");
            if (monthBtn && activeInput) {
                commitValue(Number(yearSelect.value), Number(monthBtn.dataset.month));
                return;
            }
            const action = event.target.closest("[data-action]");
            if (!action || !activeInput) {
                return;
            }
            if (action.dataset.action === "clear") {
                commitValue(null, null);
                return;
            }
            if (action.dataset.action === "today") {
                const now = new Date();
                commitValue(now.getFullYear(), now.getMonth() + 1);
            }
        });

        yearSelect.addEventListener("change", refreshMonthHighlight);
        return popupEl;
    }

    function refreshMonthHighlight() {
        if (!monthGrid || !activeInput) {
            return;
        }
        const parsed = parseYm(activeInput.value);
        const year = Number(yearSelect.value);
        monthGrid.querySelectorAll(".month-picker-month").forEach((btn) => {
            const m = Number(btn.dataset.month);
            const selected = parsed && parsed.year === year && parsed.month === m;
            btn.classList.toggle("is-selected", selected);
        });
    }

    function commitValue(year, month) {
        if (!activeInput) {
            return;
        }
        const input = activeInput;
        if (year == null || month == null) {
            input.value = "";
        } else {
            input.value = year + "-" + pad2(month);
        }
        input.dispatchEvent(new Event("input", { bubbles: true }));
        input.dispatchEvent(new Event("change", { bubbles: true }));
        close();
        input.focus();
    }

    function positionPopup(input) {
        const rect = input.getBoundingClientRect();
        const popup = ensurePopup();
        popup.classList.remove("hidden");
        const width = Math.max(280, Math.min(320, rect.width + 40));
        popup.style.width = width + "px";
        const popupHeight = popup.offsetHeight || 260;
        let top = rect.bottom + 6;
        let left = rect.left;
        if (top + popupHeight > window.innerHeight - 8) {
            top = Math.max(8, rect.top - popupHeight - 6);
        }
        if (left + width > window.innerWidth - 8) {
            left = Math.max(8, window.innerWidth - width - 8);
        }
        popup.style.top = Math.round(top) + "px";
        popup.style.left = Math.round(left) + "px";
    }

    function open(input) {
        if (!input || input.disabled || input.readOnly) {
            return;
        }
        ensurePopup();
        activeInput = input;
        const parsed = parseYm(input.value);
        const now = new Date();
        const year = parsed ? parsed.year : now.getFullYear();
        const clamped = Math.min(yearMax(), Math.max(YEAR_MIN, year));
        yearSelect.value = String(clamped);
        refreshMonthHighlight();
        positionPopup(input);
        bindDocListeners();
    }

    function close() {
        if (popupEl) {
            popupEl.classList.add("hidden");
        }
        activeInput = null;
    }

    function onDocPointer(event) {
        if (!popupEl || popupEl.classList.contains("hidden")) {
            return;
        }
        if (popupEl.contains(event.target)) {
            return;
        }
        if (activeInput && (activeInput === event.target || activeInput.contains?.(event.target))) {
            return;
        }
        if (event.target.closest?.(".month-picker-toggle")) {
            return;
        }
        // blur-normalize active input then close
        if (activeInput) {
            const normalized = normalizeToYm(activeInput.value);
            if (activeInput.value && normalized && activeInput.value !== normalized) {
                activeInput.value = normalized;
            } else if (activeInput.value && !normalized) {
                // keep typed invalid until user fixes; still close
            }
        }
        close();
    }

    function onDocKey(event) {
        if (event.key === "Escape") {
            close();
        }
    }

    function bindDocListeners() {
        if (docBound) {
            return;
        }
        docBound = true;
        document.addEventListener("mousedown", onDocPointer, true);
        document.addEventListener("keydown", onDocKey, true);
        window.addEventListener("resize", close);
        window.addEventListener("scroll", close, true);
    }

    function wrapInput(input) {
        if (input.closest(".month-picker-combo")) {
            return input.closest(".month-picker-combo");
        }
        const parentCombo = input.closest(".dict-input-combo");
        if (parentCombo) {
            if (!parentCombo.querySelector(".month-picker-toggle")) {
                const button = document.createElement("button");
                button.type = "button";
                button.className = "dict-picker-button month-picker-toggle";
                button.setAttribute("aria-label", "选择年月");
                button.title = "选择年月";
                button.textContent = "年月";
                button.addEventListener("click", (event) => {
                    event.preventDefault();
                    event.stopPropagation();
                    if (activeInput === input && popupEl && !popupEl.classList.contains("hidden")) {
                        close();
                    } else {
                        open(input);
                    }
                });
                parentCombo.appendChild(button);
            }
            return parentCombo;
        }

        const label = input.parentElement;
        const combo = document.createElement("div");
        combo.className = "month-picker-combo";
        if (label) {
            label.insertBefore(combo, input);
        } else {
            input.parentNode.insertBefore(combo, input);
        }
        combo.appendChild(input);
        const button = document.createElement("button");
        button.type = "button";
        button.className = "month-picker-toggle";
        button.setAttribute("aria-label", "选择年月");
        button.title = "选择年月";
        button.textContent = "年月";
        button.addEventListener("click", (event) => {
            event.preventDefault();
            event.stopPropagation();
            if (input.disabled || input.readOnly) {
                return;
            }
            if (activeInput === input && popupEl && !popupEl.classList.contains("hidden")) {
                close();
            } else {
                open(input);
            }
        });
        combo.appendChild(button);
        return combo;
    }

    function enhance(input) {
        if (!input || input.dataset.monthPickerEnhanced === "1") {
            return input;
        }
        if (input.tagName !== "INPUT") {
            return input;
        }
        if (input.type === "month" || input.type === "date") {
            input.type = "text";
        }
        input.dataset.monthPickerEnhanced = "1";
        input.dataset.monthField = "true";
        if (!input.placeholder) {
            input.placeholder = "例如 1980-01";
        }
        input.autocomplete = "off";
        input.spellcheck = false;

        const current = normalizeToYm(input.value);
        if (input.value && current) {
            input.value = current;
        }

        wrapInput(input);

        input.addEventListener("blur", () => {
            const normalized = normalizeToYm(input.value);
            if (input.value && normalized && input.value !== normalized) {
                input.value = normalized;
                input.dispatchEvent(new Event("change", { bubbles: true }));
            }
        });

        input.addEventListener("keydown", (event) => {
            if (event.key === "ArrowDown" && (event.altKey || event.metaKey)) {
                event.preventDefault();
                open(input);
            }
        });

        return input;
    }

    function enhanceAll(rootOrSelector) {
        let root = document;
        let selector = 'input[type="month"], input[data-month-field="true"], input[data-month-picker]';
        if (typeof rootOrSelector === "string") {
            selector = rootOrSelector;
        } else if (rootOrSelector && rootOrSelector.nodeType === 1) {
            root = rootOrSelector;
        }
        root.querySelectorAll(selector).forEach((el) => enhance(el));
    }

    function syncToggleState(input) {
        const combo = input.closest(".month-picker-combo, .dict-input-combo");
        if (!combo) {
            return;
        }
        combo.querySelectorAll(".month-picker-toggle").forEach((btn) => {
            btn.disabled = !!(input.disabled || input.readOnly);
        });
    }

    global.MonthPicker = {
        enhance,
        enhanceAll,
        normalizeToYm,
        open,
        close,
        syncToggleState,
    };
})(window);
