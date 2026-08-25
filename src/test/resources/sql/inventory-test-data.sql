


INSERT INTO organizations (
    organization_id,
    business_number,
    name,
    road_address,
    zip_code,
    address_detail,
    description,
    status,
    created_at
)
VALUES
    (
        1,
        '1234567890',
        '조선대학교병원',
        '광주광역시 동구 필문대로',
        '61453',
        '본관',
        '테스트 조직 1',
        'ACTIVE',
        '2026-08-20 10:00:00'
    ),
    (
        2,
        '0987654321',
        '테스트병원',
        '광주광역시 북구 테스트로',
        '61100',
        '별관',
        '테스트 조직 2',
        'ACTIVE',
        '2026-08-20 10:00:00'
    );


INSERT INTO departments (
    department_id,
    organization_id,
    name,
    description,
    status,
    created_at
)
VALUES
    (
        1,
        1,
        '약제부',
        '의약품 관리 부서',
        'ACTIVE',
        '2026-08-20 10:00:00'
    ),
    (
        2,
        1,
        '응급의학과',
        '응급 의약품 관리 부서',
        'ACTIVE',
        '2026-08-20 10:00:00'
    );



-- ========================================
-- 1. 의약품
-- ========================================
INSERT INTO medicines (
    medicine_id,
    item_code,
    product_name,
    storage_method,
    validity_period,
    narcotic_kind_code,
    company_name,
    created_at
)
VALUES
    (1, '001', '타이레놀정', '실온보관', '36개월', NULL, 'A제약', '2026-08-20 10:00:00'),
    (2, '002', '아스피린정', '실온보관', '24개월', NULL, 'B제약', '2026-08-20 10:00:00'),
    (3, '003', '인슐린주', '냉장보관', '12개월', NULL, 'C제약', '2026-08-20 10:00:00');


-- ========================================
-- 2. 의약품 포장단위
-- ========================================
INSERT INTO medicine_package_units (
    medicine_package_unit_id,
    medicine_id,
    pack_unit
)
VALUES
    (1, 1, '10정'),
    (2, 1, '30정'),
    (3, 2, '100정'),
    (4, 3, '1바이알');


-- ========================================
-- 3. 저장소
-- organization_id = 1이 이미 존재한다고 가정
-- ========================================
INSERT INTO storages (
    storage_id,
    organization_id,
    name,
    description,
    status,
    created_at
)
VALUES
    (1, 1, '약품창고 A', '일반 의약품 저장소', 'ACTIVE', '2026-08-20 10:00:00'),
    (2, 1, '냉장창고', '냉장 의약품 저장소', 'ACTIVE', '2026-08-20 10:00:00'),
    (3, 1, '응급약품창고', '응급 의약품 저장소', 'ACTIVE', '2026-08-20 10:00:00');


-- ========================================
-- 4. 저장소 - 부서 연결
-- department_id = 1, 2가 이미 존재한다고 가정
-- ========================================
INSERT INTO storage_departments (
    storage_department_id,
    storage_id,
    department_id,
    created_at
)
VALUES
    (1, 1, 1, '2026-08-20 10:00:00'),
    (2, 2, 1, '2026-08-20 10:00:00'),
    (3, 3, 2, '2026-08-20 10:00:00');


-- ========================================
-- 5. 구역
-- ========================================
INSERT INTO zones (
    zone_id,
    storage_id,
    name,
    description,
    status,
    env_status,
    created_at
)
VALUES
    (1, 1, '일반구역 A', '일반 의약품 구역', 'ACTIVE', 'NORMAL', '2026-08-20 10:00:00'),
    (2, 1, '일반구역 B', '일반 의약품 구역', 'ACTIVE', 'NORMAL', '2026-08-20 10:00:00'),
    (3, 2, '냉장구역', '냉장 보관 구역', 'ACTIVE', 'NORMAL', '2026-08-20 10:00:00'),
    (4, 3, '응급구역', '응급 약품 보관 구역', 'ACTIVE', 'NORMAL', '2026-08-20 10:00:00');


-- ========================================
-- 6. 의약품 재고
-- ========================================
INSERT INTO medicine_inventorys (
    inventory_id,
    medicine_package_unit_id,
    zone_id,
    lot_number,
    expiration_date,
    current_quantity,
    management_status,
    created_at,
    updated_at
)
VALUES
    (1, 1, 1, 'LOT-TY-001', '2027-01-31', 100, 'NORMAL',
     '2026-08-20 10:00:00', '2026-08-20 10:00:00'),

    (2, 2, 2, 'LOT-TY-002', '2027-03-31', 50, 'NORMAL',
     '2026-08-20 10:00:00', '2026-08-20 10:00:00'),

    (3, 3, 1, 'LOT-AS-001', '2026-12-31', 200, 'NORMAL',
     '2026-08-20 10:00:00', '2026-08-20 10:00:00'),

    (4, 4, 3, 'LOT-IN-001', '2026-10-31', 30, 'NORMAL',
     '2026-08-20 10:00:00', '2026-08-20 10:00:00'),

    (5, 1, 4, 'LOT-TY-003', '2027-05-31', 70, 'NORMAL',
     '2026-08-20 10:00:00', '2026-08-20 10:00:00');




