ALTER TABLE smart_zone
    ADD COLUMN parent_key BIGINT GENERATED ALWAYS AS (COALESCE(parent_id, 0)) STORED,
    ADD CONSTRAINT uq_zone_sibling_name_root_safe UNIQUE (parent_key, name);
