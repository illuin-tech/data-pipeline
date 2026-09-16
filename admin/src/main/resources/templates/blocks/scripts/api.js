async function fetchGlobalKpis() {
    const response = await fetch(routes.api.kpi());
    if (!response.ok) throw new Error('Failed to fetch global KPIs');
    return await response.json();
}

async function fetchPipelinesList() {
    const response = await fetch(routes.api.pipelines());
    if (!response.ok) throw new Error('Failed to fetch pipelines');
    return await response.json();
}

async function fetchPipelineKpis(id) {
    const response = await fetch(routes.api.pipelineKpi(id));
    if (!response.ok) throw new Error('Failed to fetch pipeline KPIs');
    return await response.json();
}

async function fetchPipelineDetails(id) {
    const response = await fetch(routes.api.pipeline(id));
    if (!response.ok) throw new Error('Failed to fetch pipeline details');
    return await response.json();
}

async function pingServer() {
    try {
        const response = await fetch(routes.api.ping());
        return response.ok;
    } catch (e) {
        return false;
    }
}

async function checkStatus() {
    const isOnline = await pingServer();
    const statusEl = document.getElementById('status');
    if (statusEl) {
        if (isOnline) {
            statusEl.innerText = 'Online';
            statusEl.classList.remove('offline');
        } else {
            statusEl.innerText = 'Offline';
            statusEl.classList.add('offline');
        }
    }
    return isOnline;
}
