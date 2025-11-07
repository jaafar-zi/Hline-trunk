package com.example.demo.domain.orderManagementDomain.distanceCalculator;

import java.util.Map;

public class GovernorateDistance {
    private static final Map<String, Integer> DISTANCES_FROM_KAIROUAN = Map.ofEntries(
            Map.entry("Tunis", 150),
            Map.entry("Sousse", 60),
            Map.entry("Sfax", 120),
            Map.entry("Gabes", 210),
            Map.entry("Gafsa", 170),
            Map.entry("Bizerte", 190),
            Map.entry("Beja", 140),
            Map.entry("Nabeul", 110),
            Map.entry("Monastir", 90),
            Map.entry("Mahdia", 100),
            Map.entry("Kasserine", 120),
            Map.entry("Tozeur", 280),
            Map.entry("Kebili", 240),
            Map.entry("Medenine", 300),
            Map.entry("Tataouine", 350)
    );

    public static Integer getDistance(String destination) {
        return DISTANCES_FROM_KAIROUAN.getOrDefault(destination, 0);
    }
}

