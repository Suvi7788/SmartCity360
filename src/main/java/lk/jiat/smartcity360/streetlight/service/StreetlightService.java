package lk.jiat.smartcity360.streetlight.service;

import lk.jiat.smartcity360.streetlight.dao.StreetlightRepository;
import lk.jiat.smartcity360.streetlight.flyweight.StreetlightFactory;
import lk.jiat.smartcity360.streetlight.model.LightConfiguration;
import lk.jiat.smartcity360.streetlight.model.Streetlight;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class StreetlightService {
    private final StreetlightRepository repository;
    private final StreetlightFactory factory;

    public StreetlightService(StreetlightRepository repository, StreetlightFactory factory) {
        this.repository = repository;
        this.factory = factory;
    }

    public int createBatch(int count, String location, Long zoneId, LightConfiguration configuration) {
        if (count < 1 || count > 10_000) {
            throw new IllegalArgumentException("Create between 1 and 10,000 streetlights per batch");
        }
        if (configuration == null) {
            throw new IllegalArgumentException("Streetlight configuration is required");
        }
        if (zoneId != null && zoneId < 1) {
            throw new IllegalArgumentException("Zone must be a positive number");
        }
        String cleanLocation = location == null ? "" : location.trim();
        if (cleanLocation.isEmpty() || cleanLocation.length() > 150) {
            throw new IllegalArgumentException("Location must contain 1-150 characters");
        }
        LightConfiguration shared = factory.getSharedConfiguration(configuration);
        String batchId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        List<Streetlight> lights = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            lights.add(new Streetlight("SL-" + batchId + "-" + i, zoneId,
                    cleanLocation + " / Pole " + i, "ON", shared));
        }
        repository.createBatch(lights);
        return lights.size();
    }

    public List<Streetlight> recent() {
        return repository.recent(50).stream()
                .map(streetlight -> new Streetlight(streetlight.code(), streetlight.zoneId(),
                        streetlight.location(), streetlight.status(),
                        factory.getSharedConfiguration(streetlight.configuration())))
                .toList();
    }

    public long totalLights() {
        return repository.countLights();
    }

    public long uniqueConfigurations() {
        return repository.countConfigurations();
    }

    public int cachedConfigurations() {
        return factory.uniqueSharedConfigurations();
    }
}
