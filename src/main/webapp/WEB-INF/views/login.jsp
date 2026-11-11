<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sign in · SmartCity360</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/dashboard.css">
</head>
<body class="login-page">
<main class="login-shell">
    <section class="login-card">
        <a class="brand login-brand" href="${pageContext.request.contextPath}/sign-in">
            <span class="brand-mark">S</span>
            <span><strong>SmartCity</strong><small>360 MANAGEMENT</small></span>
        </a>
        <p class="eyebrow">SECURE CITY OPERATIONS</p>
        <h1>Welcome back</h1>
        <p class="subheading">Sign in with your SmartCity360 account.</p>
        <c:if test="${not empty error}">
            <div class="alert alert-error" role="alert" aria-live="assertive"><span>!</span><c:out value="${error}"/></div>
        </c:if>
        <c:if test="${param.required == '1'}">
            <div class="alert alert-error" role="alert">Please sign in to continue.</div>
        </c:if>
        <c:if test="${param.expired == '1'}">
            <div class="alert alert-error" role="alert">Your session has ended. Please sign in again.</div>
        </c:if>
        <c:if test="${param.logout == '1'}">
            <div class="alert alert-success" role="status">You have been signed out.</div>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/sign-in" class="form-stack" data-loading-label="Signing in…">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
            <label>Username or email<input name="identifier" autocomplete="username" maxlength="254" required autofocus></label>
            <label>Password
                <span class="password-field"><input id="loginPassword" type="password" name="password" autocomplete="current-password" required><button type="button" class="password-toggle" data-password-toggle="loginPassword" aria-label="Show password">Show</button></span>
            </label>
            <button type="submit" class="button button-primary login-submit">Sign in <span>→</span></button>
        </form>
        <p class="login-footnote">Access is provided by your SmartCity360 account administrator.</p>
    </section>
</main>
<script src="${pageContext.request.contextPath}/assets/js/dashboard.js"></script>
</body>
</html>
