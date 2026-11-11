package lk.jiat.smartcity360.smartzone.composite;

import lk.jiat.smartcity360.smartzone.model.ZoneRecord;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.List;

public final class ZoneComposite implements ZoneComponent {
    private final ZoneRecord record;
    private final List<ZoneComponent> children = new ArrayList<>();

    public ZoneComposite(ZoneRecord record) {
        this.record = record;
    }

    public void add(ZoneComponent child) {
        if (record.type().childType() == null || child.record().type() != record.type().childType()) {
            throw new IllegalArgumentException("Zone child type does not match the composite hierarchy");
        }
        children.add(child);
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
        List<String> results = new ArrayList<>();
        results.add(ZoneLeaf.executeFor(record, operation));
        for (ZoneComponent child : children) {
            results.addAll(child.execute(operation));
        }
        return results;
    }

    @Override
    public List<Long> zoneIds() {
        List<Long> ids = new ArrayList<>();
        ids.add(record.id());
        for (ZoneComponent child : children) {
            ids.addAll(child.zoneIds());
        }
        return List.copyOf(ids);
    }

    @Override
    public BigDecimal totalUsageKwh() {
        BigDecimal total = record.usageKwh();
        for (ZoneComponent child : children) {
            total = total.add(child.totalUsageKwh());
        }
        return total;
    }

    public List<ZoneComponent> children() {
        return List.copyOf(children);
    }
}
