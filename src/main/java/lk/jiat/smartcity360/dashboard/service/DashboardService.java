package lk.jiat.smartcity360.dashboard.service;

import lk.jiat.smartcity360.dashboard.dao.DashboardRepository;
import lk.jiat.smartcity360.dashboard.model.DashboardStats;

public final class DashboardService {
    private final DashboardRepository repository;

    public DashboardService(DashboardRepository repository) {
        this.repository = repository;
    }

    public DashboardStats loadStats() {
        return repository.loadStats();
    }
}
