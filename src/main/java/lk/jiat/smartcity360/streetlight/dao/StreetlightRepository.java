package lk.jiat.smartcity360.streetlight.dao;

import lk.jiat.smartcity360.streetlight.model.LightConfiguration;
import lk.jiat.smartcity360.streetlight.model.Streetlight;

import java.util.List;

public interface StreetlightRepository {
    void createBatch(List<Streetlight> streetlights);

    List<Streetlight> recent(int limit);

    long countLights();

    long countConfigurations();
}
