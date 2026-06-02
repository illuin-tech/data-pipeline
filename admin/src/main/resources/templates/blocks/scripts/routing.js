function navigateToList() {
    window.location.href = rootPath + '/';
}

function navigateToPipeline(id) {
    window.location.href = rootPath + '/' + id;
}

function showList() {
    const listView = document.getElementById('pipeline-list-view');
    if (listView) listView.style.display = 'block';
    const pipelineView = document.getElementById('pipeline-view');
    if (pipelineView) pipelineView.style.display = 'none';
}

function showPipeline(id) {
    currentPipelineId = id;
    const listView = document.getElementById('pipeline-list-view');
    if (listView) listView.style.display = 'none';
    const pipelineView = document.getElementById('pipeline-view');
    if (pipelineView) pipelineView.style.display = 'block';

    const titleEl = document.getElementById('pipeline-title');
    if (titleEl) titleEl.innerText = id;
    
    refreshPipeline(id, true);
}

function refreshPipeline(id, firstLoad = false) {
    if (!id) return;
    
    fetch(apiPath + '/kpi/' + id)
        .then(r => r.json())
        .then(kpis => {
            const runsEl = document.getElementById('pipe-kpi-runs');
            const successEl = document.getElementById('pipe-kpi-success');
            if (runsEl) runsEl.innerText = kpis.totalRuns.toLocaleString();
            if (successEl) {
                successEl.innerText = (kpis.successRate * 100).toFixed(2) + '%';
                successEl.className = 'kpi-value ' + getSuccessRateClass(kpis.successRate);
            }
        });

    fetch(apiPath + '/pipeline/' + id)
        .then(r => r.json())
        .then(data => {
            if (firstLoad) {
                renderGraph(data);
            }
            renderComponentTable(data);
            renderPipelineMetrics(data.metrics);
            
            if (selectedNodeId) {
                const node = findNodeInData(data, selectedNodeId, selectedNodeType);
                if (node) {
                    renderNodeDetails(node, false);
                }
            }
        });
}

function findNodeInData(data, id, type) {
    if (type === 'init' && data.init) return {name: data.init.id || 'Initializer', type: 'init', data: data.init};
    if (type === 'step' && data.steps) {
        const step = data.steps.find(s => s.id === id);
        if (step) return {name: step.id, type: 'step', data: step};
    }
    if (type === 'sink' && data.sinks) {
        const sink = data.sinks.find(s => s.id === id);
        if (sink) return {name: sink.id, type: 'sink', data: sink};
    }
    return null;
}
