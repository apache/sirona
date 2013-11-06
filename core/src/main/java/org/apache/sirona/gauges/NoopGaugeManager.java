package org.apache.sirona.gauges;

// useful in pull mode
public class NoopGaugeManager implements GaugeManager {
    @Override
    public void stop() {
        // no-op
    }

    @Override
    public void addGauge(final Gauge gauge) {
        // no-op
    }

    @Override
    public void stopGauge(final Gauge role) {
        // no-op
    }
}
