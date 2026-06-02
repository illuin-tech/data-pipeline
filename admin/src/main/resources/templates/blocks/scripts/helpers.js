function getMetricTotal(metrics, key) {
    if (!metrics) return 0;
    const m = metrics[key];
    if (!m || !m.values) return 0;
    return Object.values(m.values).reduce((acc, v) => acc + v, 0);
}

function getMetricValue(metrics, key) {
    return getMetricTotal(metrics, key);
}

function getSuccessRateClass(rate) {
    if (rate > 0.99) return 'success-green';
    if (rate > 0.90) return 'success-yellow';
    if (rate > 0.80) return 'success-orange';
    return 'success-red';
}
