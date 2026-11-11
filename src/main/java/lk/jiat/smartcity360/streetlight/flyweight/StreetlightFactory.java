package lk.jiat.smartcity360.streetlight.flyweight;

import lk.jiat.smartcity360.streetlight.model.LightConfiguration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class StreetlightFactory {
    private final ConcurrentMap<LightConfiguration, LightConfiguration> shared = new ConcurrentHashMap<>();

    public LightConfiguration getSharedConfiguration(LightConfiguration configuration) {
        return shared.computeIfAbsent(configuration, key -> key);
    }

    public int uniqueSharedConfigurations() {
        return shared.size();
    }
}
