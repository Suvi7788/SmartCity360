package lk.jiat.smartcity360.smartzone.composite;

import lk.jiat.smartcity360.smartzone.model.ZoneRecord;

import java.math.BigDecimal;
import java.util.List;

public final class ZoneLeaf implements ZoneComponent {
    private final ZoneRecord record;

    public ZoneLeaf(ZoneRecord record) {
        this.record = record;
    }

    @Override
    public ZoneRecord record() {
        return record;
    }

    @Override
    public List<String> execute(ZoneOperation operation) {
        if (operation == null) {
            throw new IllegalArgumentException("Select a zone operation");
        }
        return List.of(executeFor(record, operation));
    }

    @Override
    public List<Long> zoneIds() {
        return List.of(record.id());
    }

    @Override
    public BigDecimal totalUsageKwh() {
        return record.usageKwh();
    }

    static String executeFor(ZoneRecord record, ZoneOperation operation) {
        if (operation == ZoneOperation.SHUTDOWN_POWER) {
            return record.type() + " " + record.name()
                    + (record.powerOn() ? ": power shut down" : ": power already shut down");
        }
        return record.type() + " " + record.name() + ": recorded usage " + record.usageKwh() + " kWh";
    }
}
