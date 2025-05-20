-- MySQL initialization script for Bankati application

-- Create tables if they don't exist
CREATE TABLE IF NOT EXISTS Users (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     firstName VARCHAR(100),
    lastName VARCHAR(100),
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    creationDate DATE NOT NULL
    );

CREATE TABLE IF NOT EXISTS Comptes (
                                       id INT AUTO_INCREMENT PRIMARY KEY,
                                       userId INT NOT NULL,
                                       solde DECIMAL(18, 2) NOT NULL,
    devise VARCHAR(10) NOT NULL,
    CONSTRAINT FK_Comptes_Users FOREIGN KEY (userId) REFERENCES Users(id)
    );

CREATE TABLE IF NOT EXISTS DemandesCredit (
                                              id INT AUTO_INCREMENT PRIMARY KEY,
                                              userId INT NOT NULL,
                                              montant DECIMAL(18, 2) NOT NULL,
    motif VARCHAR(500),
    statut VARCHAR(20) NOT NULL,
    dateCreation DATE NOT NULL,
    dateTraitement DATE,
    commentaire VARCHAR(500),
    CONSTRAINT FK_DemandesCredit_Users FOREIGN KEY (userId) REFERENCES Users(id)
    );

-- Insert default admin user if not exists
INSERT INTO Users (firstName, lastName, username, password, role, creationDate)
SELECT 'John', 'Doe', 'admin', '1234', 'ADMIN', CURDATE()
    WHERE NOT EXISTS (SELECT * FROM Users WHERE username = 'admin');

-- Insert default user if not exists
INSERT INTO Users (firstName, lastName, username, password, role, creationDate)
SELECT 'Dr', 'Wayne', 'user', '5678', 'USER', CURDATE()
    WHERE NOT EXISTS (SELECT * FROM Users WHERE username = 'user');

-- Insert accounts for users if not exists
INSERT INTO Comptes (userId, solde, devise)
SELECT (SELECT id FROM Users WHERE username = 'admin'), 1200.0, 'EUR'
    WHERE NOT EXISTS (SELECT * FROM Comptes WHERE userId = (SELECT id FROM Users WHERE username = 'admin'));

INSERT INTO Comptes (userId, solde, devise)
SELECT (SELECT id FROM Users WHERE username = 'user'), 1480.0, 'EUR'
    WHERE NOT EXISTS (SELECT * FROM Comptes WHERE userId = (SELECT id FROM Users WHERE username = 'user'));