package com.nhnacademy.inventory.reports.report.dto;

import com.nhnacademy.inventory.reports.report.domain.ReportItem;
import com.nhnacademy.inventory.reports.report.domain.ReportItemType;

public record ReportItemResponse(
        Long reportItemId,
        ReportItemType reportItemType,
        Long medicinePackageUnitId,
        String medicineName,
        String packUnit,
        int quantity
) {
    public static ReportItemResponse from(ReportItem item) {
        return new ReportItemResponse(
                item.getId(),
                item.getReportItemType(),
                item.getMedicinePackageUnitId(),
                item.getMedicineName(),
                item.getPackUnit(),
                item.getQuantity()
        );
    }
}
