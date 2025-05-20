package ma.bankati.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebListener;
import ma.bankati.dao.compteDao.ICompteDao;
import ma.bankati.dao.db.DatabaseConnection;
import ma.bankati.dao.userDao.IUserDao;
import ma.bankati.dao.creditDao.IDemandeCreditDao;
import ma.bankati.service.authentification.IAuthentificationService;
import ma.bankati.service.moneyServices.IMoneyService;
import ma.bankati.service.creditService.ICreditService;
import ma.bankati.service.compteService.ICompteService;

import java.util.Enumeration;
import java.util.Properties;

@WebListener
public class WebContext implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent ev) {
        var application = ev.getServletContext();
        application.setAttribute("AppName", "Bankati");

        // Load application properties
        var configFile = getClass().getClassLoader().getResourceAsStream("application.properties");
        if (configFile != null) {
            Properties properties = new Properties();
            try {
                properties.load(configFile);
                System.out.println("Properties file loaded successfully");

                // Log database connection parameters for debugging
                System.out.println("Database connection parameters:");
                System.out.println("URL: " + properties.getProperty("datasource.url"));
                System.out.println("Username: " + properties.getProperty("datasource.username"));
                // Don't log password for security reasons
                System.out.println("Driver: " + properties.getProperty("datasource.driver"));

                // Load JDBC driver
                String driverName = properties.getProperty("datasource.driver");
                try {
                    Class.forName(driverName);
                    System.out.println("JDBC driver loaded successfully: " + driverName);

                    // Initialize database connection
                    DatabaseConnection.initialize(properties);

                    // Test connection
                    try (var connection = DatabaseConnection.getConnection()) {
                        System.out.println("Database connection test successful");

                        // Test a simple query to ensure complete connectivity
                        var stmt = connection.createStatement();
                        var rs = stmt.executeQuery("SELECT VERSION()");
                        if (rs.next()) {
                            System.out.println("MySQL Version: " + rs.getString(1));
                        }

                        // Load application context (services, DAOs, etc.)
                        loadApplicationContext(application, properties);

                    } catch (Exception e) {
                        System.err.println("Database connection test failed: " + e.getMessage());
                        e.printStackTrace();

                        // Stop application loading completely if DB connection fails
                        // This ensures we're using only the database and not falling back to files
                        throw new ServletException("Database connection failed. Application cannot start without a database connection.", e);
                    }

                } catch (ClassNotFoundException e) {
                    System.err.println("Failed to load JDBC driver: " + e.getMessage());
                    e.printStackTrace();
                    throw new ServletException("Failed to load MySQL JDBC driver. Please check your classpath.", e);
                }
            } catch (Exception e) {
                System.err.println("Failed to load properties file: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Critical error: Failed to load application.properties", e);
            }
        } else {
            System.err.println("Properties file not found!");
            throw new RuntimeException("Critical error: application.properties file not found");
        }

        System.out.println("Application Started and context initialized");
    }

    static void loadApplicationContext(ServletContext application, Properties properties) {
        try {
            // Get DAO implementation classes from properties
            String userDaoClassName = properties.getProperty("userDao");
            String creditDaoClassName = properties.getProperty("creditDao");
            String compteDaoClassName = properties.getProperty("compteDao");
            String dataDaoClassName = "ma.bankati.dao.dataDao.memoryDb.DataDao"; // Changed to memoryDb to avoid file dependency
            String moneyServClassName = "ma.bankati.service.moneyServices.MultiCurrencyService";

            System.out.println("Loading DAOs and services with classes:");
            System.out.println("- userDao: " + userDaoClassName);
            System.out.println("- creditDao: " + creditDaoClassName);
            System.out.println("- compteDao: " + compteDaoClassName);
            System.out.println("- dataDao: " + dataDaoClassName);
            System.out.println("- moneyService: " + moneyServClassName);

            try {
                // Load DataDao
                System.out.println("Loading DataDao...");
                Class<?> cDataDao = Class.forName(dataDaoClassName);
                var dataDao = cDataDao.getDeclaredConstructor().newInstance();
                application.setAttribute("dataDao", dataDao);
                System.out.println("DataDao loaded successfully");

                // Load MoneyService
                System.out.println("Loading MoneyService...");
                Class<?> cMoneyService = Class.forName(moneyServClassName);
                var moneyService = cMoneyService.getDeclaredConstructor(Class.forName("ma.bankati.dao.dataDao.IDao"))
                        .newInstance(dataDao);
                application.setAttribute("moneyService", moneyService);
                System.out.println("MoneyService loaded successfully");

                // Load UserDao
                System.out.println("Loading UserDao...");
                Class<?> cUserDao = Class.forName(userDaoClassName);
                var userDao = cUserDao.getDeclaredConstructor().newInstance();
                application.setAttribute("userDao", userDao);
                System.out.println("UserDao loaded successfully");

                // Load AuthService
                System.out.println("Loading AuthService...");
                Class<?> cAuthService = Class.forName("ma.bankati.service.authentification.AuthentificationServiceImpl");
                var authService = cAuthService.getDeclaredConstructor(Class.forName("ma.bankati.dao.userDao.IUserDao"))
                        .newInstance(userDao);
                application.setAttribute("authService", authService);
                System.out.println("AuthService loaded successfully");

                // Load CreditDao
                System.out.println("Loading CreditDao...");
                Class<?> cCreditDao = Class.forName(creditDaoClassName);
                var creditDao = cCreditDao.getDeclaredConstructor().newInstance();
                application.setAttribute("creditDao", creditDao);
                System.out.println("CreditDao loaded successfully");

                // Load CompteDao
                System.out.println("Loading CompteDao...");
                Class<?> cCompteDao = Class.forName(compteDaoClassName);
                var compteDao = cCompteDao.getDeclaredConstructor().newInstance();
                application.setAttribute("compteDao", compteDao);
                System.out.println("CompteDao loaded successfully");

                // Load CompteService
                System.out.println("Loading CompteService...");
                Class<?> cCompteService = Class.forName("ma.bankati.service.compteService.CompteServiceImpl");
                var compteService = cCompteService.getDeclaredConstructor(Class.forName("ma.bankati.dao.compteDao.ICompteDao"))
                        .newInstance(compteDao);
                application.setAttribute("compteService", compteService);
                System.out.println("CompteService loaded successfully");

                // Load CreditService
                System.out.println("Loading CreditService...");
                Class<?> cCreditService = Class.forName("ma.bankati.service.creditService.CreditServiceImpl");
                var creditService = cCreditService.getDeclaredConstructor(
                                Class.forName("ma.bankati.dao.creditDao.IDemandeCreditDao"),
                                Class.forName("ma.bankati.service.compteService.ICompteService"))
                        .newInstance(creditDao, compteService);
                application.setAttribute("creditService", creditService);
                System.out.println("CreditService loaded successfully");

                System.out.println("Application context loaded successfully");
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to load required class: " + e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                System.err.println("Constructor not found: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to find constructor: " + e.getMessage(), e);
            } catch (Exception e) {
                System.err.println("Error instantiating class: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to instantiate class: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            System.err.println("Error initializing application context: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize application context", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent ev) {
        var application = ev.getServletContext();
        Enumeration<String> attributeNames = application.getAttributeNames();

        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            application.removeAttribute(name);
        }

        System.out.println("Application Stopped");
    }
}