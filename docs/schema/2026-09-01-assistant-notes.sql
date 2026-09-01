CREATE TABLE assistant_notes (
    assistant_note_id      BIGINT        NOT NULL AUTO_INCREMENT,
    organization_member_id BIGINT        NOT NULL,
    severity               VARCHAR(20)   NOT NULL,
    message                VARCHAR(1000) NOT NULL,
    target_type            VARCHAR(20)   NULL,
    target_id              BIGINT        NULL,
    is_read                BIT(1)        NOT NULL,
    created_at             DATETIME(6)   NOT NULL,
    PRIMARY KEY (assistant_note_id),
    KEY idx_assistant_notes_member_read (organization_member_id, is_read),
    CONSTRAINT fk_assistant_notes_member
        FOREIGN KEY (organization_member_id)
        REFERENCES organization_members (organization_member_id)
        ON DELETE CASCADE
)
