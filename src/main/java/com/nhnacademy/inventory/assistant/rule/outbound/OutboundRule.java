package com.nhnacademy.inventory.assistant.rule.outbound;

import com.nhnacademy.inventory.assistant.event.StockOutboundInspectionEvent;
import com.nhnacademy.inventory.assistant.rule.Finding;

import java.util.Optional;

public interface OutboundRule {

    Optional<Finding> evaluate(StockOutboundInspectionEvent event, Long organizationId);
}
