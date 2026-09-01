package com.nhnacademy.inventory.assistant.rule;

import com.nhnacademy.inventory.assistant.event.StockInboundCompletedEvent;

import java.util.Optional;

public interface InboundRule {

    Optional<Finding> evaluate(StockInboundCompletedEvent event, Long organizationId);
}
