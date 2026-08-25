ALTER TABLE organizations
    MODIFY COLUMN status VARCHAR(30) NOT NULL;

ALTER TABLE zones
    MODIFY COLUMN status VARCHAR(30) NOT NULL,
    MODIFY COLUMN env_status VARCHAR(30) NOT NULL;

ALTER TABLE organization_members
    MODIFY COLUMN organization_role VARCHAR(30) NOT NULL;

ALTER TABLE departments
    MODIFY COLUMN status VARCHAR(30) NOT NULL;

ALTER TABLE storages
    MODIFY COLUMN status VARCHAR(30) NOT NULL;

ALTER TABLE invitations
    MODIFY COLUMN invitation_status VARCHAR(30) NOT NULL;

ALTER TABLE medicine_environment_types
    MODIFY COLUMN environment_type VARCHAR(30) NOT NULL;

ALTER TABLE stock_transactions
    MODIFY COLUMN transaction_type VARCHAR(30) NOT NULL;

ALTER TABLE alerts
    MODIFY COLUMN alert_type VARCHAR(30) NOT NULL;

ALTER TABLE medicine_inventorys
    MODIFY COLUMN management_status VARCHAR(30) NOT NULL;
