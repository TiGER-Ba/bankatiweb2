package ma.bankati.web.controllers.creditControllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.bankati.dao.compteDao.ICompteDao;
import ma.bankati.dao.compteDao.sqlDb.CompteDao;
import ma.bankati.model.credit.DemandeCredit;
import ma.bankati.service.creditService.ICreditService;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/credits/*")
public class AdminCreditController extends HttpServlet {

    private ICreditService creditService;
    private ICompteDao compteDao;

    @Override
    public void init() throws ServletException {
        creditService = (ICreditService) getServletContext().getAttribute("creditService");
        compteDao = (ICompteDao) getServletContext().getAttribute("compteDao");

        // Vérifier l'initialisation des services
        System.out.println("AdminCreditController initialisé:");
        System.out.println("- creditService: " + (creditService != null ? "OK" : "NULL!"));
        System.out.println("- compteDao: " + (compteDao != null ? "OK" : "NULL!"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();
        System.out.println("AdminCreditController.doGet - Path: " + pathInfo);

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

        System.out.println("AdminCreditController.doPost - Path: " + pathInfo + ", CreditID: " + creditId);

        if (pathInfo.equals("/approve")) {
            System.out.println("DEMANDE D'APPROBATION - Credit ID: " + creditId);

            // Récupérer les infos de la demande de crédit
            DemandeCredit demande = null;
            List<DemandeCredit> demandes = creditService.getToutesLesDemandes();
            for (DemandeCredit d : demandes) {
                if (d.getId().equals(creditId)) {
                    demande = d;
                    break;
                }
            }

            if (demande != null) {
                System.out.println("DEMANDE TROUVÉE - Credit ID: " + creditId +
                        ", User ID: " + demande.getUserId() +
                        ", Montant: " + demande.getMontant() +
                        ", Statut: " + demande.getStatut());

                // Vérifier le solde initial
                if (compteDao != null) {
                    Long userId = demande.getUserId();
                    var compte = compteDao.findByUserId(userId);
                    if (compte != null) {
                        System.out.println("SOLDE INITIAL - User ID: " + userId +
                                ", Solde: " + compte.getSolde() + " EUR");
                    }
                }

                // Essayer d'abord la méthode directe si disponible
                boolean incrDirect = false;
                if (compteDao instanceof CompteDao) {
                    CompteDao sqlCompteDao = (CompteDao) compteDao;
                    try {
                        incrDirect = sqlCompteDao.incrementerSolde(demande.getUserId(), demande.getMontant());
                        System.out.println("INCRÉMENTATION DIRECTE - Résultat: " + (incrDirect ? "RÉUSSIE" : "ÉCHEC"));
                    } catch (Exception e) {
                        System.err.println("ERREUR pendant l'incrémentation directe: " + e.getMessage());
                        e.printStackTrace();
                        incrDirect = false;
                    }
                }

                // Si l'incrémentation directe a fonctionné, mettre à jour le statut
                if (incrDirect) {
                    try {
                        creditService.approuverDemande(creditId, commentaire);
                        System.out.println("APPROBATION EFFECTUÉE VIA INCRÉMENTATION DIRECTE + MISE À JOUR STATUT");
                    } catch (Exception e) {
                        System.err.println("ERREUR pendant la mise à jour du statut: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    // Sinon, utiliser la méthode standard
                    try {
                        creditService.approuverDemande(creditId, commentaire);
                        System.out.println("APPROBATION EFFECTUÉE VIA MÉTHODE STANDARD");
                    } catch (Exception e) {
                        System.err.println("ERREUR pendant l'approbation standard: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                // Vérifier le solde final
                if (compteDao != null) {
                    Long userId = demande.getUserId();
                    var compte = compteDao.findByUserId(userId);
                    if (compte != null) {
                        System.out.println("SOLDE FINAL - User ID: " + userId +
                                ", Solde: " + compte.getSolde() + " EUR");
                    }
                }
            } else {
                System.err.println("DEMANDE NON TROUVÉE - Credit ID: " + creditId);
                creditService.approuverDemande(creditId, commentaire);
            }

        } else if (pathInfo.equals("/reject")) {
            System.out.println("DEMANDE DE REFUS - Credit ID: " + creditId);
            creditService.refuserDemande(creditId, commentaire);
        }

        resp.sendRedirect(req.getContextPath() + "/admin/credits");
    }
}