<%@ page pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Connexion | Bankati</title>
  <link rel="stylesheet" href="${ctx}/assets/css/bootstrap.min.css">
  <link rel="stylesheet" href="${ctx}/assets/css/style.css">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
  <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
</head>
<body style="background: linear-gradient(135deg, #4e54c8, #8f94fb); min-height: 100vh;">
<div class="container">
  <div class="row justify-content-center">
    <div class="col-md-6 col-lg-5">
      <!-- Logo et titre -->
      <div class="text-center text-white my-5">
        <i class="bi bi-bank" style="font-size: 3rem;"></i>
        <h1 class="mt-3 mb-0 fw-bold">Bankati</h1>
        <p class="text-white-50">Votre banque en ligne sécurisée</p>
      </div>

      <!-- Carte de connexion -->
      <div class="card border-0 shadow-lg" style="border-radius: 15px; overflow: hidden;">
        <div class="card-header bg-white text-center border-0 pt-4 pb-3">
          <h4 class="text-primary fw-bold">Connexion</h4>
        </div>
        <div class="card-body p-4">
          <form action="login" method="post" class="needs-validation" novalidate>
            <!-- Nom d'utilisateur -->
            <div class="mb-4">
              <label for="username" class="form-label">Nom d'utilisateur</label>
              <div class="input-group">
                  <span class="input-group-text">
                    <i class="bi bi-person-fill text-primary"></i>
                  </span>
                <input type="text"
                       class="form-control ${not empty usernameError ? 'is-invalid' : ''}"
                       id="username"
                       name="lg"
                       placeholder="Entrez votre nom d'utilisateur"
                       required>
                <c:if test="${not empty usernameError}">
                  <div class="invalid-feedback">${usernameError}</div>
                </c:if>
              </div>
            </div>

            <!-- Mot de passe -->
            <div class="mb-4">
              <label for="password" class="form-label">Mot de passe</label>
              <div class="input-group">
                  <span class="input-group-text">
                    <i class="bi bi-lock-fill text-primary"></i>
                  </span>
                <input type="password"
                       class="form-control ${not empty passwordError ? 'is-invalid' : ''}"
                       id="password"
                       name="pss"
                       placeholder="Entrez votre mot de passe"
                       required>
                <button class="btn btn-outline-secondary" type="button" id="togglePassword">
                  <i class="bi bi-eye"></i>
                </button>
                <c:if test="${not empty passwordError}">
                  <div class="invalid-feedback">${passwordError}</div>
                </c:if>
              </div>
            </div>

            <!-- Message global -->
            <c:if test="${not empty globalMessage}">
              <div class="alert alert-info mb-4">
                <i class="bi bi-info-circle me-2"></i> ${globalMessage}
              </div>
            </c:if>

            <!-- Bouton de connexion -->
            <div class="d-grid">
              <button type="submit" class="btn btn-primary py-2">
                <i class="bi bi-box-arrow-in-right me-2"></i> Se connecter
              </button>
            </div>
          </form>
        </div>
        <div class="card-footer bg-white text-center border-0 pb-4">
          <p class="text-muted mb-0">
            <i class="bi bi-shield-lock me-1"></i> Connexion sécurisée
          </p>
        </div>
      </div>

      <!-- Pied de page -->
      <div class="text-center mt-4 mb-5">
        <p class="text-white-50">© Bankati 2025 - Tous droits réservés</p>
      </div>
    </div>
  </div>
</div>

<script>
  // Toggle password visibility
  document.addEventListener('DOMContentLoaded', function() {
    const togglePassword = document.querySelector('#togglePassword');
    const password = document.querySelector('#password');

    togglePassword.addEventListener('click', function() {
      const type = password.getAttribute('type') === 'password' ? 'text' : 'password';
      password.setAttribute('type', type);
      this.querySelector('i').classList.toggle('bi-eye');
      this.querySelector('i').classList.toggle('bi-eye-slash');
    });

    // Form validation
    const forms = document.querySelectorAll('.needs-validation');
    Array.from(forms).forEach(function(form) {
      form.addEventListener('submit', function(event) {
        if (!form.checkValidity()) {
          event.preventDefault();
          event.stopPropagation();
        }
        form.classList.add('was-validated');
      }, false);
    });
  });
</script>
</body>
</html>