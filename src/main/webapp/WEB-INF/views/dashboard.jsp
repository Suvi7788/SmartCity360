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
    <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon teal">◈</span><span class="kpi-label">STREAM RUNS</span></div><strong><c:choose><c:when test="${not empty stats}"><c:out value="${stats.streamRuns}"/></c:when><c:otherwise>—</c:otherwise></c:choose></strong><small>Processed meter streams</small></article>
    <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon blue">⌘</span><span class="kpi-label">SMART ZONES</span></div><strong><c:choose><c:when test="${not empty stats}"><c:out value="${stats.zones}"/></c:when><c:otherwise>—</c:otherwise></c:choose></strong><small>City hierarchy nodes</small></article>
    <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon amber">◎</span><span class="kpi-label">OPEN COMPLAINTS</span></div><strong><c:choose><c:when test="${not empty stats}"><c:out value="${stats.openComplaints}"/></c:when><c:otherwise>—</c:otherwise></c:choose></strong><small>Awaiting resolution</small></article>
    <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon green">☼</span><span class="kpi-label">STREETLIGHTS</span></div><strong><c:choose><c:when test="${not empty stats}"><c:out value="${stats.streetlights}"/></c:when><c:otherwise>—</c:otherwise></c:choose></strong><small>Managed instances</small></article>
</section>
<section class="panel-grid">
    <article class="panel welcome-panel">
        <div class="panel-heading"><div><p class="eyebrow">CONTROL CENTER</p><h2>City systems at a glance</h2></div><span class="panel-icon">✦</span></div>
        <p class="muted-copy">Operate simulated city services through five focused modules. Each module demonstrates a software design pattern with records persisted to MySQL.</p>
        <div class="module-links">
            <a href="${pageContext.request.contextPath}/complaints"><span>03</span><div><strong>Complaints</strong><small>Chain of Responsibility · Report and track service issues</small></div><b>→</b></a>
            <c:if test="${sessionScope.authenticatedUser.roleName == 'CITY_MANAGER'}">
                <a href="${pageContext.request.contextPath}/security-streams"><span>01</span><div><strong>Security streams</strong><small>Decorator · Process energy meter data</small></div><b>→</b></a>
                <a href="${pageContext.request.contextPath}/smart-zones"><span>02</span><div><strong>Smart zones</strong><small>Composite · Manage the city hierarchy</small></div><b>→</b></a>
                <a href="${pageContext.request.contextPath}/streetlights"><span>04</span><div><strong>Streetlights</strong><small>Flyweight · Share configurations at scale</small></div><b>→</b></a>
                <a href="${pageContext.request.contextPath}/policies"><span>05</span><div><strong>City policies</strong><small>Interpreter · Evaluate the city DSL</small></div><b>→</b></a>
            </c:if>
        </div>
    </article>
    <article class="panel status-panel">
        <div class="panel-heading"><div><p class="eyebrow">PLATFORM HEALTH</p><h2>Service status</h2></div></div>
        <div class="service-status"><span class="status-icon green">✓</span><div><strong>Web application</strong><small>Dashboard request is being served</small></div><span class="pill pill-success"><c:out value="${webApplicationStatus}"/></span></div>
        <div class="service-status"><span class="status-icon blue">↔</span><div><strong>Database</strong><small>MySQL via JDBC resource</small></div><span class="pill ${databaseStatus == 'CONNECTED' ? 'pill-success' : 'pill-danger'}"><c:out value="${databaseStatus}"/></span></div>
        <div class="service-status"><span class="status-icon teal">▣</span><div><strong>Application services</strong><small>Required module services initialized</small></div><span class="pill ${requiredServicesAvailable ? 'pill-success' : 'pill-danger'}"><c:out value="${requiredServicesAvailable ? 'READY' : 'UNAVAILABLE'}"/></span></div>
        <div class="service-status"><span class="status-icon teal">▣</span><div><strong>Active policies</strong><small><c:choose><c:when test="${not empty stats}"><c:out value="${stats.activePolicies}"/> enabled for evaluation</c:when><c:otherwise>Unavailable until the database responds</c:otherwise></c:choose></small></div><span class="pill ${not empty stats and stats.activePolicies > 0 ? 'pill-success' : 'pill-neutral'}"><c:choose><c:when test="${not empty stats and stats.activePolicies > 0}">ACTIVE</c:when><c:when test="${not empty stats}">NONE</c:when><c:otherwise>UNKNOWN</c:otherwise></c:choose></span></div>
        <div class="callout"><strong>Simulation environment</strong><p>Operations in this assessment demo update application records only. No external city infrastructure is controlled.</p></div>
    </article>
</section>
<%@ include file="fragments/footer.jspf" %>
