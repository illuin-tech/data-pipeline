function renderBarChart(metric, tagKey, isError = false) {
    if (!metric || !metric.values || Object.keys(metric.values).length === 0) return '<div style="font-size: 0.7rem; color: var(--text-dim);">No data</div>';
    
    const entries = Object.entries(metric.values).map(([idStr, value]) => {
        let label = 'unknown';
        const tagPattern = new RegExp(`Tag\\(key=${tagKey}, value=([^\\)]+)\\)`);
        const match = idStr.match(tagPattern);
        if (match) {
            label = match[1];
        } else {
            const simplePattern = new RegExp(`${tagKey}=([^, \\]\\)]+)`);
            const simpleMatch = idStr.match(simplePattern);
            if (simpleMatch) label = simpleMatch[1];
        }

        return { label, value };
    });

    const sortedEntries = entries.sort((a, b) => b.value - a.value);
    const max = Math.max(...entries.map(e => e.value));
    
    let html = '<div class="bar-chart">';
    sortedEntries.forEach(e => {
        const percentage = max > 0 ? (e.value / max) * 100 : 0;
        html += `
            <div class="bar-row">
                <div class="bar-label-container">
                    <span class="bar-label">${e.label}</span>
                    <span class="bar-count">${e.value.toLocaleString()}</span>
                </div>
                <div class="bar-container">
                    <div class="bar-fill ${isError ? 'error-bar' : ''}" style="width: ${percentage}%"></div>
                </div>
            </div>
        `;
    });
    html += '</div>';
    return html;
}

