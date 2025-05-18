-- Create tables if they don't exist
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Users]') AND type in (N'U'))
BEGIN
CREATE TABLE Users (
                       id INT IDENTITY(1,1) PRIMARY KEY,
                       firstName NVARCHAR(100),
                       lastName NVARCHAR(100),
                       username NVARCHAR(50) NOT NULL,
                       password NVARCHAR(100) NOT NULL,
                       role NVARCHAR(20) NOT NULL,
                       creationDate DATE NOT NULL
);
END

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Comptes]') AND type in (N'U'))
BEGIN
CREATE TABLE Comptes (
                         id INT IDENTITY(1,1) PRIMARY KEY,
                         userId INT NOT NULL,
                         solde DECIMAL(18, 2) NOT NULL,
                         devise NVARCHAR(10) NOT NULL,
                         CONSTRAINT FK_Comptes_Users FOREIGN KEY (userId) REFERENCES Users(id)
);
END

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[DemandesCredit]') AND type in (N'U'))
BEGIN
CREATE TABLE DemandesCredit (
                                id INT IDENTITY(1,1) PRIMARY KEY,
                                userId INT NOT NULL,
                                montant DECIMAL(18, 2) NOT NULL,
                                motif NVARCHAR(500),
                                statut NVARCHAR(20) NOT NULL,
                                dateCreation DATE NOT NULL,
                                dateTraitement DATE,
                                commentaire NVARCHAR(500),
                                CONSTRAINT FK_DemandesCredit_Users FOREIGN KEY (userId) REFERENCES Users(id)
);
END

-- Insert default admin user if not exists
IF NOT EXISTS (SELECT * FROM Users WHERE username = 'admin')
BEGIN
INSERT INTO Users (firstName, lastName, username, password, role, creationDate)
VALUES ('John', 'Doe', 'admin', '1234', 'ADMIN', GETDATE());
END

-- Insert default user if not exists
IF NOT EXISTS (SELECT * FROM Users WHERE username = 'user')
BEGIN
INSERT INTO Users (firstName, lastName, username, password, role, creationDate)
VALUES ('Dr', 'Wayne', 'user', '5678', 'USER', GETDATE());
END

-- Insert accounts for users if not exists
IF EXISTS (SELECT * FROM Users WHERE username = 'admin' AND NOT EXISTS (SELECT * FROM Comptes WHERE userId = (SELECT id FROM Users WHERE username = 'admin')))
BEGIN
INSERT INTO Comptes (userId, solde, devise)
VALUES ((SELECT id FROM Users WHERE username = 'admin'), 1200.0, 'EUR');
END

IF EXISTS (SELECT * FROM Users WHERE username = 'user' AND NOT EXISTS (SELECT * FROM Comptes WHERE userId = (SELECT id FROM Users WHERE username = 'user')))
BEGIN
INSERT INTO Comptes (userId, solde, devise)
VALUES ((SELECT id FROM Users WHERE username = 'user'), 1480.0, 'EUR');
END