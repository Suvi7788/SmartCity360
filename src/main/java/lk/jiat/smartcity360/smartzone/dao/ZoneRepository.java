package lk.jiat.smartcity360.smartzone.dao;

import lk.jiat.smartcity360.smartzone.composite.ZoneOperation;
import lk.jiat.smartcity360.smartzone.model.ZoneRecord;

import java.math.BigDecimal;
import java.util.List;

public interface ZoneRepository {
    List<ZoneRecord> findAll();

    long add(Long parentId, String type, String name, BigDecimal usageKwh);

    void recordOperation(long zoneId, ZoneOperation operation, String summary, List<Long> affectedZoneIds);
}
