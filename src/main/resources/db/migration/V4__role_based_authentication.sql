CREATE TABLE app_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    username VARCHAR(64) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    role ENUM('RESIDENT', 'LOCAL_STAFF', 'SUPERVISOR', 'CITY_MANAGER') NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_app_user_username UNIQUE (username),
    CONSTRAINT uq_app_user_email UNIQUE (email),
    INDEX idx_app_user_role_active (role, active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE complaint
    ADD COLUMN resident_id BIGINT NULL,
    ADD COLUMN assigned_user_id BIGINT NULL,
    ADD CONSTRAINT fk_complaint_resident FOREIGN KEY (resident_id) REFERENCES app_user(id),
    ADD CONSTRAINT fk_complaint_assigned_user FOREIGN KEY (assigned_user_id) REFERENCES app_user(id),
    ADD INDEX idx_complaint_resident (resident_id, created_at),
    ADD INDEX idx_complaint_handler (current_handler, assigned_user_id, status);

ALTER TABLE complaint_escalation
    ADD COLUMN actor_user_id BIGINT NULL,
    ADD COLUMN actor_role ENUM('RESIDENT', 'LOCAL_STAFF', 'SUPERVISOR', 'CITY_MANAGER') NULL,
    ADD CONSTRAINT fk_escalation_actor FOREIGN KEY (actor_user_id) REFERENCES app_user(id),
    ADD INDEX idx_escalation_actor (actor_user_id, occurred_at);
