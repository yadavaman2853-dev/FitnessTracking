CREATE DATABASE IF NOT EXISTS fitness_tracking;
USE fitness_tracking;

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN','USER') NOT NULL DEFAULT 'USER',
    goal VARCHAR(150) DEFAULT 'General Fitness',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workouts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    workout_type VARCHAR(100) NOT NULL,
    duration INT NOT NULL,
    intensity VARCHAR(30) NOT NULL,
    calories INT DEFAULT 0,
    workout_date DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workout_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS progress (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    weight DECIMAL(6,2),
    steps INT DEFAULT 0,
    workout_minutes INT DEFAULT 0,
    recorded_at DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_progress_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS challenges (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    target INT NOT NULL,
    unit VARCHAR(50) NOT NULL,
    status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS challenge_participants (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    challenge_id INT NOT NULL,
    progress INT NOT NULL DEFAULT 0,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_participation (user_id, challenge_id),
    CONSTRAINT fk_participant_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_participant_challenge
        FOREIGN KEY (challenge_id) REFERENCES challenges(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS system_settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS activity_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NULL,
    action VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_activity_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE SET NULL
);

INSERT INTO users (name, email, password_hash, role, goal)
SELECT 'Administrator', 'admin@fitness.com',
       SHA2('admin123', 256), 'ADMIN', 'System Management'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@fitness.com'
);

INSERT INTO challenges (title, description, target, unit, status)
SELECT '30 Day Workout Challenge',
       'Complete 30 workouts and build a consistent fitness habit.',
       30, 'workouts', 'APPROVED'
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = '30 Day Workout Challenge'
);

INSERT INTO challenges (title, description, target, unit, status)
SELECT '10K Steps Challenge',
       'Reach 10,000 steps as part of your daily fitness routine.',
       10000, 'steps', 'APPROVED'
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = '10K Steps Challenge'
);

INSERT INTO challenges (title, description, target, unit, status)
SELECT '300 Minute Fitness Challenge',
       'Complete 300 minutes of exercise.',
       300, 'minutes', 'APPROVED'
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = '300 Minute Fitness Challenge'
);

INSERT INTO system_settings (setting_key, setting_value)
VALUES
('application_name', 'Online Fitness Tracking Application'),
('maintenance_mode', 'OFF'),
('max_workout_minutes', '600')
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value);

CREATE INDEX idx_workouts_user_date ON workouts(user_id, workout_date);
CREATE INDEX idx_progress_user_date ON progress(user_id, recorded_at);
CREATE INDEX idx_activity_date ON activity_log(created_at);
