function parseMeterId(idStr) {
    if (!idStr) return {};
    const tags = {};
    const tagRegex = /Tag\(key=([^,]+),\s*value=([^)]*)\)/g;
    let match;
    let found = false;
    while ((match = tagRegex.exec(idStr)) !== null) {
        tags[match[1].trim()] = match[2].trim();
        found = true;
    }
    if (!found) {
        const simpleRegex = /([a-zA-Z0-9_.-]+)=([^, \[\](){}]+)/g;
        while ((match = simpleRegex.exec(idStr)) !== null) {
            tags[match[1].trim()] = match[2].trim();
        }
    }
    return tags;
}

function getMetricTotal(metrics, key) {
    if (!metrics) return 0;
    const m = metrics[key];
    if (!m || !m.values) return 0;
    return Object.values(m.values).reduce((acc, v) => acc + (Number(v) || 0), 0);
}

function getMetricValue(metrics, key) {
    if (!metrics) return 0;
    const m = metrics[key];
    if (!m || !m.values) return 0;
    return Object.values(m.values).reduce((acc, v) => acc + (Number(v) || 0), 0);
}

function getSuccessRateClass(rate) {
    if (rate >= 0.95) return 'success-green';
    if (rate >= 0.80) return 'success-yellow';
    return 'success-red';
}

function findNodeInData(data, id, type) {
    if (!data) return null;
    if (type === 'init' && data.init) return { name: data.init.id || 'Initializer', type: 'init', data: data.init };
    if (type === 'step' && data.steps) {
        const step = data.steps.find(s => s.id === id);
        if (step) return { name: step.id, type: 'step', data: step };
    }
    if (type === 'sink' && data.sinks) {
        const sink = data.sinks.find(s => s.id === id);
        if (sink) return { name: sink.id, type: 'sink', data: sink };
    }
    return null;
}

function computeBreakdowns(metric, tagKey) {
    if (!metric || !metric.values || Object.keys(metric.values).length === 0) return [];
    
    const groups = new Map();
    const ignoredTags = new Set(['pipeline', 'step', 'sink', 'initializer', 'component.family', tagKey]);

    Object.entries(metric.values).forEach(([idStr, rawValue]) => {
        const val = Number(rawValue) || 0;
        const allTags = parseMeterId(idStr);
        let label = allTags[tagKey];
        if (!label) {
            const tagPattern = new RegExp(`Tag\\(key=${tagKey}, value=([^\\)]+)\\)`);
            const match = idStr.match(tagPattern);
            if (match) {
                label = match[1];
            } else {
                const simplePattern = new RegExp(`${tagKey}=([^, \\]\\)]+)`);
                const simpleMatch = idStr.match(simplePattern);
                if (simpleMatch) label = simpleMatch[1];
            }
        }
        if (!label) label = 'unknown';

        const secondaryTags = [];
        Object.entries(allTags).forEach(([k, v]) => {
            if (!ignoredTags.has(k)) {
                secondaryTags.push({ key: k, value: v });
            }
        });

        if (!groups.has(label)) {
            groups.set(label, { label: label, total: 0, breakdowns: [] });
        }
        const grp = groups.get(label);
        grp.total += val;
        grp.breakdowns.push({ tags: secondaryTags, value: val });
    });

    const sortedGroups = Array.from(groups.values()).sort((a, b) => b.total - a.total);
    const max = Math.max(...sortedGroups.map(g => g.total), 1);
    
    sortedGroups.forEach(g => {
        g.percentage = max > 0 ? (g.total / max) * 100 : 0;
        g.hasSecondaryTags = g.breakdowns.some(b => b.tags.length > 0);
        g.breakdowns.sort((a, b) => b.value - a.value);
    });

    return sortedGroups;
}
