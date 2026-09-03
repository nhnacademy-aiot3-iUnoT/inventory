ALTER TABLE assistant_notes
    ADD COLUMN target_storage_id BIGINT NULL AFTER target_type;
