
INSERT INTO medicines (medicine_id, item_code, product_name, company_name,storage_method,validity_period,narcotic_kind_code,created_at)
VALUES
    (1, '001', '타이레놀정', 'A제약','실온보관','10개월',null,'2026-08-20 17:00:00'),
    (2, '002', '타이레놀8시간이알서방정', 'B제약','실온보관','12개월',null,'2026-08-21 12:30:00'),
    (3, '003', '아스피린정', 'C제약','실온보관','20개월',null,'2026-08-22 14:10:00');

INSERT INTO medicine_package_units (medicine_package_unit_id, medicine_id, pack_unit)
VALUES
    (1, 1, '10정'),
    (2, 1, '30정'),
    (3, 2, '20정'),
    (4, 3, '100정');