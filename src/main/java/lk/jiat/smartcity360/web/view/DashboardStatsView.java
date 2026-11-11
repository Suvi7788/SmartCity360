package lk.jiat.smartcity360.web.view;

import lk.jiat.smartcity360.dashboard.model.DashboardStats;

public final class DashboardStatsView {
    private final long streamRuns;
    private final long zones;
    private final long openComplaints;
    private final long streetlights;
    private final long activePolicies;

    public DashboardStatsView(DashboardStats stats) {
        this.streamRuns = stats.streamRuns();
        this.zones = stats.zones();
        this.openComplaints = stats.openComplaints();
        this.streetlights = stats.streetlights();
        this.activePolicies = stats.activePolicies();
    }

    public long getStreamRuns() {
        return streamRuns;
    }

    public long getZones() {
        return zones;
    }

    public long getOpenComplaints() {
        return openComplaints;
    }

    public long getStreetlights() {
        return streetlights;
    }

    public long getActivePolicies() {
        return activePolicies;
    }
}
