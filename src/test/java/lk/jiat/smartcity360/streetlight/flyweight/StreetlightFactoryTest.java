package lk.jiat.smartcity360.streetlight.flyweight;

import lk.jiat.smartcity360.streetlight.model.LightConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class StreetlightFactoryTest {
    @Test
    void sharesEqualImmutableConfigurations() {
        StreetlightFactory factory = new StreetlightFactory();
        LightConfiguration first = new LightConfiguration(80, "Adaptive", 4000, "Standard");
        LightConfiguration second = new LightConfiguration(80, "Adaptive", 4000, "Standard");

        assertSame(factory.getSharedConfiguration(first), factory.getSharedConfiguration(second));
        assertEquals(1, factory.uniqueSharedConfigurations());
    }

    @Test
    void doesNotShareDifferentConfigurations() {
        StreetlightFactory factory = new StreetlightFactory();
        LightConfiguration first = new LightConfiguration(80, "Adaptive", 4000, "Standard");
        LightConfiguration different = new LightConfiguration(120, "Adaptive", 4000, "Standard");

        assertNotSame(factory.getSharedConfiguration(first),
                factory.getSharedConfiguration(different));
        assertEquals(2, factory.uniqueSharedConfigurations());
    }
}
