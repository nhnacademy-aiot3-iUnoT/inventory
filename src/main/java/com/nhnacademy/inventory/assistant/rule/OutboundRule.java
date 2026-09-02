package com.nhnacademy.inventory.assistant.rule;

import com.nhnacademy.inventory.assistant.event.StockOutboundCompletedEvent;

import java.util.Optional;

public interface OutboundRule {

    Optional<Finding> evaluate(StockOutboundCompletedEvent event, Long organizationId);
}
