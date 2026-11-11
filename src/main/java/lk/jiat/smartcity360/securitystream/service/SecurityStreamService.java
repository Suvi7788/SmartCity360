package lk.jiat.smartcity360.securitystream.service;

import lk.jiat.smartcity360.securitystream.dao.StreamRunRepository;
import lk.jiat.smartcity360.securitystream.decorator.AuthenticationDecorator;
import lk.jiat.smartcity360.securitystream.decorator.CompressionDecorator;
import lk.jiat.smartcity360.securitystream.decorator.EncryptionDecorator;
import lk.jiat.smartcity360.securitystream.decorator.RawMeterStream;
import lk.jiat.smartcity360.securitystream.decorator.StreamProcessor;
import lk.jiat.smartcity360.securitystream.model.StreamRun;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

public final class SecurityStreamService {
    private final StreamRunRepository repository;

    public SecurityStreamService(StreamRunRepository repository) {
        this.repository = repository;
    }

    public long process(String meterId, double energyKwh, List<String> decorators) {
        if (meterId == null || !meterId.matches("[A-Za-z0-9_-]{1,80}")) {
            throw new IllegalArgumentException("Meter ID must be 1-80 letters, digits, underscores, or hyphens");
        }
        if (!Double.isFinite(energyKwh) || energyKwh < 0 || energyKwh > 1_000_000) {
            throw new IllegalArgumentException("Energy usage must be between 0 and 1,000,000 kWh");
        }
        List<String> requested = decorators == null ? List.of() : List.copyOf(decorators);
        if (requested.stream().anyMatch(name ->
                !List.of("Compression", "Encryption", "Authentication").contains(name))) {
            throw new IllegalArgumentException("Unsupported stream decorator");
        }
        if (requested.stream().distinct().count() != requested.size()) {
            throw new IllegalArgumentException("A stream decorator can only be selected once");
        }
        List<String> selected = List.of("Compression", "Encryption", "Authentication").stream()
                .filter(requested::contains).toList();

        String original = "{\"meterId\":\"" + meterId + "\",\"energyKwh\":" + energyKwh +
                ",\"capturedAt\":\"" + Instant.now() + "\"}";
        StreamProcessor processor = new RawMeterStream();
        for (String decorator : selected) {
            processor = switch (decorator) {
                case "Compression" -> new CompressionDecorator(processor);
                case "Encryption" -> new EncryptionDecorator(processor);
                case "Authentication" -> new AuthenticationDecorator(processor);
                default -> throw new IllegalArgumentException("Unsupported stream decorator: " + decorator);
            };
        }
        String processed = Base64.getEncoder().encodeToString(processor.process(original.getBytes(StandardCharsets.UTF_8)));
        String chain = selected.isEmpty() ? "None" : String.join(" → ", selected);
        return repository.save(meterId, chain, original, processed);
    }

    public List<StreamRun> recentRuns() {
        return repository.findRecent(20);
    }
}
