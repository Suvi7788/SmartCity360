package lk.jiat.smartcity360.smartzone.composite;

import lk.jiat.smartcity360.smartzone.model.ZoneRecord;
import lk.jiat.smartcity360.smartzone.model.ZoneType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZoneCompositeTest {
    @Test
    void appliesOperationsToTheSelectedNodeAndEveryDescendant() {
        ZoneComposite city = new ZoneComposite(new ZoneRecord(1, null, ZoneType.CITY, "Metro"));
        ZoneComposite district = new ZoneComposite(new ZoneRecord(2, 1L, ZoneType.DISTRICT, "North"));
        ZoneComposite street = new ZoneComposite(new ZoneRecord(3, 2L, ZoneType.STREET, "Main"));
        street.add(new ZoneLeaf(new ZoneRecord(4, 3L, ZoneType.BUILDING, "Library")));
        district.add(street);
        city.add(district);

        var result = city.execute(ZoneOperation.SHUTDOWN_POWER);

        assertEquals(4, result.size());
        assertThrows(IllegalArgumentException.class, () -> city.execute(null));
    }
}
