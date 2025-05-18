package ma.bankati.dao.compteDao.fileDb;

import ma.bankati.dao.compteDao.ICompteDao;
import ma.bankati.model.compte.Compte;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CompteDao implements ICompteDao {

    private Path path;

    public CompteDao() {
        try {
            this.path = Paths.get(
                    getClass().getClassLoader()
                            .getResource("FileBase/comptes.txt")
                            .toURI()
            );
        } catch (Exception e) {
            System.err.println("FileBase/comptes.txt not found");
        }
    }

    private Compte map(String fileLine) {
        String[] fields = fileLine.split("-");
        Long id = Long.parseLong(fields[0]);
        Long userId = Long.parseLong(fields[1]);
        Double solde = Double.parseDouble(fields[2]);
        String devise = fields[3];

        return Compte.builder()
                .id(id)
                .userId(userId)
                .solde(solde)
                .devise(devise)
                .build();
    }

    private String mapToFileLine(Compte compte) {
        return compte.getId() + "-" +
                compte.getUserId() + "-" +
                compte.getSolde() + "-" +
                compte.getDevise() +
                System.lineSeparator();
    }

    @Override
    public Compte findByUserId(Long userId) {
        return findAll().stream()
                .filter(compte -> compte.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Compte> findAll() {
        try {
            return Files.readAllLines(path)
                    .stream()
                    .skip(1) // Skip header
                    .map(this::map)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Compte save(Compte compte) {
        // Implémenter la logique de sauvegarde
        return compte;
    }

    @Override
    public void update(Compte compte) {
        List<Compte> updatedList = findAll().stream()
                .map(c -> c.getId().equals(compte.getId()) ? compte : c)
                .collect(Collectors.toList());
        rewriteFile(updatedList);
    }

    @Override
    public void updateSolde(Long userId, Double nouveauSolde) {
        Compte compte = findByUserId(userId);
        if (compte != null) {
            compte.setSolde(nouveauSolde);
            update(compte);
        }
    }

    private void rewriteFile(List<Compte> comptes) {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("ID-UserId-Solde-Devise");
            for (Compte compte : comptes) {
                lines.add(mapToFileLine(compte).trim());
            }
            Files.write(path, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}