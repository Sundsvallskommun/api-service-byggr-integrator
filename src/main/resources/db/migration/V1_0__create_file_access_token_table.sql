CREATE TABLE file_access_token (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    file_id VARCHAR(255) NOT NULL,
    municipality_id VARCHAR(4) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created DATETIME(6) NOT NULL,
    INDEX idx_file_access_token_expires_at (expires_at),
    INDEX idx_file_access_token_file_id_municipality_id (file_id, municipality_id)
) ENGINE=InnoDB;
