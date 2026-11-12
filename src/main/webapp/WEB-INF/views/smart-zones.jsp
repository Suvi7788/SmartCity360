<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Smart zones"/>
<c:set var="activeNav" value="zones"/>
<%@ include file="fragments/header.jspf" %>
<section class="page-heading"><div><h1>Smart Zones</h1><p class="subheading">Build a city hierarchy and apply simulated operations recursively to any node.</p></div></section>
<section class="content-grid two-column zone-layout">
    <article class="panel">
        <div class="panel-heading"><div><p class="eyebrow">CITY HIERARCHY</p><h2>Zone tree</h2></div><span class="count-label"><c:out value="${empty zoneRows ? 0 : zoneRows.size()}"/> nodes</span></div>
        <label class="tree-search">Find a zone<input type="search" placeholder="Search zone names…" data-zone-search></label>
        <div class="zone-tree">
            <c:forEach items="${zoneRows}" var="row">
                <div class="zone-row" data-zone-row="<c:out value='${row.id}'/>" data-parent="<c:out value='${row.parentId}'/>" data-depth="<c:out value='${row.depth}'/>" style="--depth:${row.depth}">
                    <c:choose><c:when test="${row.hasChildren}"><button type="button" class="tree-toggle" data-zone-toggle aria-expanded="true" aria-label="Collapse children">−</button></c:when><c:otherwise><span class="tree-spacer"></span></c:otherwise></c:choose>
                    <span class="zone-type-icon"><c:out value="${row.type}"/></span>
                    <strong><c:out value="${row.name}"/></strong>
                    <span class="pill ${row.powerOn ? 'pill-success' : 'pill-danger'}"><c:out value="${row.powerOn ? 'POWER ON' : 'POWER OFF'}"/></span>
                    <span class="zone-id">#<c:out value="${row.id}"/></span>
                    <div class="zone-actions">
                        <form method="post" action="${pageContext.request.contextPath}/smart-zones">
                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                            <input type="hidden" name="action" value="execute"><input type="hidden" name="zoneId" value="<c:out value='${row.id}'/>">
                            <select name="operation" aria-label="Operation for <c:out value='${row.name}'/>" data-zone-operation><option value="SHUTDOWN_POWER">Shutdown power</option><option value="RESTORE_POWER">Restore power</option><option value="REPORT_USAGE">Report usage</option></select>
                            <button class="button button-small button-quiet" type="submit">Run</button>
                        </form>
                    </div>
                </div>
            </c:forEach>
            <c:if test="${empty zoneRows}"><div class="empty-state"><strong>Your city tree is empty</strong><br>Add a city, then build out districts, streets, and buildings.</div></c:if>
        </div>
        <c:if test="${not empty operationResults}">
            <div class="results-box"><strong>Recursive operation results</strong><ul><c:forEach items="${operationResults}" var="result"><li><c:out value="${result}"/></li></c:forEach></ul></div>
        </c:if>
    </article>
    <article class="panel">
        <div class="panel-heading"><div><p class="eyebrow">HIERARCHY MANAGEMENT</p><h2>Add a zone</h2></div></div>
        <form method="post" action="${pageContext.request.contextPath}/smart-zones" class="form-stack" data-loading-label="Adding zone…">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
            <input type="hidden" name="action" value="add">
            <label>Parent zone<select name="parentId"><option value="">No parent · Create a City</option><c:forEach items="${zoneRows}" var="row"><c:if test="${row.canHaveChildren}"><option value="<c:out value='${row.id}'/>"><c:out value="${row.type}"/> · <c:out value="${row.name}"/></option></c:if></c:forEach></select></label>
            <label>Zone name<input name="name" maxlength="120" placeholder="e.g. North District" required></label>
            <label>Initial usage (kWh, building only)<input name="usageKwh" type="number" min="0" max="999999999.999" step="0.001" value="0"></label>
            <button type="submit" class="button button-primary">Add child node <span>→</span></button>
        </form>
        <div class="hierarchy-note"><strong>Allowed hierarchy</strong><p>City → District → Street → Building. Parent-child levels are validated by the service; buildings cannot have children.</p></div>
    </article>
</section>
<%@ include file="fragments/footer.jspf" %>
