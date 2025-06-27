CREATE DATABASE login;
USE login;
CREATE TABLE user (
                      user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      email VARCHAR(255) UNIQUE NOT NULL,
                      password VARCHAR(255),
                      nickname VARCHAR(100) NOT NULL,
                      login_type ENUM('LOCAL', 'KAKAO', 'NAVER', 'GOOGLE', 'APPLE') DEFAULT 'LOCAL',
                      is_locked BOOLEAN DEFAULT FALSE,
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


CREATE TABLE login_fail (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            user_id BIGINT,
                            fail_count INT DEFAULT 0,
                            last_fail_at DATETIME,
                            FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);


CREATE TABLE remember_token (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                user_id BIGINT NOT NULL,
                                token VARCHAR(255) NOT NULL,
                                expired_at DATETIME NOT NULL,
                                user_agent VARCHAR(255),
                                ip_address VARCHAR(45),
                                FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);


CREATE TABLE email_token (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             user_id BIGINT NOT NULL,
                             token VARCHAR(255) NOT NULL,
                             type ENUM('VERIFY', 'RESET') NOT NULL,
                             expired_at DATETIME NOT NULL,
                             is_used BOOLEAN DEFAULT FALSE,
                             FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);
