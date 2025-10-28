-- College Fee And Student Management System - Database Schema
-- Use this as reference to set up your local database

CREATE DATABASE IF NOT EXISTS college_management;
USE college_management;

-- Students table
CREATE TABLE students (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          roll_no VARCHAR(50) UNIQUE NOT NULL,
                          first_name VARCHAR(100) NOT NULL,
                          last_name VARCHAR(100) NOT NULL,
                          email VARCHAR(255) UNIQUE NOT NULL,
                          phone VARCHAR(20),
                          program VARCHAR(100),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Student fees table
CREATE TABLE student_fees (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              student_id BIGINT NOT NULL,
                              admission_fee DECIMAL(10,2) DEFAULT 0.00,
                              tuition_fee DECIMAL(10,2) DEFAULT 0.00,
                              exam_fee DECIMAL(10,2) DEFAULT 0.00,
                              university_charge DECIMAL(10,2) DEFAULT 0.00,
                              eca_charge DECIMAL(10,2) DEFAULT 0.00,
                              scholarship_amount DECIMAL(10,2) DEFAULT 0.00,
                              discount_amount DECIMAL(10,2) DEFAULT 0.00,
                              total_fee DECIMAL(10,2) DEFAULT 0.00,
                              net_fee DECIMAL(10,2) DEFAULT 0.00,
                              course_duration_years INT DEFAULT 4,
                              total_installments INT DEFAULT 8,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

-- Fee installments table
CREATE TABLE fee_installments (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  student_fee_id BIGINT NOT NULL,
                                  installment_number INT NOT NULL,
                                  amount DECIMAL(10,2) NOT NULL,
                                  paid_amount DECIMAL(10,2) DEFAULT 0.00,
                                  pending_amount DECIMAL(10,2) NOT NULL,
                                  paid BOOLEAN DEFAULT FALSE,
                                  description TEXT,
                                  is_misc_expense BOOLEAN DEFAULT FALSE,
                                  FOREIGN KEY (student_fee_id) REFERENCES student_fees(id) ON DELETE CASCADE
);

-- Miscellaneous expenses table
CREATE TABLE misc_expenses (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               student_fee_id BIGINT NOT NULL,
                               amount DECIMAL(10,2) NOT NULL,
                               description TEXT NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (student_fee_id) REFERENCES student_fees(id) ON DELETE CASCADE
);

-- Users table for authentication
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User roles table
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role VARCHAR(50) NOT NULL,
                            PRIMARY KEY (user_id, role),
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Sample data (optional)
INSERT INTO students (roll_no, first_name, last_name, email, phone, program) VALUES
                                                                                 ('2024001', 'Kavya', 'shrestha', 'kavya@example.com', '1234567890', 'BCA'),
                                                                                 ('2024002', 'Supriya', 'Bajracharya', 'supriya@example.com', '1234567891', 'BIT');

INSERT INTO users (username, password) VALUES
                                           ('admin', '$2a$10$exampleHashedPassword123456789012'),
                                           ('user', '$2a$10$exampleHashedPassword123456789013');

INSERT INTO user_roles (user_id, role) VALUES
                                           (1, 'ROLE_ADMIN'),
                                           (2, 'ROLE_USER');