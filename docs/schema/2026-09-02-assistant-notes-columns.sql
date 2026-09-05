ALTER TABLE assistant_notes
    ADD COLUMN operation      VARCHAR(20)  NOT NULL DEFAULT 'INBOUND' AFTER organization_member_id,
    ADD COLUMN finding_type   VARCHAR(30)  NULL     AFTER severity,
    ADD COLUMN subject        VARCHAR(300) NOT NULL DEFAULT '' AFTER finding_type,
    ADD COLUMN subject_detail VARCHAR(300) NULL     AFTER subject,
    ADD COLUMN target_storage_id BIGINT    NULL     AFTER target_type;
