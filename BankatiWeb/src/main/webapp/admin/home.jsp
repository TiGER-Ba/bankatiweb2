<%@page import="ma.bankati.model.data.MoneyData" pageEncoding="UTF-8" %>
<%@page import="ma.bankati.model.users.User" %>
<%@page import="ma.bankati.service.statsService.StatsService" %>
<%
    var ctx = request.getContextPath();
%>
<html>
<head>
    <title>Accueil Admin</title>
    <link rel="stylesheet" href="<%= ctx %>/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="<%= ctx %>/assets/css/bootstrap-icons.css">
    <link rel="stylesheet" href="<%= ctx %>/assets/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</head>
    <%
    var connectedUser = (User) session.getAttribute("connectedUser");
    var appName = (String) application.getAttribute("AppName");
    var statsService = (StatsService) application.getAttribute("statsService");

    // Récupérer les statistiques
    int nombreUtilisateurs = statsService != null ? statsService.getNombreUtilisateurs() : 0;
    int nombreDemandesEnAttente = statsService != null ? statsService.getNombreDemandesEnAttente() : 0;
    double montantTotalCreditApprouve = statsService != null ? statsService.getMontantTotalCreditApprouve() : 0.0;
%>
<body class="Optima bgBlue">

<!-- ✅ NAVBAR -->
<nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
    <div class="container-fluid">
        <!-- Logo & Brand -->
        <a class="navbar-brand d-flex align-items-center" href="<%= ctx %>/home">
            <img src="<%= ctx %>/assets/img/logoBlue.png" alt="Logo" width="40" height="40" class="d-inline-block align-text-top me-2">
            <strong class="blue ml-1"><%=application.getAttribute("AppName")%></strong>
        </a>

        <!-- Menu de navigation -->
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
                        <i class="bi bi-credit-card-fill me-1"></i> Gestion Crédits
                    </a>
                </li>
            </ul>
        </div>

        <!-- Infos session avec sous-menu -->
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
<div class="container w-75 bg-white mt-5 border border-light rounded-3 mb-5 p-4">
    <div class="card-body text-center">
        <h4 class="mt-4 mb-4 text-primary font-weight-bold">
            Tableau de bord d'administration
        </h4>

        <div class="row mt-4">
            <div class="col-md-6 mb-3">
                <div class="card border-primary h-100">
                    <div class="card-body">
                        <h5 class="card-title text-primary">Statistiques</h5>
                        <ul class="list-group list-group-flush mt-3">
                            <li class="list-group-item d-flex justify-content-between align-items-center">
                                <span>Nombre d'utilisateurs:</span>
                                <span class="badge bg-primary rounded-pill"><%= nombreUtilisateurs %></span>
                            </li>
                            <li class="list-group-item d-flex justify-content-between align-items-center">
                                <span>Demandes en attente:</span>
                                <span class="badge bg-warning rounded-pill"><%= nombreDemandesEnAttente %></span>
                            </li>
                            <li class="list-group-item d-flex justify-content-between align-items-center">
                                <span>Montant total des crédits approuvés:</span>
                                <span class="badge bg-success rounded-pill"><%= montantTotalCreditApprouve %> DH</span>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>

            <div class="col-md-6 mb-3">
                <div class="card border-primary h-100">
                    <div class="card-body">
                        <h5 class="card-title text-primary">Actions rapides</h5>
                        <div class="d-grid gap-2 mt-3">
                            <a href="<%= ctx %>/users" class="btn btn-outline-primary">
                                <i class="bi bi-people-fill me-2"></i>
                                Gérer les utilisateurs
                            </a>
                            <a href="<%= ctx %>/admin/credits" class="btn btn-outline-success">
                                <i class="bi bi-credit-card-fill me-2"></i>
                                Gérer les crédits
                            </a>
                            <a href="<%= ctx %>/admin/credits/pending" class="btn btn-outline-warning">
                                <i class="bi bi-clock-fill me-2"></i>
                                Demandes en attente
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row mt-4">
            <div class="col-12">
                <div class="card border-info">
                    <div class="card-header bg-info text-white">
                        <h5 class="card-title mb-0">Résumé de l'activité</h5>
                    </div>
                    <div class="card-body">
                        <p class="text-center">
                            Bienvenue sur le tableau de bord administrateur de <span class="text-primary font-weight-bold"><%= appName %></span>.
                            De ce panneau, vous pouvez gérer les utilisateurs, approuver ou refuser les demandes de crédit,
                            et consulter les statistiques globales du système.
                        </p>

                        <div class="alert alert-warning" role="alert">
                            <i class="bi bi-exclamation-triangle-fill me-2"></i>
                            Vous avez <strong><%= nombreDemandesEnAttente %></strong> demande(s) de crédit en attente de traitement.
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- ✅ FOOTER FIXÉ EN BAS -->
<nav class="navbar footer-navbar fixed-bottom bg-white shadow-sm">
    <div class="container d-flex justify-content-between align-items-center w-100">
        <span class="text-muted small"><b class="blue"><i class="bi bi-house-door me-1"></i> Bankati 2025 </b>– © Tous droits réservés</span>
    </div>
</nav>

</body>
</html>