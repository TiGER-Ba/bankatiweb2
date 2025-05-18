package ma.bankati.dao.dataDao.fileDb;

import ma.bankati.dao.dataDao.IDao;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataDao implements IDao {

    public DataDao() {
    }

    @Override
    public double fetchData() {
        try {
            URL ressource = getClass().getClassLoader().getResource("FileBase/compte.txt");
            if (ressource == null) {
                System.err.println("FileBase/compte.txt not found");
                return 0.0;
            }

            Path chemin = Paths.get(ressource.toURI());

            var lines = Files.readAllLines(chemin);
            if (lines.size() > 1) {
                String soldeLine = lines.get(1).trim();
                System.out.println("Lecture du solde: " + soldeLine);
                return Double.parseDouble(soldeLine);
            }

            return 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}