package ma.bankati.web.controllers.creditControllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.bankati.model.credit.DemandeCredit;
import ma.bankati.service.creditService.ICreditService;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/credits/*")
public class AdminCreditController extends HttpServlet {

    private ICreditService creditService;

    @Override
    public void init() throws ServletException {
        creditService = (ICreditService) getServletContext().getAttribute("creditService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // Afficher toutes les demandes
            List<DemandeCredit> demandes = creditService.getToutesLesDemandes();
            req.setAttribute("demandes", demandes);
            req.getRequestDispatcher("/admin/credits.jsp").forward(req, resp);

        } else if (pathInfo.equals("/pending")) {
            // Afficher uniquement les demandes en attente
            List<DemandeCredit> demandes = creditService.getDemandesEnAttente();
            req.setAttribute("demandes", demandes);
            req.getRequestDispatcher("/admin/credits.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();
        Long creditId = Long.parseLong(req.getParameter("id"));
        String commentaire = req.getParameter("commentaire");

        if (pathInfo.equals("/approve")) {
            creditService.approuverDemande(creditId, commentaire);

        } else if (pathInfo.equals("/reject")) {
            creditService.refuserDemande(creditId, commentaire);
        }

        resp.sendRedirect(req.getContextPath() + "/admin/credits");
    }
}