package lk.jiat.smartcity360.smartzone.model;

import java.math.BigDecimal;

public record ZoneViewRow(long id, Long parentId, ZoneType type, String name,
                          int depth, boolean hasChildren, boolean powerOn, BigDecimal usageKwh) {
    public ZoneViewRow {
        usageKwh = usageKwh == null ? BigDecimal.ZERO : usageKwh;
    }

    public boolean canHaveChildren() {
        return type.childType() != null;
    }
}
