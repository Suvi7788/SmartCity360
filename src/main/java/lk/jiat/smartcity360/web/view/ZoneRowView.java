package lk.jiat.smartcity360.web.view;

import lk.jiat.smartcity360.smartzone.model.ZoneViewRow;

import java.math.BigDecimal;

public final class ZoneRowView {
    private final long id;
    private final Long parentId;
    private final String type;
    private final String name;
    private final int depth;
    private final boolean hasChildren;
    private final boolean canHaveChildren;
    private final boolean powerOn;
    private final BigDecimal usageKwh;

    public ZoneRowView(ZoneViewRow row) {
        this.id = row.id();
        this.parentId = row.parentId();
        this.type = row.type().name();
        this.name = row.name();
        this.depth = row.depth();
        this.hasChildren = row.hasChildren();
        this.canHaveChildren = row.canHaveChildren();
        this.powerOn = row.powerOn();
        this.usageKwh = row.usageKwh();
    }

    public long getId() {
        return id;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getDepth() {
        return depth;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public boolean isCanHaveChildren() {
        return canHaveChildren;
    }

    public boolean isPowerOn() {
        return powerOn;
    }

    public BigDecimal getUsageKwh() {
        return usageKwh;
    }
}
