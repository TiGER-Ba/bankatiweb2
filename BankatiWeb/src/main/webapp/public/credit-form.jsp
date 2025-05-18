<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%
  var ctx = request.getContextPath();
  var connectedUser = (ma.bankati.model.users.User) session.getAttribute("connectedUser");
%>

<html>
<head>
  <title>Nouvelle demande de crédit</title>
  <link rel="stylesheet" href="<%= ctx %>/assets/css/bootstrap.min.css">
  <link rel="stylesheet" href="<%= ctx %>/assets/css/bootstrap-icons.css">
  <link rel="stylesheet" href="<%= ctx %>/assets/css/style.css">
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

<!-- ✅ FORMULAIRE -->
<div class="container w-50 bg-white mt-5 border border-light rounded-3 p-4">
  <h4 class="text-center text-primary mb-4">Nouvelle demande de crédit</h4>

  <form action="${pageContext.request.contextPath}/client/credits/save" method="post">

    <div class="mb-3">
      <label class="form-label text-primary fw-bold">Montant (DH)</label>
      <div class="input-group">
                <span class="input-group-text bg-white">
                    <i class="bi bi-currency-dollar text-primary"></i>
                </span>
        <input type="number"
               class="form-control"
               name="montant"
               placeholder="Montant souhaité"
               min="1000"
               step="100"
               required>
      </div>
    </div>

    <div class="mb-3">
      <label class="form-label text-primary fw-bold">Motif de la demande</label>
      <div class="input-group">
                <span class="input-group-text bg-white">
                    <i class="bi bi-chat-left-text text-primary"></i>
                </span>
        <textarea class="form-control"
                  name="motif"
                  rows="3"
                  placeholder="Expliquez le motif de votre demande..."
                  required></textarea>
      </div>
    </div>

    <div class="text-center mt-4">
      <button type="submit" class="btn btn-outline-success me-2">
        <i class="bi bi-check-circle me-1"></i> Soumettre
      </button>
      <a href="${pageContext.request.contextPath}/client/credits" class="btn btn-outline-secondary">
        <i class="bi bi-x-circle me-1"></i> Annuler
      </a>
    </div>
  </form>
</div>

<!-- ✅ FOOTER -->
<nav class="navbar footer-navbar fixed-bottom bg-white shadow-sm">
  <div class="container d-flex justify-content-between align-items-center w-100">
    <span class="text-muted small"><b class="blue"><i class="bi bi-house-door me-1"></i> Bankati 2025 </b>– © Tous droits réservés</span>
  </div>
</nav>

</body>
</html>