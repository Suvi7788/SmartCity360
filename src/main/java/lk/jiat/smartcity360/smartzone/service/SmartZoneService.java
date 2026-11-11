package lk.jiat.smartcity360.smartzone.service;

import lk.jiat.smartcity360.smartzone.composite.ZoneComponent;
import lk.jiat.smartcity360.smartzone.composite.ZoneComposite;
import lk.jiat.smartcity360.smartzone.composite.ZoneLeaf;
import lk.jiat.smartcity360.smartzone.composite.ZoneOperation;
import lk.jiat.smartcity360.smartzone.dao.ZoneRepository;
import lk.jiat.smartcity360.smartzone.model.ZoneRecord;
import lk.jiat.smartcity360.smartzone.model.ZoneType;
import lk.jiat.smartcity360.smartzone.model.ZoneViewRow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class SmartZoneService {
    private final ZoneRepository repository;

    public SmartZoneService(ZoneRepository repository) {
        this.repository = repository;
    }

    public List<ZoneComponent> tree() {
        return buildTree(repository.findAll());
    }

    private List<ZoneComponent> buildTree(List<ZoneRecord> records) {
        Map<Long, ZoneComponent> nodes = new HashMap<>();
        Map<Long, ZoneRecord> byId = new HashMap<>();
        Set<String> siblingNames = new HashSet<>();
        for (ZoneRecord record : records) {
            if (record.id() < 1 || record.type() == null || record.name() == null
                    || record.name().isBlank() || record.name().length() > 120) {
                throw new IllegalStateException("Stored zone contains invalid identity or name data");
            }
            if (byId.putIfAbsent(record.id(), record) != null) {
                throw new IllegalStateException("Stored zone IDs are not unique");
            }
            String parentKey = record.parentId() == null ? "root" : record.parentId().toString();
            String siblingKey = parentKey + ":" + record.name().trim().toLowerCase(Locale.ROOT);
            if (!siblingNames.add(siblingKey)) {
                throw new IllegalStateException("Stored zones contain duplicate sibling names");
            }
            nodes.put(record.id(), record.type() == ZoneType.BUILDING
                    ? new ZoneLeaf(record) : new ZoneComposite(record));
        }
        List<ZoneComponent> roots = new ArrayList<>();
        for (ZoneRecord record : records) {
            ZoneComponent node = nodes.get(record.id());
            if (record.parentId() == null) {
                if (record.type() != ZoneType.CITY) {
                    throw new IllegalStateException("Root zone must be a city: " + record.name());
                }
                roots.add(node);
                continue;
            }
            ZoneComponent parent = nodes.get(record.parentId());
            ZoneRecord parentRecord = byId.get(record.parentId());
            if (parentRecord == null || !(parent instanceof ZoneComposite composite)
                    || parentRecord.type().childType() != record.type()) {
                throw new IllegalStateException("Invalid zone hierarchy at " + record.name());
            }
            composite.add(node);
        }
        Set<Long> visited = new HashSet<>();
        for (ZoneComponent root : roots) {
            validateAcyclic(root, visited);
        }
        if (visited.size() != records.size()) {
            throw new IllegalStateException("Stored zone hierarchy contains a cycle or disconnected nodes");
        }
        return List.copyOf(roots);
    }

    private void validateAcyclic(ZoneComponent node, Set<Long> visited) {
        if (!visited.add(node.record().id())) {
            throw new IllegalStateException("Stored zone hierarchy contains a cycle");
        }
        if (node instanceof ZoneComposite composite) {
            for (ZoneComponent child : composite.children()) {
                validateAcyclic(child, visited);
            }
        }
    }

    public List<ZoneViewRow> rows() {
        List<ZoneViewRow> result = new ArrayList<>();
        for (ZoneComponent root : tree()) {
            appendRows(root, null, 0, result);
        }
        return List.copyOf(result);
    }

    public long add(Long parentId, String name) {
        return add(parentId, name, BigDecimal.ZERO);
    }

    public long add(Long parentId, String name, BigDecimal usageKwh) {
        String cleanName = name == null ? "" : name.trim();
        if (cleanName.isEmpty() || cleanName.length() > 120) {
            throw new IllegalArgumentException("Zone name must contain 1-120 characters");
        }
        BigDecimal cleanUsage = usageKwh == null ? BigDecimal.ZERO : usageKwh;
        if (cleanUsage.signum() < 0 || cleanUsage.scale() > 3
                || cleanUsage.compareTo(new BigDecimal("999999999.999")) > 0) {
            throw new IllegalArgumentException("Usage must be between 0 and 999999999.999 kWh");
        }
        List<ZoneRecord> zones = repository.findAll();
        buildTree(zones);
        if (zones.stream().anyMatch(zone -> Objects.equals(zone.parentId(), parentId)
                && zone.name().trim().equalsIgnoreCase(cleanName))) {
            throw new IllegalArgumentException("A zone with this name already exists under the selected parent");
        }
        ZoneType type;
        if (parentId == null) {
            type = ZoneType.CITY;
            if (cleanUsage.signum() != 0) {
                throw new IllegalArgumentException("Usage readings can only be recorded for buildings");
            }
        } else {
            if (parentId < 1) {
                throw new IllegalArgumentException("Parent zone must be a positive number");
            }
            ZoneRecord parent = zones.stream()
                    .filter(zone -> zone.id() == parentId.longValue())
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Parent zone does not exist"));
            type = parent.type().childType();
            if (type == null) {
                throw new IllegalArgumentException("Buildings cannot contain child zones");
            }
            if (type != ZoneType.BUILDING && cleanUsage.signum() != 0) {
                throw new IllegalArgumentException("Usage readings can only be recorded for buildings");
            }
        }
        return repository.add(parentId, type.name(), cleanName, cleanUsage);
    }

    public List<String> execute(long zoneId, ZoneOperation operation) {
        if (operation == null) {
            throw new IllegalArgumentException("Select a zone operation");
        }
        ZoneComponent root = flatten(tree()).get(zoneId);
        if (root == null) {
            throw new IllegalArgumentException("Zone does not exist");
        }
        List<String> results = root.execute(operation);
        if (operation == ZoneOperation.REPORT_USAGE) {
            results = new ArrayList<>(results);
            results.add("Subtree total: " + root.totalUsageKwh() + " kWh across "
                    + root.zoneIds().size() + " zone(s)");
        }
        repository.recordOperation(zoneId, operation, String.join("\n", results), root.zoneIds());
        return results;
    }

    private Map<Long, ZoneComponent> flatten(List<ZoneComponent> roots) {
        Map<Long, ZoneComponent> nodes = new HashMap<>();
        for (ZoneComponent root : roots) {
            flatten(root, nodes);
        }
        return nodes;
    }

    private void flatten(ZoneComponent node, Map<Long, ZoneComponent> nodes) {
        nodes.put(node.record().id(), node);
        if (node instanceof ZoneComposite composite) {
            for (ZoneComponent child : composite.children()) {
                flatten(child, nodes);
            }
        }
    }

    private void appendRows(ZoneComponent node, Long parentId, int depth, List<ZoneViewRow> rows) {
        List<ZoneComponent> children = node instanceof ZoneComposite composite ? composite.children() : List.of();
        rows.add(new ZoneViewRow(node.record().id(), parentId, node.record().type(), node.record().name(),
                depth, !children.isEmpty(), node.record().powerOn(), node.record().usageKwh()));
        for (ZoneComponent child : children) {
            appendRows(child, node.record().id(), depth + 1, rows);
        }
    }
}
