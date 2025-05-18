<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
  var ctx = request.getContextPath();
  var connectedUser = (ma.bankati.model.users.User) session.getAttribute("connectedUser");
%>

<html>
<head>
  <title>Gestion des crédits</title>
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
          <a class="nav-link text-primary fw-bold" href="<%= ctx %>/users">
            <i class="bi bi-people-fill me-1"></i> Utilisateurs
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link text-primary fw-bold" href="<%= ctx %>/admin/credits">
            <i class="bi bi-credit-card-fill me-1"></i> Crédits
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
  <h4 class="text-center text-primary mb-4">Gestion des demandes de crédit</h4>

  <div class="text-end mb-3">
    <a href="${pageContext.request.contextPath}/admin/credits/pending" class="btn btn-outline-warning">
      <i class="bi bi-clock-fill me-1"></i> En attente seulement
    </a>
    <a href="${pageContext.request.contextPath}/admin/credits" class="btn btn-outline-primary">
      <i class="bi bi-list me-1"></i> Toutes les demandes
    </a>
  </div>

  <table class="table table-hover table-bordered text-center">
    <thead class="table-light blue">
    <tr>
      <th>Client</th>
      <th>Montant</th>
      <th>Motif</th>
      <th>Date</th>
      <th>Statut</th>
      <th>Actions</th>
    </tr>
    </thead>
    <tbody class="bold">
    <c:forEach items="${demandes}" var="demande">
      <tr>
        <td>User ID: ${demande.userId}</td>
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
          <c:if test="${demande.statut == 'EN_ATTENTE'}">
            <button class="btn btn-success btn-sm" data-bs-toggle="modal" data-bs-target="#approveModal${demande.id}">
              <i class="bi bi-check-circle"></i>
            </button>
            <button class="btn btn-danger btn-sm" data-bs-toggle="modal" data-bs-target="#rejectModal${demande.id}">
              <i class="bi bi-x-circle"></i>
            </button>
          </c:if>
        </td>
      </tr>

      <!-- Modal Approuver -->
      <div class="modal fade" id="approveModal${demande.id}" tabindex="-1">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Approuver la demande</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/credits/approve" method="post">
              <div class="modal-body">
                <input type="hidden" name="id" value="${demande.id}">
                <div class="mb-3">
                  <label class="form-label">Commentaire</label>
                  <textarea class="form-control" name="commentaire" rows="3"></textarea>
                </div>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Annuler</button>
                <button type="submit" class="btn btn-success">Approuver</button>
              </div>
            </form>
          </div>
        </div>
      </div>

      <!-- Modal Refuser -->
      <div class="modal fade" id="rejectModal${demande.id}" tabindex="-1">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Refuser la demande</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/credits/reject" method="post">
              <div class="modal-body">
                <input type="hidden" name="id" value="${demande.id}">
                <div class="mb-3">
                  <label class="form-label">Motif de refus</label>
                  <textarea class="form-control" name="commentaire" rows="3" required></textarea>
                </div>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Annuler</button>
                <button type="submit" class="btn btn-danger">Refuser</button>
              </div>
            </form>
          </div>
        </div>
      </div>
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