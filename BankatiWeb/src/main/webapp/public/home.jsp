<%@ page import="ma.bankati.model.users.User" pageEncoding="UTF-8" %>
<%@ page import="ma.bankati.model.data.MoneyData" %>
<%@ page import="ma.bankati.model.data.Devise" %>
<%@ page import="ma.bankati.model.compte.Compte" %>
<%@ page import="ma.bankati.dao.compteDao.ICompteDao" %>
<%@ page import="ma.bankati.service.moneyServices.MultiCurrencyService" %>
<html>
<head>
	<title>Accueil Client</title>
	<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/bootstrap.min.css">
	<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
	<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
	<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</head>
	<%
	var ctx = request.getContextPath();
	var user = (User) session.getAttribute("connectedUser");
	var appName = (String) application.getAttribute("AppName");

	// Récupérer le solde et la devise directement
	var result = (MoneyData) request.getAttribute("result");
	var deviseSelectionnee = (Devise) request.getAttribute("deviseSelectionnee");

    // Fallback en cas de problème
    if (result == null && user != null) {
        // Récupérer directement depuis la base de données
        var compteDao = (ICompteDao) application.getAttribute("compteDao");
        if (compteDao != null) {
            Compte compte = compteDao.findByUserId(user.getId());
            double solde = (compte != null) ? compte.getSolde() : 0.0;

            // Convertir selon la devise
            var moneyService = (MultiCurrencyService) application.getAttribute("moneyService");
            if (moneyService != null && deviseSelectionnee != null) {
                result = moneyService.convertAmount(solde, deviseSelectionnee);
            } else {
                result = new MoneyData(solde, Devise.Dh);
            }
        }
    }

    // Dernière sécurité
    if (result == null) {
        result = new MoneyData(0.0, Devise.Dh);
    }
%>
<body class="bgBlue Optima">

<!-- ✅ NAVBAR HAUT -->
<nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
	<div class="container-fluid">
		<a class="navbar-brand d-flex align-items-center" href="<%= ctx %>/home">
			<img src="<%= ctx %>/assets/img/login.png" alt="Logo" width="40" height="40" class="d-inline-block align-text-top me-2">
			<strong class="blue ml-1"><%= appName %></strong>
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
					<a class="nav-link text-primary fw-bold" href="<%= ctx %>/client/credits">
						<i class="bi bi-credit-card-fill me-1"></i> Mes crédits
					</a>
				</li>
			</ul>
		</div>

		<div class="dropdown d-flex align-items-center">
			<a class="btn btn-sm btn-light border dropdown-toggle text-success fw-bold"
			   href="#" role="button" id="dropdownSessionMenu" data-bs-toggle="dropdown" aria-expanded="false">
				<i class="bi bi-person-circle me-1"></i> <b><%= user.getRole() %></b> : <i><%= user.getFirstName() + " " + user.getLastName() %></i>
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
<div class="container w-50 bg-white mt-5 border border-light rounded-3 mb-5 p-4">
	<div class="card-body text-center">
		<h3 class="mt-4 mb-3 text-primary font-weight-bold">
			Bienvenue <%= user.getFirstName() %>
		</h3>

		<div class="row mt-4">
			<div class="col-md-12 mb-3">
				<div class="card border-primary">
					<div class="card-body">
						<h5 class="card-title text-primary">Solde de votre compte</h5>
						<p class="h3 text-danger font-weight-bold"><%= result %></p>

						<!-- Sélecteur de devise -->
						<form action="<%= ctx %>/home" method="get" class="mt-3">
							<div class="input-group">
								<label class="input-group-text">Devise :</label>
								<select name="devise" class="form-select" onchange="this.form.submit()">
									<option value="Dh" <%= "Dh".equals(deviseSelectionnee.toString()) ? "selected" : "" %>>Dirham (DH)</option>
									<option value="€" <%= "€".equals(deviseSelectionnee.toString()) ? "selected" : "" %>>Euro (€)</option>
									<option value="$" <%= "$".equals(deviseSelectionnee.toString()) ? "selected" : "" %>>Dollar ($)</option>
									<option value="£" <%= "£".equals(deviseSelectionnee.toString()) ? "selected" : "" %>>Pound (£)</option>
								</select>
							</div>
						</form>
					</div>
				</div>
			</div>
		</div>

		<div class="row mt-3">
			<div class="col-md-6 mb-3">
				<a href="<%= ctx %>/home" class="btn btn-outline-primary btn-lg w-100">
					<i class="bi bi-currency-exchange me-2"></i>
					Convertir devises
				</a>
			</div>
			<div class="col-md-6 mb-3">
				<a href="<%= ctx %>/client/credits" class="btn btn-outline-success btn-lg w-100">
					<i class="bi bi-credit-card-fill me-2"></i>
					Mes demandes de crédit
				</a>
			</div>
		</div>

		<hr class="my-4">

		<div class="text-muted">
			<small>Connecté en tant que : <%= user.getUsername() %></small>
		</div>
	</div>
</div>

<!-- ✅ FOOTER FIXÉ EN BAS -->
<nav class="navbar footer-navbar fixed-bottom bg-white">
	<div class="container d-flex justify-content-between align-items-center w-100">
		<span class="text-muted small">
			© <%= appName %> 2025 – Tous droits réservés
		</span>
	</div>
</nav>

</body>
</html>