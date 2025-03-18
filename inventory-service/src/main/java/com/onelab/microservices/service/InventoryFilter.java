package com.onelab.microservices.service;

import com.onelab.microservices.model.NewInventoryItem;

@FunctionalInterface
public interface InventoryFilter {
    boolean filter(NewInventoryItem item);
}

