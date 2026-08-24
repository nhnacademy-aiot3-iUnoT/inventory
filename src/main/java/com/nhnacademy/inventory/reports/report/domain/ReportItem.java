package com.nhnacademy.inventory.reports.report.domain;

import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "report_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    @Column(name = "report_item_type", length = 30, nullable = false)
    private ReportItemType reportItemType;

    @Column(name = "medicine_package_unit_id", nullable = false)
    private Long medicinePackageUnitId;

    @Column(name = "medicine_name", nullable = false)
    private String medicineName;

    @Column(name = "pack_unit", nullable = false)
    private String packUnit;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    private ReportItem(Report report, ReportItemType reportItemType, MedicinePackageUnit unit, int quantity) {
        this.report = report;
        this.reportItemType = reportItemType;
        this.medicineName = unit.getMedicine().getProductName();
        this.medicinePackageUnitId = unit.getId();
        this.packUnit = unit.getPackUnit();
        this.quantity = quantity;
    }

    public static ReportItem inbound(Report report, MedicinePackageUnit unit, int quantity) {
        return new ReportItem(report, ReportItemType.INBOUND, unit, quantity);
    }

    public static ReportItem outbound(Report report, MedicinePackageUnit unit, int quantity) {
        return new ReportItem(report, ReportItemType.OUTBOUND, unit, quantity);
    }

    public static ReportItem disposal(Report report, MedicinePackageUnit unit, int quantity) {
        return new ReportItem(report, ReportItemType.DISPOSAL, unit, quantity);
    }
}
