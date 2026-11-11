package lk.jiat.smartcity360.smartzone.composite;

import lk.jiat.smartcity360.smartzone.model.ZoneRecord;

import java.math.BigDecimal;
import java.util.List;

public interface ZoneComponent {
    ZoneRecord record();

    List<String> execute(ZoneOperation operation);

    List<Long> zoneIds();

    BigDecimal totalUsageKwh();
}
