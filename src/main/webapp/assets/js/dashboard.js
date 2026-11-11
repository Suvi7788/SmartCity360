(() => {
    const menuToggle = document.getElementById("menuToggle");
    const sidebar = document.getElementById("sidebar");
    if (menuToggle && sidebar) {
        menuToggle.setAttribute("aria-expanded", "false");
        menuToggle.addEventListener("click", () => {
            const open = sidebar.classList.toggle("sidebar-open");
            menuToggle.setAttribute("aria-expanded", String(open));
        });
        document.addEventListener("keydown", (event) => {
            if (event.key === "Escape" && sidebar.classList.contains("sidebar-open")) {
                sidebar.classList.remove("sidebar-open");
                menuToggle.setAttribute("aria-expanded", "false");
                menuToggle.focus();
            }
        });
    }

    document.querySelectorAll("[data-alert-dismiss]").forEach((button) => {
        button.addEventListener("click", () => button.closest(".alert")?.remove());
    });

    document.querySelectorAll("[data-data-table]").forEach((container) => {
        const table = container.querySelector("table");
        const body = table?.tBodies[0];
        if (!table || !body) return;

        const rows = [...body.querySelectorAll("tr[data-data-row]")];
        const emptyState = body.querySelector("[data-empty-state]");
        const pageSize = Math.max(1, Number(container.dataset.pageSize) || 10);
        const search = document.querySelector(`[data-table-search="${container.id}"]`);
        const count = document.querySelector(`[data-table-count="${container.id}"]`);
        const filters = [...document.querySelectorAll(`[data-table-target="${container.id}"][data-table-filter]`)];
        const pagination = document.createElement("nav");
        pagination.className = "table-pagination";
        pagination.setAttribute("aria-label", "Table pages");
        pagination.hidden = true;
        container.insertAdjacentElement("afterend", pagination);

        const emptyFiltered = document.createElement("tr");
        emptyFiltered.className = "filtered-empty";
        const emptyCell = document.createElement("td");
        emptyCell.colSpan = table.tHead?.rows[0]?.cells.length || 1;
        emptyCell.className = "empty-state";
        emptyCell.textContent = "No records match your search or filters. Adjust them and try again.";
        emptyFiltered.append(emptyCell);
        body.append(emptyFiltered);

        const controller = {
            page: 0,
            render() {
                const term = (search?.value || "").trim().toLocaleLowerCase();
                const activeFilters = filters
                    .map((select) => [select.dataset.tableFilter, select.value])
                    .filter(([, value]) => value !== "");
                const matching = rows.filter((row) => {
                    if (term && !row.textContent.toLocaleLowerCase().includes(term)) return false;
                    return activeFilters.every(([key, value]) => row.dataset[key] === value);
                });
                const pageCount = Math.max(1, Math.ceil(matching.length / pageSize));
                controller.page = Math.min(controller.page, pageCount - 1);
                const visibleStart = controller.page * pageSize;
                const visibleRows = new Set(matching.slice(visibleStart, visibleStart + pageSize));

                rows.forEach((row) => {
                    row.hidden = !visibleRows.has(row);
                });
                const hasServerEmptyState = rows.length === 0;
                if (emptyState) emptyState.hidden = !hasServerEmptyState;
                emptyFiltered.hidden = hasServerEmptyState || matching.length > 0;

                if (count) {
                    count.textContent = rows.length === 0
                        ? "0 records"
                        : `${matching.length} of ${rows.length} records`;
                }

                pagination.replaceChildren();
                pagination.hidden = matching.length <= pageSize;
                if (!pagination.hidden) {
                    const summary = document.createElement("span");
                    summary.textContent = `Page ${controller.page + 1} of ${pageCount}`;
                    const previous = document.createElement("button");
                    previous.type = "button";
                    previous.className = "button button-small button-quiet";
                    previous.textContent = "Previous";
                    previous.disabled = controller.page === 0;
                    previous.addEventListener("click", () => {
                        controller.page--;
                        controller.render();
                    });
                    const next = document.createElement("button");
                    next.type = "button";
                    next.className = "button button-small button-quiet";
                    next.textContent = "Next";
                    next.disabled = controller.page >= pageCount - 1;
                    next.addEventListener("click", () => {
                        controller.page++;
                        controller.render();
                    });
                    pagination.append(previous, summary, next);
                }
            }
        };
        search?.addEventListener("input", () => {
            controller.page = 0;
            controller.render();
        });
        filters.forEach((select) => select.addEventListener("change", () => {
            controller.page = 0;
            controller.render();
        }));
        controller.render();
    });

    document.querySelectorAll("[data-zone-search]").forEach((search) => {
        const rows = [...document.querySelectorAll("[data-zone-row]")];
        const rowById = new Map(rows.map((row) => [row.dataset.zoneRow, row]));
        const collapsed = new Set();
        const termMatches = new Set();

        function updateVisibility() {
            const term = search.value.trim().toLocaleLowerCase();
            termMatches.clear();
            if (term) {
                rows.forEach((row) => {
                    if (row.textContent.toLocaleLowerCase().includes(term)) {
                        termMatches.add(row.dataset.zoneRow);
                        let parentId = row.dataset.parent;
                        while (parentId) {
                            termMatches.add(parentId);
                            parentId = rowById.get(parentId)?.dataset.parent;
                        }
                    }
                });
            }
            rows.forEach((row) => {
                let parentId = row.dataset.parent;
                let hidden = Boolean(term) && !termMatches.has(row.dataset.zoneRow);
                while (!hidden && parentId) {
                    if (!term && collapsed.has(parentId)) {
                        hidden = true;
                        break;
                    }
                    parentId = rowById.get(parentId)?.dataset.parent;
                }
                row.hidden = hidden;
            });
        }

        search.addEventListener("input", updateVisibility);
        document.querySelectorAll("[data-zone-toggle]").forEach((button) => {
            button.addEventListener("click", () => {
                const row = button.closest("[data-zone-row]");
                const id = row.dataset.zoneRow;
                const expanded = button.getAttribute("aria-expanded") === "true";
                if (expanded) collapsed.add(id);
                else collapsed.delete(id);
                button.setAttribute("aria-expanded", String(!expanded));
                button.textContent = expanded ? "+" : "−";
                updateVisibility();
            });
        });
    });

    document.querySelectorAll("form").forEach((form) => {
        form.addEventListener("submit", (event) => {
            if (form.dataset.submitting === "true") {
                event.preventDefault();
                return;
            }

            const submitter = event.submitter || document.activeElement;
            let confirmation = submitter?.dataset.confirm || form.dataset.confirm || "";
            const operation = form.querySelector("[data-zone-operation]");
            if (operation?.value === "SHUTDOWN_POWER") {
                const zoneName = form.closest("[data-zone-row]")?.querySelector("strong")?.textContent?.trim();
                confirmation = `Shut down power for ${zoneName || "this zone"} and all descendants?`;
            }
            const count = Number(form.querySelector("[name='count']")?.value);
            if (!confirmation && form.hasAttribute("data-bulk-form") && count > 1000) {
                confirmation = `Create ${count.toLocaleString()} simulated streetlights?`;
            }
            if (confirmation && !window.confirm(confirmation)) {
                event.preventDefault();
                return;
            }

            if (submitter?.name && submitter.value) {
                const actionCopy = document.createElement("input");
                actionCopy.type = "hidden";
                actionCopy.name = submitter.name;
                actionCopy.value = submitter.value;
                form.append(actionCopy);
            }

            form.dataset.submitting = "true";
            form.setAttribute("aria-busy", "true");
            form.querySelectorAll("button[type='submit'], input[type='submit']").forEach((button) => {
                button.disabled = true;
                if (button === submitter || form.querySelectorAll("button[type='submit'], input[type='submit']").length === 1) {
                    if (button.tagName === "BUTTON") {
                        button.dataset.originalContent = button.innerHTML;
                        button.textContent = form.dataset.loadingLabel || "Working…";
                        button.insertAdjacentHTML("beforeend", ' <span class="loading-spinner" aria-hidden="true"></span>');
                    } else {
                        button.dataset.originalValue = button.value;
                        button.value = form.dataset.loadingLabel || "Working…";
                    }
                }
            });
        });
    });
})();
