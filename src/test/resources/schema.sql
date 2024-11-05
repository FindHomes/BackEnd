-- User 테이블 생성
CREATE TABLE user_tbl (
    user_id VARCHAR(255) PRIMARY KEY,
    user_nickname VARCHAR(50),
    login_api VARCHAR(255),
    kakao_id VARCHAR(255) UNIQUE,
    status VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME
);

-- Search Log 테이블 생성
CREATE TABLE search_logs_tbl (
     search_log_id INT AUTO_INCREMENT PRIMARY KEY,
     user_id VARCHAR(255) NOT NULL,
     search_condition JSON,
     status VARCHAR(20) DEFAULT 'ACTIVE',
     created_at DATETIME,
     FOREIGN KEY (user_id) REFERENCES user_tbl(user_id)
);