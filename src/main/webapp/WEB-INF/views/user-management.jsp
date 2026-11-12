<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="User management"/>
<c:set var="activeNav" value="users"/>
<%@ include file="fragments/header.jspf" %>
<section class="page-heading">
    <div><p class="eyebrow">ACCOUNT ADMINISTRATION</p><h1>User management</h1><p class="subheading">Create accounts and manage roles, access, and password resets.</p></div>
    <span class="access-badge">CITY MANAGER ONLY</span>
</section>
<section class="content-grid two-column">
    <article class="panel">
        <div class="panel-heading"><div><p class="eyebrow">NEW ACCOUNT</p><h2>Create a user</h2></div></div>
        <form method="post" action="${pageContext.request.contextPath}/user-management" class="form-stack" data-loading-label="Creating account…">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
            <input type="hidden" name="action" value="create">
            <label>Full name<input name="fullName" maxlength="120" autocomplete="name" required></label>
            <label>Username<input name="username" minlength="3" maxlength="64" autocomplete="username" required></label>
            <label>Email<input type="email" name="email" maxlength="254" autocomplete="email" required></label>
            <label>Temporary password<input type="password" name="password" minlength="12" maxlength="72" autocomplete="new-password" required><small class="form-hint">Use 12–72 UTF-8 bytes. Share temporary credentials through a secure channel.</small></label>
            <label>Role<select name="role" required><c:forEach items="${roles}" var="role"><option value="<c:out value='${role}'/>"><c:out value="${role}"/></option></c:forEach></select></label>
            <button type="submit" class="button button-primary">Create account <span>→</span></button>
        </form>
    </article>
    <article class="panel info-panel">
        <p class="eyebrow">ACCESS POLICY</p><h2>Role boundaries</h2>
        <div class="flow-list">
            <div><span>01</span><strong>Resident</strong><small>Submit and view only their own complaints</small></div>
            <div><span>02</span><strong>Local Staff</strong><small>Process complaints assigned to Local Staff</small></div>
            <div><span>03</span><strong>Supervisor</strong><small>Process complaints assigned to Supervisors</small></div>
            <div><span>04</span><strong>City Manager</strong><small>Manage users and access all four operations modules</small></div>
        </div>
        <div class="callout"><strong>Account security</strong><p>Passwords are stored as BCrypt hashes. User lists never expose password hashes; deactivated users are denied access on their next request.</p></div>
    </article>
</section>
<section class="panel table-panel">
    <div class="panel-heading"><div><p class="eyebrow">REGISTERED ACCOUNTS</p><h2>User accounts</h2></div><span class="count-label"><c:out value="${empty users ? 0 : users.size()}"/> accounts</span></div>
    <div class="table-toolbar">
        <label class="table-search">Search users<input type="search" placeholder="Name, username, email…" data-table-search="usersTable"></label>
        <label>Role<select data-table-filter="role" data-table-target="usersTable"><option value="">All roles</option><c:forEach items="${roles}" var="role"><option value="<c:out value='${role}'/>"><c:out value="${role}"/></option></c:forEach></select></label>
        <label>Status<select data-table-filter="status" data-table-target="usersTable"><option value="">All statuses</option><option value="true">Active</option><option value="false">Inactive</option></select></label>
        <span class="table-match-count" data-table-count="usersTable" aria-live="polite"></span>
    </div>
    <div class="table-wrap" data-data-table id="usersTable" data-page-size="10"><table><thead><tr><th>USER</th><th>ROLE</th><th>STATUS</th><th>CHANGE ROLE</th><th>ACCESS</th><th>RESET PASSWORD</th></tr></thead><tbody>
    <c:forEach items="${users}" var="user">
        <tr data-data-row data-role="<c:out value='${user.roleName}'/>" data-status="<c:out value='${user.active}'/>">
            <td><strong><c:out value="${user.fullName}"/></strong><small class="cell-sub"><c:out value="${user.username}"/> · <c:out value="${user.email}"/></small></td>
            <td><c:out value="${user.roleName}"/></td>
            <td><span class="pill ${user.active ? 'pill-success' : 'pill-neutral'}"><c:out value="${user.active ? 'ACTIVE' : 'INACTIVE'}"/></span></td>
            <td>
                <form method="post" action="${pageContext.request.contextPath}/user-management" class="inline-actions">
                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                    <input type="hidden" name="action" value="role"><input type="hidden" name="userId" value="<c:out value='${user.id}'/>">
                    <select name="role" aria-label="Role for <c:out value='${user.username}'/>"><c:forEach items="${roles}" var="role"><option value="<c:out value='${role}'/>" ${user.roleName == role ? 'selected' : ''}><c:out value="${role}"/></option></c:forEach></select>
                    <button class="button button-small button-quiet" type="submit">Save</button>
                </form>
            </td>
            <td>
                <form method="post" action="${pageContext.request.contextPath}/user-management" class="inline-actions">
                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                    <input type="hidden" name="action" value="active"><input type="hidden" name="userId" value="<c:out value='${user.id}'/>">
                    <input type="hidden" name="active" value="${user.active ? 'false' : 'true'}">
                    <button class="button button-small ${user.active ? 'button-danger' : 'button-quiet'}" type="submit" ${user.active ? 'data-confirm="Deactivate this account?"' : ''}><c:out value="${user.active ? 'Deactivate' : 'Activate'}"/></button>
                </form>
            </td>
            <td>
                <form method="post" action="${pageContext.request.contextPath}/user-management" class="inline-actions">
                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                    <input type="hidden" name="action" value="password"><input type="hidden" name="userId" value="<c:out value='${user.id}'/>">
                    <input type="password" name="password" minlength="12" maxlength="72" placeholder="New password" aria-label="New password for <c:out value='${user.username}'/>" required>
                    <button class="button button-small button-quiet" type="submit">Reset</button>
                </form>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty users}"><tr data-empty-state><td colspan="6" class="empty-state"><strong>No user accounts found</strong><br>Create an account above to get started.</td></tr></c:if>
    </tbody></table></div>
</section>
<%@ include file="fragments/footer.jspf" %>
