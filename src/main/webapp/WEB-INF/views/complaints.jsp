<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Complaint management"/>
<c:set var="activeNav" value="complaints"/>
<%@ include file="fragments/header.jspf" %>
<section class="page-heading"><div><p class="eyebrow">CHAIN OF RESPONSIBILITY</p><h1>Complaint management</h1><p class="subheading">Route citizen reports by severity and keep a traceable escalation history.</p></div><span class="pattern-badge amber-badge">ESCALATION CHAIN</span></section>
<section class="content-grid two-column">
    <article class="panel">
        <c:if test="${sessionScope.authenticatedUser.roleName == 'RESIDENT'}">
        <div class="panel-heading"><div><p class="eyebrow">CITIZEN INTAKE</p><h2>Submit a complaint</h2></div></div>
        <form method="post" action="${pageContext.request.contextPath}/complaints" class="form-stack" data-loading-label="Submitting complaint…">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
            <input type="hidden" name="action" value="submit">
            <label>Complaint details<textarea name="description" rows="4" minlength="8" maxlength="2000" placeholder="Describe the issue and where it occurred..." required></textarea></label>
            <label>Severity<select name="severity" required><option value="LOW">Low · Local staff</option><option value="MEDIUM">Medium · Supervisor</option><option value="HIGH">High · City manager</option></select></label>
            <button class="button button-primary" type="submit">Submit complaint <span>→</span></button>
        </form>
        <div class="hierarchy-note"><strong>Routing policy</strong><p>Low starts with Local Staff, Medium with Supervisor, and High with City Manager. Unresolved cases can move forward through the chain.</p></div>
        </c:if>
        <c:if test="${sessionScope.authenticatedUser.roleName != 'RESIDENT'}">
            <div class="panel-heading"><div><p class="eyebrow">STAFF WORK QUEUE</p><h2>Assigned responsibilities</h2></div></div>
            <div class="hierarchy-note"><strong><c:out value="${sessionScope.authenticatedUser.roleName}"/></strong><p>You can process complaints currently routed to your role. Resident identities and other residents' private intake details are not shown.</p></div>
        </c:if>
    </article>
    <article class="panel">
        <div class="panel-heading"><div><p class="eyebrow">PROCESS FLOW</p><h2>Escalation path</h2></div></div>
        <div class="handler-chain"><div><span>01</span><strong>Local Staff</strong><small>Low severity</small></div><b>→</b><div><span>02</span><strong>Supervisor</strong><small>Medium severity</small></div><b>→</b><div><span>03</span><strong>City Manager</strong><small>High severity</small></div></div>
        <p class="muted-copy">Each assignment and resolution is appended to the complaint history. Escalations update the complaint and history in one database transaction.</p>
    </article>
</section>
<section class="panel table-panel">
    <div class="panel-heading"><div><p class="eyebrow">CASE REGISTER</p><h2>Complaints</h2></div><span class="count-label"><c:out value="${empty complaints ? 0 : complaints.size()}"/> records</span></div>
    <div class="table-toolbar">
        <label class="table-search">Search complaints<input type="search" placeholder="Reference, details, handler…" data-table-search="complaintsTable"></label>
        <label>Severity<select data-table-filter="severity" data-table-target="complaintsTable"><option value="">All severities</option><option>LOW</option><option>MEDIUM</option><option>HIGH</option></select></label>
        <label>Status<select data-table-filter="status" data-table-target="complaintsTable"><option value="">All statuses</option><option>OPEN</option><option>IN_PROGRESS</option><option>RESOLVED</option></select></label>
        <span class="table-match-count" data-table-count="complaintsTable" aria-live="polite"></span>
    </div>
    <div class="table-wrap" data-data-table id="complaintsTable" data-page-size="10"><table><thead><tr><th>REFERENCE</th><th>DETAILS</th><th>SEVERITY</th><th>CURRENT HANDLER</th><th>STATUS</th><th>HISTORY / ACTIONS</th></tr></thead><tbody>
    <c:forEach items="${complaints}" var="complaint"><tr data-data-row data-severity="<c:out value='${complaint.severity}'/>" data-status="<c:out value='${complaint.status}'/>">
        <td><strong><c:out value="${complaint.referenceNo}"/></strong><small class="cell-sub"><c:out value="${complaint.createdAt}"/></small></td>
        <td class="description-cell"><c:out value="${complaint.description}"/></td>
        <td><span class="pill ${complaint.severity == 'HIGH' ? 'pill-danger' : complaint.severity == 'MEDIUM' ? 'pill-warning' : 'pill-neutral'}"><c:out value="${complaint.severity}"/></span></td>
        <td><c:out value="${complaint.currentHandler}"/></td><td><span class="pill ${complaint.status == 'RESOLVED' ? 'pill-success' : 'pill-warning'}"><c:out value="${complaint.status}"/></span></td>
        <td><a class="table-link" href="${pageContext.request.contextPath}/complaints?selected=<c:out value='${complaint.id}'/>">History</a>
        <c:if test="${complaint.status != 'RESOLVED' and complaint.currentHandler == sessionScope.authenticatedUser.roleName and sessionScope.authenticatedUser.roleName != 'RESIDENT'}">
            <form method="post" action="${pageContext.request.contextPath}/complaints" class="inline-actions">
                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                <input type="hidden" name="complaintId" value="<c:out value='${complaint.id}'/>">
                <input type="hidden" name="currentHandler" value="<c:out value='${complaint.currentHandler}'/>">
                <input type="text" name="note" maxlength="500" placeholder="Optional note" aria-label="Escalation note">
                <button class="button button-small button-quiet" name="action" value="escalate" type="submit">Escalate</button>
                <button class="button button-small button-danger" name="action" value="resolve" type="submit" data-confirm="Resolve this complaint? This action cannot be undone.">Resolve</button>
            </form>
        </c:if></td>
    </tr></c:forEach>
    <c:if test="${empty complaints}"><tr data-empty-state><td colspan="6" class="empty-state"><strong>No complaints yet</strong><br>New citizen reports will appear here after submission.</td></tr></c:if>
    </tbody></table></div>
</section>
<c:if test="${not empty history}">
    <section class="panel table-panel">
        <div class="panel-heading"><div><p class="eyebrow">AUDIT TRAIL</p><h2>Complaint history · #<c:out value="${selectedComplaintId}"/></h2></div></div>
        <div class="timeline"><c:forEach items="${history}" var="event"><div class="timeline-item"><span class="timeline-dot"></span><div><strong><c:out value="${event.handler}"/> · <c:out value="${event.action}"/> · <c:out value="${event.actorRole}"/></strong><small><c:out value="${event.occurredAt}"/></small><c:if test="${not empty event.note}"><p><c:out value="${event.note}"/></p></c:if></div></div></c:forEach><c:if test="${empty history}"><p class="empty-state">No activity has been recorded for this complaint yet.</p></c:if></div>
    </section>
</c:if>
<%@ include file="fragments/footer.jspf" %>