function renderNodeDetails(node, scroll = true) {
    selectedNodeId = node.type === 'init' ? 'init' : node.name;
    selectedNodeType = node.type;
    const container = document.getElementById('component-details');
    if (!container) return;
    const data = node.data;
    
    let typeColor = 'var(--nasa-blue)';
    if (node.type === 'step') typeColor = '#9c27b0';
    if (node.type === 'sink') typeColor = 'var(--success-color)';

    const javaType = data.step || data.sink || data.initializer || 'Unknown';
    
    const modifiers = [];
    if (data.isAsync) modifiers.push({ label: 'Async', value: null });
    if (data.pinned) modifiers.push({ label: 'Pinned', value: null });
    
    const addMod = (label, val) => {
        if (val) modifiers.push({ label: label, value: typeof val === 'string' ? val : (val.type || JSON.stringify(val)) });
    };
    
    addMod('Wrapper', data.executionWrapper);
    addMod('Error Handler', data.errorHandler);
    addMod('Condition', data.condition);
    addMod('Evaluator', data.resultEvaluator);

    const prefix = node.type === 'init' ? 'pipeline.initialization' : `pipeline.${node.type}`;
    const total = getMetricTotal(data.metrics, `${prefix}.run.total`);
    const success = getMetricTotal(data.metrics, `${prefix}.run.success`);
    const failure = getMetricTotal(data.metrics, `${prefix}.run.failure`);
    const rate = total > 0 ? (success / total * 100).toFixed(1) : 0;

    const resultsMetric = data.metrics[`pipeline.step.result.total`];
    const errorsMetric = data.metrics[`${prefix}.error.total`];

    container.innerHTML = `
        <div class="id-card">
            <div class="id-card-header">
                <div class="id-card-dot" style="background: ${typeColor}"></div>
                <div class="id-card-title mono">${node.name}</div>
            </div>
            
            <div class="component-details-layout">
                <div class="details-left">
                    <div class="id-card-field">
                        <div class="id-card-label">Full Java Type</div>
                        <div class="id-card-value mono" style="word-break: break-all; font-size: 0.7rem;">${typeof javaType === 'string' ? javaType : JSON.stringify(javaType)}</div>
                    </div>
                    <div class="id-card-field">
                        <div class="id-card-label">Applied Modifiers</div>
                        <div class="id-card-modifiers" style="flex-direction: column; align-items: stretch; gap: 8px;">
                            ${modifiers.length > 0 ? modifiers.map(m => `
                                <div style="display: flex; flex-direction: column; gap: 4px;">
                                    <span class="modifier-tag" style="align-self: flex-start;">${m.label}</span>
                                    ${m.value ? `<div class="mono" style="font-size: 0.7rem; color: var(--text-color); word-break: break-all; padding-left: 8px; border-left: 1px solid var(--nasa-orange); margin-left: 4px;">${m.value}</div>` : ''}
                                </div>
                            `).join('') : '<span style="color: var(--text-dim); font-size: 0.75rem;">None</span>'}
                        </div>
                    </div>
                </div>

                <div class="details-right">
                    <div class="id-card-metrics" style="margin-top: 0; padding-top: 0; border-top: none;">
                        <div class="metrics-group">
                            <div class="metric-card">
                                <div class="metric-name">Runs</div>
                                <div class="metric-value">${total}</div>
                            </div>
                            <div class="metric-card">
                                <div class="metric-name">Success</div>
                                <div class="metric-value success">${success}</div>
                            </div>
                            <div class="metric-card">
                                <div class="metric-name">Failure</div>
                                <div class="metric-value error">${failure}</div>
                            </div>
                            <div class="metric-card">
                                <div class="metric-name">Rate</div>
                                <div class="metric-value ${getSuccessRateClass(success/total)}">${rate}%</div>
                            </div>
                        </div>
                    </div>

                    ${node.type === 'step' ? `
                        <div class="id-card-field">
                            <div class="id-card-label">Result Breakdown</div>
                            ${renderBarChart(resultsMetric, 'result')}
                        </div>
                    ` : ''}

                    <div class="id-card-field">
                        <div class="id-card-label">Error Breakdown</div>
                        ${renderBarChart(errorsMetric, 'error', true)}
                    </div>
                </div>
            </div>
        </div>
        <details>
            <summary>Raw Component Data (JSON)</summary>
            <pre style="max-height: 300px;">${JSON.stringify(data, null, 2)}</pre>
        </details>
    `;
    if (scroll) container.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function renderPipelineMetrics(metrics) {
    const container = document.getElementById('pipeline-metrics');
    if (!container) return;
    if (!metrics || Object.keys(metrics).length === 0) {
        container.innerHTML = 'No metrics available';
        return;
    }

    const total = getMetricTotal(metrics, 'pipeline.run.total');
    const success = getMetricTotal(metrics, 'pipeline.run.success');
    const failure = getMetricTotal(metrics, 'pipeline.run.failure');
    const errors = getMetricTotal(metrics, 'pipeline.run.error.total');

    let html = `
        <div class="metrics">
            <div class="metrics-group">
                <div class="metric-card">
                    <div class="metric-name">Total Runs</div>
                    <div class="metric-value">${total}</div>
                </div>
                <div class="metric-card">
                    <div class="metric-name">Success</div>
                    <div class="metric-value success">${success}</div>
                </div>
                <div class="metric-card">
                    <div class="metric-name">Failure</div>
                    <div class="metric-value error">${failure}</div>
                </div>
                <div class="metric-card">
                    <div class="metric-name">System Errors</div>
                    <div class="metric-value error">${errors}</div>
                </div>
            </div>
            <div class="metrics-group">
    `;

    const skipKeys = ['pipeline.run.total', 'pipeline.run.success', 'pipeline.run.failure', 'pipeline.run.error.total', 'pipeline.run'];
    Object.entries(metrics).forEach(([key, metric]) => {
        if (skipKeys.some(sk => key.startsWith(sk))) return;
        const val = getMetricTotal(metrics, key);
        html += `
            <div class="metric-card">
                <div class="metric-name">${key}</div>
                <div class="metric-value">${val}</div>
            </div>
        `;
    });

    html += '</div></div>';
    container.innerHTML = html;
}

function renderComponentTable(data) {
    const list = document.getElementById('component-list');
    if (!list) return;
    list.innerHTML = '';
    
    const components = [];
    if (data.init) components.push({type: 'init', data: data.init});
    if (data.steps) data.steps.forEach(s => components.push({type: 'step', data: s}));
    if (data.sinks) data.sinks.forEach(s => components.push({type: 'sink', data: s}));
    
    components.forEach(c => {
        const tr = document.createElement('tr');
        tr.className = 'clickable-row';
        
        const prefix = c.type === 'init' ? 'pipeline.initialization' : `pipeline.${c.type}`;
        const total = getMetricValue(c.data.metrics, `${prefix}.run.total`);
        const success = getMetricValue(c.data.metrics, `${prefix}.run.success`);
        const error = getMetricValue(c.data.metrics, `${prefix}.error.total`);
        const rate = total > 0 ? success / total : 0;
        
        tr.innerHTML = `
            <td><span class="indicator indicator-${c.type}"></span><span style="text-transform: capitalize">${c.type}</span></td>
            <td style="font-weight: 600" class="mono">${c.data.id}</td>
            <td><span class="badge badge-total">${total}</span></td>
            <td><span class="badge badge-success">${success}</span></td>
            <td><span class="badge badge-error">${error}</span></td>
            <td><span class="${getSuccessRateClass(rate)}">${(rate * 100).toFixed(1)}%</span></td>
        `;
        tr.onclick = () => renderNodeDetails({name: c.data.id, type: c.type, data: c.data});
        list.appendChild(tr);
    });
}
