<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="City overview"/>
<c:set var="activeNav" value="dashboard"/>
<%@ include file="fragments/header.jspf" %>
<section class="page-heading">
    <div><p class="eyebrow">CITY OPERATIONS</p><h1>Overview</h1><p class="subheading">A database-backed snapshot of recorded city operations.</p></div>
    <span class="date-chip">● &nbsp; System overview</span>
</section>
<section class="kpi-grid">
    <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon teal">◈</span><span class="kpi-label">METER RUNS</span></div><strong><c:choose><c:when test="${not empty stats}"><c:out value="${stats.streamRuns}"/></c:when><c:otherwise>—</c:otherwise></c:choose></strong><small>Processed meter readings</small></article>
    <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon blue">⌘</span><span class="kpi-label">SMART ZONES</span></div><strong><c:choose><c:when test="${not empty stats}"><c:out value="${stats.zones}"/></c:when><c:otherwise>—</c:otherwise></c:choose></strong><small>City hierarchy nodes</small></article>
    <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon amber">◎</span><span class="kpi-label">OPEN COMPLAINTS</span></div><strong><c:choose><c:when test="${not empty stats}"><c:out value="${stats.openComplaints}"/></c:when><c:otherwise>—</c:otherwise></c:choose></strong><small>Awaiting resolution</small></article>
    <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon green">☼</span><span class="kpi-label">STREETLIGHTS</span></div><strong><c:choose><c:when test="${not empty stats}"><c:out value="${stats.streetlights}"/></c:when><c:otherwise>—</c:otherwise></c:choose></strong><small>Managed instances</small></article>
</section>
<section class="content-grid">
    <article class="panel welcome-panel">
        <div class="panel-heading"><div><p class="eyebrow">CONTROL CENTER</p><h2>City systems at a glance</h2></div><span class="panel-icon">✦</span></div>
        <p class="muted-copy">Operate simulated city services through five focused modules, with records persisted to MySQL.</p>
        <div class="module-links">
            <a href="${pageContext.request.contextPath}/complaints"><span>03</span><div><strong>Complaints</strong><small>Report and track service issues</small></div><b>→</b></a>
            <c:if test="${sessionScope.authenticatedUser.roleName == 'CITY_MANAGER'}">
                <a href="${pageContext.request.contextPath}/security-streams"><span>01</span><div><strong>Meters</strong><small>Process energy meter data</small></div><b>→</b></a>
                <a href="${pageContext.request.contextPath}/smart-zones"><span>02</span><div><strong>Smart Zones</strong><small>Manage the city hierarchy</small></div><b>→</b></a>
                <a href="${pageContext.request.contextPath}/streetlights"><span>04</span><div><strong>Streetlights</strong><small>Manage streetlight configurations</small></div><b>→</b></a>
                <a href="${pageContext.request.contextPath}/policies"><span>05</span><div><strong>City policies</strong><small>Evaluate city policies</small></div><b>→</b></a>
            </c:if>
        </div>
    </article>
</section>
<%@ include file="fragments/footer.jspf" %>
