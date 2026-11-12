<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Streetlight management"/>
<c:set var="activeNav" value="streetlights"/>
<%@ include file="fragments/header.jspf" %>
<section class="page-heading"><div><h1>Streetlight management</h1><p class="subheading">Generate large fleets while reusing immutable configurations across unique light instances.</p></div></section>
<section class="kpi-grid three-kpis">
    <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon teal">☼</span><span class="kpi-label">TOTAL INSTANCES</span></div><strong><c:out value="${totalLights}"/></strong><small>Persisted streetlights</small></article>
    <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon blue">◈</span><span class="kpi-label">SHARED CONFIGS</span></div><strong><c:out value="${uniqueConfigurations}"/></strong><small>Unique persisted configurations</small></article>
    <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon green">↗</span><span class="kpi-label">IN-MEMORY POOL</span></div><strong><c:out value="${cachedConfigurations}"/></strong><small>Configs interned by this app instance</small></article>
</section>
<section class="content-grid">
    <article class="panel">
        <div class="panel-heading"><div><p class="eyebrow">BULK SIMULATION</p><h2>Create a fleet</h2></div></div>
        <form method="post" action="${pageContext.request.contextPath}/streetlights" class="form-stack" data-bulk-form data-loading-label="Creating streetlights…">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
            <label>Number of lights<input type="number" name="count" min="1" max="10000" value="1000" required></label>
            <label>Street / area<input name="location" maxlength="150" value="Central Boulevard" required></label>
            <label>Zone ID <span class="optional">(optional)</span><input type="number" name="zoneId" min="1" placeholder="Leave empty to skip zone"></label>
            <div class="form-grid">
                <label>Wattage<input type="number" name="wattage" min="1" max="1000" value="80" required></label>
                <label>Color temperature (K)<input type="number" name="colorTemperature" min="1000" max="10000" value="4000" required></label>
            </div>
            <label>Brightness mode<select name="brightnessMode"><option>Adaptive</option><option>Constant</option><option>Motion-triggered</option></select></label>
            <label>Sensor profile<select name="sensorProfile"><option>Standard</option><option>Traffic-aware</option><option>Pedestrian-aware</option></select></label>
            <button class="button button-primary" type="submit">Create light instances <span>→</span></button>
        </form>
    </article>
</section>
<section class="panel table-panel">
    <div class="panel-heading"><div><p class="eyebrow">RECENT INSTANCES</p><h2>Streetlight register</h2></div><span class="count-label">Latest 50</span></div>
    <div class="table-toolbar">
        <label class="table-search">Search streetlights<input type="search" placeholder="ID, location, configuration…" data-table-search="streetlightsTable"></label>
        <label>Status<select data-table-filter="status" data-table-target="streetlightsTable"><option value="">All statuses</option><option>ON</option><option>OFF</option><option>MAINTENANCE</option></select></label>
        <span class="table-match-count" data-table-count="streetlightsTable" aria-live="polite"></span>
    </div>
    <div class="table-wrap" data-data-table id="streetlightsTable" data-page-size="10"><table><thead><tr><th>LIGHT ID</th><th>LOCATION</th><th>ZONE</th><th>CONFIGURATION</th><th>STATUS</th></tr></thead><tbody>
    <c:forEach items="${streetlights}" var="light"><tr data-data-row data-status="<c:out value='${light.status}'/>"><td><strong><c:out value="${light.code}"/></strong></td><td><c:out value="${light.location}"/></td><td><c:out value="${light.zoneId == null ? '—' : light.zoneId}"/></td><td><c:out value="${light.configuration.wattage}"/> W · <c:out value="${light.configuration.brightnessMode}"/> · <c:out value="${light.configuration.colorTemperature}"/> K · <c:out value="${light.configuration.sensorProfile}"/></td><td><span class="pill ${light.status == 'ON' ? 'pill-success' : light.status == 'MAINTENANCE' ? 'pill-warning' : 'pill-neutral'}"><c:out value="${light.status}"/></span></td></tr></c:forEach>
    <c:if test="${empty streetlights}"><tr data-empty-state><td colspan="5" class="empty-state"><strong>No streetlights registered</strong><br>Create a simulated fleet to populate the register.</td></tr></c:if>
    </tbody></table></div>
</section>
<%@ include file="fragments/footer.jspf" %>
