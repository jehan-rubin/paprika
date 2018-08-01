package paprika.analyse.metrics;

import paprika.analyse.entities.Entity;

public abstract class Condition<T, E extends Entity> {

    private String metricName;

    public Condition(String metricName) {
        this.metricName = metricName;
    }

    public abstract boolean matches(T sootItem);

    public boolean createIfMatching(T sootItem, E entity) {
        UnaryMetric<Boolean> metric = new UnaryMetric<>(metricName, entity, matches(sootItem));
        metric.updateEntity();
        return matches(sootItem);
    }
}
