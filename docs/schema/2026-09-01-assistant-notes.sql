CREATE TABLE assistant_notes (
    assistant_note_id      BIGINT        NOT NULL AUTO_INCREMENT,
    organization_member_id BIGINT        NOT NULL,
    operation              VARCHAR(20)   NOT NULL COMMENT 'INBOUND / OUTBOUND',
    severity               VARCHAR(20)   NOT NULL COMMENT 'INFO / WARN / CRITICAL',
    finding_type           VARCHAR(30)   NULL,
    subject                VARCHAR(300)  NOT NULL COMMENT '의약품명 / 포장단위',
    subject_detail         VARCHAR(300)  NULL COMMENT '구역, 수량 등 부가 정보',
    message                VARCHAR(1000) NOT NULL COMMENT '안내문',
    target_type            VARCHAR(20)   NULL COMMENT 'ZONE / STORAGE / PACK_UNIT / REPORT',
    target_storage_id      BIGINT        NULL,
    target_id              BIGINT        NULL,
    is_read                BIT(1)        NOT NULL,
    created_at             DATETIME(6)   NOT NULL,
    PRIMARY KEY (assistant_note_id),
    KEY idx_assistant_notes_member_read (organization_member_id, is_read),
    KEY idx_assistant_notes_member_created (organization_member_id, created_at),
    CONSTRAINT fk_assistant_notes_member
        FOREIGN KEY (organization_member_id)
            REFERENCES organization_members (organization_member_id)
            ON DELETE CASCADE
);
