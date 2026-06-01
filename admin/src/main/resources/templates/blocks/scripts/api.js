function fetchKpis() {
    fetch(apiPath + '/kpi')
        .then(r => r.json())
        .then(kpis => {
            const pipelinesEl = document.getElementById('kpi-pipelines');
            const runsEl = document.getElementById('kpi-runs');
            const successEl = document.getElementById('kpi-success');
            if (pipelinesEl) pipelinesEl.innerText = kpis.pipelineCount;
            if (runsEl) runsEl.innerText = kpis.totalRuns.toLocaleString();
            if (successEl) {
                const rate = (kpis.successRate * 100).toFixed(2) + '%';
                successEl.innerText = rate;
                successEl.className = 'kpi-value ' + getSuccessRateClass(kpis.successRate);
            }
        });
}

function fetchPipelines() {
    fetchKpis();
    fetch(apiPath + '/pipeline')
        .then(r => r.json())
        .then(pipelines => {
            const list = document.getElementById('pipeline-list');
            if (!list) return;
            list.innerHTML = '';
            pipelines.forEach(p => {
                const tr = document.createElement('tr');
                tr.className = 'clickable-row';
                const total = getMetricValue(p.metrics, 'pipeline.run.total');
                const success = getMetricValue(p.metrics, 'pipeline.run.success');
                const error = getMetricValue(p.metrics, 'pipeline.run.error.total');
                const rate = total > 0 ? success / total : 0;
                
                tr.innerHTML = `
                    <td style="font-weight: 600" class="mono">${p.id}</td>
                    <td><span class="badge badge-total">${total}</span></td>
                    <td><span class="badge badge-success">${success}</span></td>
                    <td><span class="badge badge-error">${error}</span></td>
                    <td><span class="${getSuccessRateClass(rate)}">${(rate * 100).toFixed(1)}%</span></td>
                    <td style="text-align: right; color: var(--nasa-orange);">→</td>
                `;
                tr.onclick = () => navigateToPipeline(p.id);
                list.appendChild(tr);
            });
        });
}

function checkStatus() {
    fetch(apiPath + '/ping')
        .then(r => {
            const statusEl = document.getElementById('status');
            if (!statusEl) return;
            if (r.ok) {
                statusEl.innerText = 'Online';
                statusEl.classList.remove('offline');
            } else {
                throw new Error();
            }
        })
        .catch(() => {
            const statusEl = document.getElementById('status');
            if (statusEl) {
                statusEl.innerText = 'Offline';
                statusEl.classList.add('offline');
            }
        });
}
