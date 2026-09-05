package com.nhnacademy.inventory.assistant.rule.outbound;

import com.nhnacademy.inventory.assistant.event.StockOutboundCompletedEvent;
import com.nhnacademy.inventory.assistant.rule.Finding;

import java.util.Optional;

public interface OutboundRule {

    Optional<Finding> evaluate(StockOutboundCompletedEvent event, Long organizationId);
}
