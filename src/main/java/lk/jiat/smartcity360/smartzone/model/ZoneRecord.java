package lk.jiat.smartcity360.smartzone.model;

import java.math.BigDecimal;

public record ZoneRecord(long id, Long parentId, ZoneType type, String name,
                         boolean powerOn, BigDecimal usageKwh) {
    public ZoneRecord {
        usageKwh = usageKwh == null ? BigDecimal.ZERO : usageKwh;
    }

    public ZoneRecord(long id, Long parentId, ZoneType type, String name) {
        this(id, parentId, type, name, true, BigDecimal.ZERO);
    }
}
