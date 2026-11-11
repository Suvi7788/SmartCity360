package lk.jiat.smartcity360.securitystream.dao;

import lk.jiat.smartcity360.securitystream.model.StreamRun;

import java.util.List;

public interface StreamRunRepository {
    long save(String meterId, String decoratorChain, String originalPayload, String processedPayload);

    List<StreamRun> findRecent(int limit);
}
