package com.nhnacademy.inventory.assistant.rule.inbound;

import com.nhnacademy.inventory.assistant.event.StockInboundCompletedEvent;
import com.nhnacademy.inventory.assistant.rule.Finding;

import java.util.Optional;

public interface InboundRule {

    Optional<Finding> evaluate(StockInboundCompletedEvent event, Long organizationId);
}
