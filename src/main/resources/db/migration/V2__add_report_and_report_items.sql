CREATE TABLE reports (
    report_id BIGINT NOT NULL AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    report_type VARCHAR(255) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    ai_summary TEXT,
    ai_summary_status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (report_id),
    CONSTRAINT uk_reports_organization_type_period
        UNIQUE (organization_id, report_type, period_start)
);

CREATE TABLE report_items (
    report_item_id BIGINT NOT NULL AUTO_INCREMENT,
    report_id BIGINT NOT NULL,
    report_item_type VARCHAR(30) NOT NULL,
    medicine_package_unit_id BIGINT NOT NULL,
    medicine_name VARCHAR(255) NOT NULL,
    pack_unit VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (report_item_id),
    CONSTRAINT fk_report_items_report
        FOREIGN KEY (report_id) REFERENCES reports (report_id)
);