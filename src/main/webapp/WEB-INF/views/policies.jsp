<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="City policies"/>
<c:set var="activeNav" value="policies"/>
<%@ include file="fragments/header.jspf" %>
<section class="page-heading"><div><p class="eyebrow">INTERPRETER PATTERN</p><h1>City policies</h1><p class="subheading">Define a small, validated policy language and evaluate it against supplied city data.</p></div><span class="pattern-badge violet-badge">INTERPRETER</span></section>
<section class="content-grid two-column">
    <article class="panel">
        <div class="panel-heading"><div><p class="eyebrow">POLICY EDITOR</p><h2>Create a policy</h2></div></div>
        <form method="post" action="${pageContext.request.contextPath}/policies" class="form-stack" data-loading-label="Saving policy…">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
            <input type="hidden" name="action" value="save">
            <c:if test="${not empty editingPolicy}"><input type="hidden" name="policyId" value="<c:out value='${editingPolicy.id}'/>"></c:if>
            <label>Policy name<input name="name" maxlength="120" value="<c:out value='${editingPolicy.name}'/>" placeholder="Quiet hours threshold" required></label>
            <label>Policy expression<input name="expression" maxlength="500" value="<c:out value='${editingPolicy.expression}'/>" placeholder="NOISE_LEVEL <= 60" required></label>
            <label class="check-row"><input type="checkbox" name="active" value="true" ${empty editingPolicy or editingPolicy.active ? 'checked' : ''}><span><strong>Active</strong><small>Allow this policy to be evaluated</small></span></label>
            <button class="button button-primary" type="submit"><c:out value="${empty editingPolicy ? 'Validate & save' : 'Update policy'}"/> <span>→</span></button>
        </form>
        <div class="hierarchy-note"><strong>Supported DSL</strong><p>Comparisons: <code>&lt;</code>, <code>&lt;=</code>, <code>=</code>, <code>&gt;=</code>, <code>&gt;</code>. Combine comparisons with <code>AND</code>. Variables: <code>NOISE_LEVEL</code> and <code>PARKING_HOUR</code>.</p></div>
    </article>
    <article class="panel">
        <div class="panel-heading"><div><p class="eyebrow">EVALUATION</p><h2>Test a policy</h2></div></div>
        <form method="post" action="${pageContext.request.contextPath}/policies" class="form-stack" data-loading-label="Evaluating policy…">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
            <input type="hidden" name="action" value="evaluate">
            <label>Policy<select name="policyId" required><option value="">Choose a policy</option><c:forEach items="${policies}" var="policy"><option value="<c:out value='${policy.id}'/>"><c:out value="${policy.name}"/> · <c:out value="${policy.expression}"/></option></c:forEach></select></label>
            <label>Input values<input name="inputValues" placeholder="NOISE_LEVEL=55;PARKING_HOUR=9" required></label>
            <button class="button button-secondary" type="submit">Evaluate expression <span>→</span></button>
        </form>
        <div class="dsl-example"><code>NOISE_LEVEL &lt;= 60 AND PARKING_HOUR &gt;= 8 AND PARKING_HOUR &lt;= 18</code><span>All comparisons must be true.</span></div>
    </article>
</section>
<section class="panel table-panel">
    <div class="panel-heading"><div><p class="eyebrow">MANAGED RULES</p><h2>Saved policies</h2></div><span class="count-label"><c:out value="${empty policies ? 0 : policies.size()}"/> policies</span></div>
    <div class="table-toolbar">
        <label class="table-search">Search policies<input type="search" placeholder="Name or expression…" data-table-search="policiesTable"></label>
        <label>Status<select data-table-filter="status" data-table-target="policiesTable"><option value="">All statuses</option><option value="true">ACTIVE</option><option value="false">INACTIVE</option></select></label>
        <span class="table-match-count" data-table-count="policiesTable" aria-live="polite"></span>
    </div>
    <div class="table-wrap" data-data-table id="policiesTable" data-page-size="10"><table><thead><tr><th>NAME</th><th>DSL EXPRESSION</th><th>STATUS</th><th>ACTION</th></tr></thead><tbody><c:forEach items="${policies}" var="policy"><tr data-data-row data-status="<c:out value='${policy.active}'/>"><td><strong><c:out value="${policy.name}"/></strong></td><td><code><c:out value="${policy.expression}"/></code></td><td><span class="pill ${policy.active ? 'pill-success' : 'pill-neutral'}"><c:out value="${policy.active ? 'ACTIVE' : 'INACTIVE'}"/></span></td><td><a class="table-link" href="${pageContext.request.contextPath}/policies?edit=<c:out value='${policy.id}'/>">Edit</a></td></tr></c:forEach><c:if test="${empty policies}"><tr data-empty-state><td colspan="4" class="empty-state"><strong>No policies saved</strong><br>Create a validated city rule to get started.</td></tr></c:if></tbody></table></div>
</section>
<section class="panel table-panel">
    <div class="panel-heading"><div><p class="eyebrow">EVALUATION LOG</p><h2>Recent results &amp; validation errors</h2></div></div>
    <div class="table-toolbar">
        <label class="table-search">Search evaluation log<input type="search" placeholder="Policy, inputs, validation…" data-table-search="evaluationsTable"></label>
        <label>Result<select data-table-filter="result" data-table-target="evaluationsTable"><option value="">All results</option><option value="true">Passed</option><option value="false">Failed</option><option value="unknown">Validation error</option></select></label>
        <span class="table-match-count" data-table-count="evaluationsTable" aria-live="polite"></span>
    </div>
    <div class="table-wrap" data-data-table id="evaluationsTable" data-page-size="10"><table><thead><tr><th>POLICY</th><th>INPUT VALUES</th><th>RESULT</th><th>VALIDATION</th><th>TIME</th></tr></thead><tbody><c:forEach items="${evaluations}" var="evaluation"><tr data-data-row data-result="${evaluation.result == null ? 'unknown' : evaluation.result}"><td><c:out value="${empty evaluation.policyName ? 'Policy removed' : evaluation.policyName}"/></td><td><code><c:out value="${evaluation.inputValues}"/></code></td><td><c:choose><c:when test="${evaluation.result == null}">—</c:when><c:otherwise><span class="pill ${evaluation.result ? 'pill-success' : 'pill-danger'}"><c:out value="${evaluation.result}"/></span></c:otherwise></c:choose></td><td class="error-cell"><c:out value="${evaluation.validationError}"/></td><td><c:out value="${evaluation.evaluatedAt}"/></td></tr></c:forEach><c:if test="${empty evaluations}"><tr data-empty-state><td colspan="5" class="empty-state"><strong>No evaluations recorded</strong><br>Run a policy evaluation to see its inputs and result here.</td></tr></c:if></tbody></table></div>
</section>
<%@ include file="fragments/footer.jspf" %>
