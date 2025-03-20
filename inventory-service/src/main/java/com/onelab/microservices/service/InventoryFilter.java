package com.onelab.microservices.service;

import com.onelab.microservices.model.InventoryItem;

@FunctionalInterface
public interface InventoryFilter {
    boolean filter(InventoryItem item);
}

