CREATE TABLE IF NOT EXISTS security_stream_run (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meter_id VARCHAR(80) NOT NULL,
    decorator_chain VARCHAR(255) NOT NULL,
    original_payload LONGTEXT NOT NULL,
    processed_payload LONGTEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS smart_zone (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT NULL,
    parent_key BIGINT GENERATED ALWAYS AS (COALESCE(parent_id, 0)) STORED,
    zone_type ENUM('CITY', 'DISTRICT', 'STREET', 'BUILDING') NOT NULL,
    name VARCHAR(120) NOT NULL,
    power_on BOOLEAN NOT NULL DEFAULT TRUE,
    usage_kwh DECIMAL(12, 3) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_zone_parent FOREIGN KEY (parent_id) REFERENCES smart_zone(id),
    CONSTRAINT uq_zone_sibling_name UNIQUE (parent_id, name),
    CONSTRAINT uq_zone_sibling_name_root_safe UNIQUE (parent_key, name),
    INDEX idx_zone_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS zone_operation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    root_zone_id BIGINT NOT NULL,
    operation_type ENUM('SHUTDOWN_POWER', 'REPORT_USAGE', 'RESTORE_POWER') NOT NULL,
    status VARCHAR(24) NOT NULL,
    result_summary TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_zone_operation_root FOREIGN KEY (root_zone_id) REFERENCES smart_zone(id),
    INDEX idx_zone_operation_date (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS app_user (
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

CREATE TABLE IF NOT EXISTS complaint (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reference_no VARCHAR(32) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    severity ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL,
    status ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED') NOT NULL DEFAULT 'OPEN',
    current_handler ENUM('LOCAL_STAFF', 'SUPERVISOR', 'CITY_MANAGER') NOT NULL,
    resident_id BIGINT NULL,
    assigned_user_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_complaint_resident FOREIGN KEY (resident_id) REFERENCES app_user(id),
    CONSTRAINT fk_complaint_assigned_user FOREIGN KEY (assigned_user_id) REFERENCES app_user(id),
    INDEX idx_complaint_status (status, severity),
    INDEX idx_complaint_resident (resident_id, created_at),
    INDEX idx_complaint_handler (current_handler, assigned_user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS complaint_escalation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    complaint_id BIGINT NOT NULL,
    handler_level ENUM('LOCAL_STAFF', 'SUPERVISOR', 'CITY_MANAGER') NOT NULL,
    actor_user_id BIGINT NULL,
    actor_role ENUM('RESIDENT', 'LOCAL_STAFF', 'SUPERVISOR', 'CITY_MANAGER') NULL,
    action VARCHAR(24) NOT NULL,
    note VARCHAR(500) NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_escalation_complaint FOREIGN KEY (complaint_id) REFERENCES complaint(id) ON DELETE CASCADE,
    CONSTRAINT fk_escalation_actor FOREIGN KEY (actor_user_id) REFERENCES app_user(id),
    INDEX idx_escalation_complaint_date (complaint_id, occurred_at),
    INDEX idx_escalation_actor (actor_user_id, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS streetlight_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wattage SMALLINT UNSIGNED NOT NULL,
    brightness_mode VARCHAR(32) NOT NULL,
    color_temperature SMALLINT UNSIGNED NOT NULL,
    sensor_profile VARCHAR(48) NOT NULL,
    CONSTRAINT uq_streetlight_config UNIQUE (wattage, brightness_mode, color_temperature, sensor_profile)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS streetlight (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    light_code VARCHAR(64) NOT NULL UNIQUE,
    config_id BIGINT NOT NULL,
    zone_id BIGINT NULL,
    location VARCHAR(180) NOT NULL,
    latitude DECIMAL(10, 7) NULL,
    longitude DECIMAL(10, 7) NULL,
    status ENUM('ON', 'OFF', 'MAINTENANCE') NOT NULL DEFAULT 'ON',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_streetlight_config FOREIGN KEY (config_id) REFERENCES streetlight_config(id),
    CONSTRAINT fk_streetlight_zone FOREIGN KEY (zone_id) REFERENCES smart_zone(id),
    INDEX idx_streetlight_config (config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS city_policy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL UNIQUE,
    expression_text VARCHAR(500) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS policy_evaluation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    policy_id BIGINT NULL,
    input_values JSON NOT NULL,
    result BOOLEAN NULL,
    validation_error VARCHAR(500) NULL,
    evaluated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_policy_evaluation FOREIGN KEY (policy_id) REFERENCES city_policy(id) ON DELETE SET NULL,
    INDEX idx_policy_evaluation_date (evaluated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
