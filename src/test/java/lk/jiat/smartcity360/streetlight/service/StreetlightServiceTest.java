package lk.jiat.smartcity360.streetlight.service;

import lk.jiat.smartcity360.streetlight.dao.StreetlightRepository;
import lk.jiat.smartcity360.streetlight.flyweight.StreetlightFactory;
import lk.jiat.smartcity360.streetlight.model.LightConfiguration;
import lk.jiat.smartcity360.streetlight.model.Streetlight;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

class StreetlightServiceTest {
    private static final LightConfiguration CONFIGURATION =
            new LightConfiguration(80, "Adaptive", 4000, "Standard");

    @Test
    void createsOneThousandLightsWithOneSharedConfiguration() {
        assertBatchUsesOneFlyweight(1_000);
    }

    @Test
    void createsTenThousandLightsWithOneSharedConfiguration() {
        assertBatchUsesOneFlyweight(10_000);
    }

    @Test
    void reconstructsRetrievedLightsThroughTheFactory() {
        TestRepository repository = new TestRepository();
        repository.recent = List.of(
                new Streetlight("SL-1", 1L, "Main / Pole 1", "ON",
                        new LightConfiguration(80, "Adaptive", 4000, "Standard")),
                new Streetlight("SL-2", 1L, "Main / Pole 2", "OFF",
                        new LightConfiguration(80, "Adaptive", 4000, "Standard")));
        repository.persistedConfigurationCount = 12;
        StreetlightService service = new StreetlightService(repository, new StreetlightFactory());

        List<Streetlight> lights = service.recent();

        assertSame(lights.get(0).configuration(), lights.get(1).configuration());
        assertEquals(12, service.uniqueConfigurations());
        assertEquals(1, service.cachedConfigurations());
        assertEquals("SL-1", lights.get(0).code());
        assertEquals("Main / Pole 2", lights.get(1).location());
    }

    @Test
    void rejectsUnsupportedOrOversizedConfigurationFields() {
        assertThrows(IllegalArgumentException.class,
                () -> new LightConfiguration(80, "Unknown", 4000, "Standard"));
        assertThrows(IllegalArgumentException.class,
                () -> new LightConfiguration(80, "Adaptive", 4000, "Unsupported"));
        assertThrows(IllegalArgumentException.class,
                () -> new LightConfiguration(80, "A".repeat(33), 4000, "Standard"));
        assertThrows(IllegalArgumentException.class,
                () -> new LightConfiguration(80, "Adaptive", 4000, "S".repeat(49)));
    }

    private void assertBatchUsesOneFlyweight(int count) {
        TestRepository repository = new TestRepository();
        StreetlightFactory factory = new StreetlightFactory();
        StreetlightService service = new StreetlightService(repository, factory);

        assertEquals(count, service.createBatch(count, "Central Avenue", 7L, CONFIGURATION));

        assertEquals(count, repository.created.size());
        assertEquals(1, factory.uniqueSharedConfigurations());
        Streetlight first = repository.created.get(0);
        for (Streetlight streetlight : repository.created) {
            assertSame(first.configuration(), streetlight.configuration());
        }
    }

    private static final class TestRepository implements StreetlightRepository {
        private List<Streetlight> created = List.of();
        private List<Streetlight> recent = List.of();
        private long persistedConfigurationCount;

        @Override
        public void createBatch(List<Streetlight> streetlights) {
            created = List.copyOf(streetlights);
        }

        @Override
        public List<Streetlight> recent(int limit) {
            return recent;
        }

        @Override
        public long countLights() {
            return created.size();
        }

        @Override
        public long countConfigurations() {
            return persistedConfigurationCount;
        }
    }
}
