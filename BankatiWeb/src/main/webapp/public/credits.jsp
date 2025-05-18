<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    var ctx = request.getContextPath();
    var connectedUser = (ma.bankati.model.users.User) session.getAttribute("connectedUser");
%>

<html>
<head>
    <title>Mes demandes de crédit</title>
    <link rel="stylesheet" href="<%= ctx %>/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="<%= ctx %>/assets/css/bootstrap-icons.css">
    <link rel="stylesheet" href="<%= ctx %>/assets/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</head>
<body class="Optima bgBlue">

<!-- ✅ NAVBAR -->
<nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
    <div class="container-fluid">
        <a class="navbar-brand d-flex align-items-center" href="<%= ctx %>/home">
            <img src="<%= ctx %>/assets/img/logoBlue.png" alt="Logo" width="40" height="40" class="d-inline-block align-text-top me-2">
            <strong class="blue ml-1"><%=application.getAttribute("AppName")%></strong>
        </a>

        <div class="collapse navbar-collapse">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link text-primary fw-bold" href="<%= ctx %>/home">
                        <i class="bi bi-house-door me-1"></i> Accueil
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link text-primary fw-bold" href="<%= ctx %>/client/credits">
                        <i class="bi bi-credit-card-fill me-1"></i> Mes crédits
                    </a>
                </li>
            </ul>
        </div>

        <div class="dropdown d-flex align-items-center">
            <a class="btn btn-sm btn-light border dropdown-toggle text-success fw-bold"
               href="#" role="button" id="dropdownSessionMenu" data-bs-toggle="dropdown" aria-expanded="false">
                <i class="bi bi-person-circle me-1"></i> <b><%= connectedUser.getRole() %></b> : <i><%= connectedUser.getFirstName() + " " + connectedUser.getLastName() %></i>
            </a>
            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="dropdownSessionMenu">
                <li><hr class="dropdown-divider"></li>
                <li>
                    <a class="dropdown-item text-danger logout-btn fw-bold" href="<%= ctx %>/logout">
                        <i class="bi bi-box-arrow-right me-1"></i> <b>Déconnexion</b>
                    </a>
                </li>
            </ul>
        </div>
    </div>
</nav>

<!-- ✅ CONTENU PRINCIPAL -->
<div class="container w-75 mt-5 mb-5 bg-white p-4 rounded-3 shadow-sm border border-light">
    <h4 class="text-center text-primary mb-4">Mes demandes de crédit</h4>

    <div class="text-end mb-3">
        <a href="${pageContext.request.contextPath}/client/credits/new" class="btn btn-outline-primary">
            <i class="bi bi-plus-circle me-1"></i> Nouvelle demande
        </a>
    </div>

    <table class="table table-hover table-bordered text-center">
        <thead class="table-light blue">
        <tr>
            <th>Montant</th>
            <th>Motif</th>
            <th>Date de demande</th>
            <th>Statut</th>
            <th>Commentaire</th>
            <th>Actions</th>
        </tr>
        </thead>
        <tbody class="bold">
        <c:forEach items="${demandes}" var="demande">
            <tr>
                <td><fmt:formatNumber value="${demande.montant * 10}" type="currency" currencySymbol="DH"/></td>
                <td>${demande.motif}</td>
                <td><fmt:formatDate value="${demande.dateCreationAsDate}" pattern="dd/MM/yyyy"/></td>
                <td>
                    <c:choose>
                        <c:when test="${demande.statut == 'EN_ATTENTE'}">
                            <span class="badge bg-warning">En attente</span>
                        </c:when>
                        <c:when test="${demande.statut == 'APPROUVEE'}">
                            <span class="badge bg-success">Approuvée</span>
                        </c:when>
                        <c:when test="${demande.statut == 'REFUSEE'}">
                            <span class="badge bg-danger">Refusée</span>
                        </c:when>
                    </c:choose>
                </td>
                <td>
                    <c:if test="${not empty demande.commentaire}">
                        ${demande.commentaire}
                    </c:if>
                </td>
                <td>
                    <c:if test="${demande.statut == 'EN_ATTENTE'}">
                        <a href="${pageContext.request.contextPath}/client/credits/delete?id=${demande.id}"
                           class="btn btn-outline-danger btn-sm"
                           onclick="return confirm('Êtes-vous sûr de vouloir supprimer cette demande?')">
                            <i class="bi bi-trash-fill"></i>
                        </a>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<!-- ✅ FOOTER -->
<nav class="navbar footer-navbar fixed-bottom bg-white shadow-sm">
    <div class="container d-flex justify-content-between align-items-center w-100">
        <span class="text-muted small"><b class="blue"><i class="bi bi-house-door me-1"></i> Bankati 2025 </b>– © Tous droits réservés</span>
    </div>
</nav>

</body>
</html>