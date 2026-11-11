<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Security streams"/>
<c:set var="activeNav" value="security"/>
<%@ include file="fragments/header.jspf" %>
<section class="page-heading"><div><p class="eyebrow">DECORATOR PATTERN</p><h1>Security streams</h1><p class="subheading">Compose processing layers dynamically around a simulated energy meter stream.</p></div><span class="pattern-badge">DECORATOR</span></section>
<section class="content-grid two-column">
    <article class="panel">
        <div class="panel-heading"><div><p class="eyebrow">NEW SIMULATION</p><h2>Configure stream</h2></div></div>
        <form method="post" action="${pageContext.request.contextPath}/security-streams" class="form-stack" data-loading-label="Processing stream…">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
            <label>Meter ID<input name="meterId" value="MTR-001" maxlength="80" required></label>
            <label>Energy usage (kWh)<input name="energyKwh" type="number" min="0" max="1000000" step="0.01" value="42.8" required></label>
            <fieldset class="decorator-options"><legend>Processing decorators</legend>
                <label class="check-row"><input type="checkbox" name="decorator" value="Authentication"><span><strong>Authentication</strong><small>Attach an HMAC-SHA256 integrity signature</small></span></label>
                <label class="check-row"><input type="checkbox" name="decorator" value="Encryption"><span><strong>Encryption</strong><small>Encrypt with AES-GCM and a fresh IV</small></span></label>
                <label class="check-row"><input type="checkbox" name="decorator" value="Compression"><span><strong>Compression</strong><small>Compress with GZIP</small></span></label>
            </fieldset>
            <p class="form-hint">Authentication and encryption require the server environment keys <code>SMARTCITY_STREAM_HMAC_KEY</code> and <code>SMARTCITY_STREAM_AES_KEY</code> (Base64 encoded). Keys are never stored in MySQL.</p>
            <button class="button button-primary" type="submit">Run processing chain <span>→</span></button>
        </form>
    </article>
    <article class="panel info-panel">
        <p class="eyebrow">CHAIN PREVIEW</p><h2>Layer by layer</h2>
        <div class="flow-list"><div><span>1</span><strong>Raw meter source</strong><small>JSON energy reading</small></div><div><span>2</span><strong>Selected decorators</strong><small>Composed for this request</small></div><div><span>3</span><strong>Processed output</strong><small>Base64 display for binary transforms</small></div></div>
        <div class="callout"><strong>Decorator order</strong><p>Selected transforms run in a safe, deterministic order: compression, encryption, then authentication. Each stage is independently optional and can be combined with the others.</p></div>
    </article>
</section>
<section class="panel table-panel">
    <div class="panel-heading"><div><p class="eyebrow">PERSISTED HISTORY</p><h2>Recent stream runs</h2></div><span class="count-label"><c:out value="${empty runs ? 0 : runs.size()}"/> records</span></div>
    <div class="table-toolbar">
        <label class="table-search">Search stream history<input type="search" placeholder="Meter, decorator, payload…" data-table-search="streamRunsTable"></label>
        <span class="table-match-count" data-table-count="streamRunsTable" aria-live="polite"></span>
    </div>
    <div class="table-wrap" data-data-table id="streamRunsTable" data-page-size="10"><table><thead><tr><th>METER</th><th>DECORATOR CHAIN</th><th>ORIGINAL DATA</th><th>PROCESSED DATA</th><th>TIME</th></tr></thead><tbody>
    <c:forEach items="${runs}" var="run"><tr data-data-row><td><strong><c:out value="${run.meterId}"/></strong></td><td><span class="pill pill-neutral"><c:out value="${run.decoratorChain}"/></span></td><td><code class="payload"><c:out value="${run.originalPayload}"/></code></td><td><details><summary>View encoded output</summary><code class="payload"><c:out value="${run.processedPayload}"/></code></details></td><td><c:out value="${run.createdAt}"/></td></tr></c:forEach>
    <c:if test="${empty runs}"><tr data-empty-state><td colspan="5" class="empty-state"><strong>No stream runs yet</strong><br>Process a meter reading to review its original and transformed data.</td></tr></c:if>
    </tbody></table></div>
</section>
<%@ include file="fragments/footer.jspf" %>
