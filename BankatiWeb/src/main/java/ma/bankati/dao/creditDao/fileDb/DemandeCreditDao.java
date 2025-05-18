package ma.bankati.dao.creditDao.fileDb;

import ma.bankati.dao.creditDao.IDemandeCreditDao;
import ma.bankati.model.credit.DemandeCredit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DemandeCreditDao implements IDemandeCreditDao {

    private Path path;

    public DemandeCreditDao() {
        try {
            this.path = Paths.get(
                    getClass().getClassLoader()
                            .getResource("FileBase/credits.txt")
                            .toURI()
            );
        } catch (Exception e) {
            System.err.println("FileBase/credits.txt not found");
        }
    }

    private DemandeCredit map(String fileLine) {
        try {
            String[] fields = fileLine.split("-");
            if (fields.length < 8) {
                System.err.println("Invalid line format: " + fileLine);
                return null;
            }

            Long id = Long.parseLong(fields[0]);
            Long userId = Long.parseLong(fields[1]);
            Double montant = Double.parseDouble(fields[2]);
            String motif = fields[3].equals("null") ? null : fields[3];
            String statut = fields[4];
            LocalDate dateCreation = LocalDate.parse(fields[5], DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            LocalDate dateTraitement = fields[6].equals("null") ? null :
                    LocalDate.parse(fields[6], DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String commentaire = fields[7].equals("null") ? null : fields[7];

            return DemandeCredit.builder()
                    .id(id)
                    .userId(userId)
                    .montant(montant)
                    .motif(motif)
                    .statut(statut)
                    .dateCreation(dateCreation)
                    .dateTraitement(dateTraitement)
                    .commentaire(commentaire)
                    .build();
        } catch (Exception e) {
            System.err.println("Error parsing line: " + fileLine);
            e.printStackTrace();
            return null;
        }
    }

    private String mapToFileLine(DemandeCredit credit) {
        String dateTraitementStr = credit.getDateTraitement() == null ? "null" :
                credit.getDateTraitement().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        return credit.getId() + "-" +
                credit.getUserId() + "-" +
                credit.getMontant() + "-" +
                (credit.getMotif() == null ? "null" : credit.getMotif()) + "-" +
                credit.getStatut() + "-" +
                credit.getDateCreation().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "-" +
                dateTraitementStr + "-" +
                (credit.getCommentaire() == null ? "null" : credit.getCommentaire()) +
                System.lineSeparator();
    }

    private long newMaxId() {
        return findAll().stream()
                .mapToLong(DemandeCredit::getId)
                .max()
                .orElse(0) + 1;
    }

    @Override
    public List<DemandeCredit> findAll() {
        try {
            return Files.readAllLines(path)
                    .stream()
                    .skip(1) // Skip header
                    .map(this::map)
                    .filter(credit -> credit != null) // Filtrer les lignes invalides
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public DemandeCredit findById(Long id) {
        return findAll().stream()
                .filter(credit -> credit.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<DemandeCredit> findByUserId(Long userId) {
        return findAll().stream()
                .filter(credit -> credit.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<DemandeCredit> findByStatut(String statut) {
        return findAll().stream()
                .filter(credit -> credit.getStatut().equals(statut))
                .collect(Collectors.toList());
    }

    @Override
    public DemandeCredit save(DemandeCredit credit) {
        try {
            credit.setId(newMaxId());
            credit.setDateCreation(LocalDate.now());
            credit.setStatut("EN_ATTENTE");
            Files.writeString(path, mapToFileLine(credit), StandardOpenOption.APPEND);
            return credit;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void update(DemandeCredit credit) {
        List<DemandeCredit> updatedList = findAll().stream()
                .map(c -> c.getId().equals(credit.getId()) ? credit : c)
                .collect(Collectors.toList());
        rewriteFile(updatedList);
    }

    @Override
    public void delete(DemandeCredit credit) {
        deleteById(credit.getId());
    }

    @Override
    public void deleteById(Long id) {
        List<DemandeCredit> updatedList = findAll().stream()
                .filter(c -> !c.getId().equals(id))
                .collect(Collectors.toList());
        rewriteFile(updatedList);
    }

    @Override
    public void updateStatut(Long id, String statut, String commentaire) {
        DemandeCredit credit = findById(id);
        if (credit != null) {
            credit.setStatut(statut);
            credit.setCommentaire(commentaire);
            credit.setDateTraitement(LocalDate.now());
            update(credit);
        }
    }

    private void rewriteFile(List<DemandeCredit> credits) {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("ID-UserId-Montant-Motif-Statut-DateCreation-DateTraitement-Commentaire");
            for (DemandeCredit credit : credits) {
                lines.add(mapToFileLine(credit).trim());
            }
            Files.write(path, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}