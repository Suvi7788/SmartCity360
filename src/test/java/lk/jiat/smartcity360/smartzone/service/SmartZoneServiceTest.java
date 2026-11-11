package lk.jiat.smartcity360.smartzone.service;

import lk.jiat.smartcity360.smartzone.composite.ZoneOperation;
import lk.jiat.smartcity360.smartzone.dao.ZoneRepository;
import lk.jiat.smartcity360.smartzone.model.ZoneRecord;
import lk.jiat.smartcity360.smartzone.model.ZoneType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmartZoneServiceTest {
    @Test
    void buildsTreeRowsFromRootNodesAndDescendants() {
        ZoneRepository repository = new ZoneRepository() {
            @Override
            public List<ZoneRecord> findAll() {
                return List.of(
                        new ZoneRecord(1, null, ZoneType.CITY, "Metro"),
                        new ZoneRecord(2, 1L, ZoneType.DISTRICT, "North"),
                        new ZoneRecord(3, 2L, ZoneType.STREET, "Main"),
                        new ZoneRecord(4, 3L, ZoneType.BUILDING, "Library",
                                true, new BigDecimal("12.500")));
            }

            @Override
            public long add(Long parentId, String type, String name, BigDecimal usageKwh) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void recordOperation(long zoneId, ZoneOperation operation, String summary,
                                        List<Long> affectedZoneIds) {
                throw new UnsupportedOperationException();
            }
        };

        var rows = new SmartZoneService(repository).rows();

        assertEquals(4, rows.size());
        assertEquals(0, rows.get(0).depth());
        assertEquals(3, rows.get(3).depth());
        assertEquals(new BigDecimal("12.500"), rows.get(3).usageKwh());
    }

    @Test
    void shutdownPersistsSelectedZoneAndEveryDescendant() {
        AtomicReference<List<Long>> affectedIds = new AtomicReference<>();
        LinkedHashMap<Long, ZoneRecord> persistedZones = new LinkedHashMap<>();
        ZoneRepository repository = new ZoneRepository() {
            @Override
            public List<ZoneRecord> findAll() {
                List<ZoneRecord> zones = List.of(
                        new ZoneRecord(1, null, ZoneType.CITY, "Metro"),
                        new ZoneRecord(2, 1L, ZoneType.DISTRICT, "North"),
                        new ZoneRecord(3, 2L, ZoneType.STREET, "Main"),
                        new ZoneRecord(4, 3L, ZoneType.BUILDING, "Library"));
                zones.forEach(zone -> persistedZones.putIfAbsent(zone.id(), zone));
                return List.copyOf(persistedZones.values());
            }

            @Override
            public long add(Long parentId, String type, String name, BigDecimal usageKwh) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void recordOperation(long zoneId, ZoneOperation operation, String summary,
                                        List<Long> affectedZoneIds) {
                assertEquals(1, zoneId);
                assertEquals(ZoneOperation.SHUTDOWN_POWER, operation);
                affectedIds.set(affectedZoneIds);
                affectedZoneIds.forEach(id -> {
                    ZoneRecord prior = persistedZones.get(id);
                    persistedZones.put(id, new ZoneRecord(prior.id(), prior.parentId(), prior.type(),
                            prior.name(), false, prior.usageKwh()));
                });
            }
        };

        List<String> results = new SmartZoneService(repository).execute(1, ZoneOperation.SHUTDOWN_POWER);

        assertEquals(List.of(1L, 2L, 3L, 4L), affectedIds.get());
        assertTrue(persistedZones.values().stream().noneMatch(ZoneRecord::powerOn));
        assertEquals(4, results.size());
        assertTrue(results.stream().allMatch(line -> line.contains("power shut down")));
    }

    @Test
    void reportsPersistedUsageAndSupportsBuildingLevelOperations() {
        AtomicReference<List<Long>> affectedIds = new AtomicReference<>();
        AtomicReference<String> operationSummary = new AtomicReference<>();
        ZoneRepository repository = new ZoneRepository() {
            @Override
            public List<ZoneRecord> findAll() {
                return List.of(
                        new ZoneRecord(1, null, ZoneType.CITY, "Metro"),
                        new ZoneRecord(2, 1L, ZoneType.DISTRICT, "North"),
                        new ZoneRecord(3, 2L, ZoneType.STREET, "Main"),
                        new ZoneRecord(4, 3L, ZoneType.BUILDING, "Library",
                                true, new BigDecimal("12.500")),
                        new ZoneRecord(5, 3L, ZoneType.BUILDING, "Office",
                                true, new BigDecimal("2.500")));
            }

            @Override
            public long add(Long parentId, String type, String name, BigDecimal usageKwh) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void recordOperation(long zoneId, ZoneOperation operation, String summary,
                                        List<Long> affectedZoneIds) {
                assertEquals(ZoneOperation.REPORT_USAGE, operation);
                affectedIds.set(affectedZoneIds);
                operationSummary.set(summary);
            }
        };

        SmartZoneService service = new SmartZoneService(repository);
        List<String> cityReport = service.execute(1, ZoneOperation.REPORT_USAGE);
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), affectedIds.get());
        assertTrue(cityReport.stream().anyMatch(line -> line.contains("Subtree total: 15.000 kWh")));

        List<String> report = service.execute(4, ZoneOperation.REPORT_USAGE);
        assertEquals(List.of(4L), affectedIds.get());
        assertTrue(report.stream()
                .anyMatch(line -> line.contains("12.500 kWh")));
        assertTrue(operationSummary.get().contains("Subtree total: 12.500 kWh"));
    }

    @Test
    void rejectsMissingParentsBuildingChildrenDuplicateNamesAndInvalidStoredCycles() {
        SmartZoneService emptyService = new SmartZoneService(repositoryFor(List.of()));
        assertThrows(IllegalArgumentException.class, () -> emptyService.add(99L, "Missing"));

        SmartZoneService buildingService = new SmartZoneService(repositoryFor(List.of(
                new ZoneRecord(1, null, ZoneType.CITY, "Metro"),
                new ZoneRecord(2, 1L, ZoneType.DISTRICT, "North"),
                new ZoneRecord(3, 2L, ZoneType.STREET, "Main"),
                new ZoneRecord(4, 3L, ZoneType.BUILDING, "Library"))));
        assertThrows(IllegalArgumentException.class, () -> buildingService.add(4L, "Annex"));

        SmartZoneService duplicateService = new SmartZoneService(repositoryFor(List.of(
                new ZoneRecord(1, null, ZoneType.CITY, "Metro"))));
        assertThrows(IllegalArgumentException.class, () -> duplicateService.add(null, " metro "));

        SmartZoneService cyclicService = new SmartZoneService(repositoryFor(List.of(
                new ZoneRecord(1, 2L, ZoneType.CITY, "Metro"),
                new ZoneRecord(2, 1L, ZoneType.DISTRICT, "North"))));
        assertThrows(IllegalStateException.class, cyclicService::tree);
    }

    private ZoneRepository repositoryFor(List<ZoneRecord> records) {
        return new ZoneRepository() {
            @Override
            public List<ZoneRecord> findAll() {
                return records;
            }

            @Override
            public long add(Long parentId, String type, String name, BigDecimal usageKwh) {
                return 1;
            }

            @Override
            public void recordOperation(long zoneId, ZoneOperation operation, String summary,
                                        List<Long> affectedZoneIds) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
