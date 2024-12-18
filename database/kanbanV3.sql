
CREATE SCHEMA IF NOT EXISTS kanban_DB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE kanban_DB;

-- MySQL dump 10.13  Distrib 8.0.34, for macos13 (arm64)
--
-- Host: 127.0.0.1    Database: kanban
-- ------------------------------------------------------
-- Server version	8.2.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET SQL_NOTES=0 */;

-- Drop tables if they exist
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS statuses;
DROP TABLE IF EXISTS collabs; -- Junction table
DROP TABLE IF EXISTS boards;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS attachments;

-- Table structure for 'users'
CREATE TABLE users (
  oid CHAR(36) NOT NULL, -- UUID for user identification
  name VARCHAR(100) NOT NULL, -- Full name with a limit of 100 characters
  username VARCHAR(50) NOT NULL, -- Username with a limit of 50 characters
  email VARCHAR(50) NOT NULL, -- Email with a limit of 50 characters
  createdOn TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updatedOn TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (oid),
  UNIQUE (email),
  UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for 'boards'
CREATE TABLE boards (
  boardId VARCHAR(10) NOT NULL,
  boardName VARCHAR(120) NOT NULL,
	ownerOid VARCHAR(36),
  visibility ENUM('PRIVATE','PUBLIC') NOT NULL DEFAULT 'PRIVATE',
  createdOn TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updatedOn TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (boardId),
  CONSTRAINT fk_boards_users FOREIGN KEY (ownerOid) REFERENCES users(oid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE collabs (

  boardId VARCHAR(10) NOT NULL,
  userOid CHAR(36) NOT NULL,
  email VARCHAR(50) NOT NULL,
	name VARCHAR(100) NOT NULL,
  access_right ENUM('READ', 'WRITE') NOT NULL DEFAULT 'READ', 
  invitation ENUM('PENDING', 'ACCEPTED', 'DECLINED') DEFAULT 'PENDING',
  added_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  
  PRIMARY KEY (boardId, userOid),
  CONSTRAINT fk_collaborators_board FOREIGN KEY (boardId) REFERENCES boards(boardId) ,
  CONSTRAINT fk_collaborators_user FOREIGN KEY (userOid) REFERENCES users(oid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for 'statuses'
CREATE TABLE statuses (
  statusId INT NOT NULL AUTO_INCREMENT,
  statusName VARCHAR(50) NOT NULL,
  statusDescription VARCHAR(200),
  boardId VARCHAR(10), 
  PRIMARY KEY (statusId),
  CONSTRAINT fk_statuses_boards FOREIGN KEY (boardId) REFERENCES boards(boardId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for 'tasks'
CREATE TABLE tasks (
  taskId INT NOT NULL AUTO_INCREMENT,
  title VARCHAR(100) DEFAULT NULL,
  description VARCHAR(500) DEFAULT NULL,
  assignees VARCHAR(30) DEFAULT NULL,
  createdOn TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updatedOn TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  statusId INT NOT NULL,
  boardId VARCHAR(10),
  PRIMARY KEY (taskId),
  UNIQUE KEY id_UNIQUE (taskId),
  CONSTRAINT fk_tasks_statuses FOREIGN KEY (statusId) REFERENCES statuses(statusId),
  CONSTRAINT fk_tasks_boards FOREIGN KEY (boardId) REFERENCES boards(boardId)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE attachments (
  attachmentId INT NOT NULL AUTO_INCREMENT,
  taskId INT NOT NULL,
  file VARCHAR(255) NOT NULL, 
  uploaded_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (attachmentId),
  CONSTRAINT fk_attachments_tasks FOREIGN KEY (taskId) REFERENCES tasks(taskId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- Insert data into 'boards' table
INSERT INTO boards (boardId, boardName)
VALUES
    ('nanoid1', 'Board 1'),
    ('nanoid2', 'Board 2'),
    ('nanoid3', 'Board 3');

-- Insert data into 'collaborators' table to represent board collaboration, with access rights
INSERT INTO collabs (boardId, userOid, email, access_right)
VALUES
    ('nanoid1', 'owner-uuid-1', 'owner1@example.com', 'write'),
    ('nanoid2', 'owner-uuid-2', 'owner2@example.com', 'write'),
    ('nanoid3', 'owner-uuid-3', 'owner3@example.com', 'write'),
    ('nanoid1', 'collaborator-uuid-1', 'collab1@example.com', 'read'),
    ('nanoid2', 'collaborator-uuid-2', 'collab2@example.com', 'read');

-- Insert data into 'statuses' table
INSERT INTO statuses (statusName, statusDescription, boardId)
VALUES
    ('No Status', 'The default status', 'nanoid1'),
    ('To Do', 'The task is included in the project', 'nanoid1'),
    ('In Progress', 'The task is being worked on', 'nanoid2'),
    ('Reviewing', 'The task is being reviewed', 'nanoid2'),
    ('Testing', 'The task is being tested', 'nanoid3'),
    ('Done', 'Finished', 'nanoid3'),
    ('Waiting', 'The task is waiting for a resource', 'nanoid3');

-- Insert data into 'tasks' table
INSERT INTO tasks (title, description, assignees, statusId, boardId)
VALUES
    ('NS01', '', '', 1, 'nanoid1'),
    ('TD01', '', '', 2, 'nanoid1'),
    ('IP01', '', '', 3, 'nanoid2'),
    ('TD02', '', '', 2, 'nanoid2'),
    ('DO01', '', '', 7, 'nanoid3'),
    ('IP02', '', '', 3, 'nanoid3');
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;