const routes = {
    list: () => rootPath + '/',
    pipeline: (id) => rootPath + '/' + encodeURIComponent(id),
    api: {
        kpi: () => apiPath + '/kpi',
        pipelines: () => apiPath + '/pipeline',
        pipelineKpi: (id) => apiPath + '/kpi/' + encodeURIComponent(id),
        pipeline: (id) => apiPath + '/pipeline/' + encodeURIComponent(id),
        ping: () => apiPath + '/ping'
    }
};

function navigateToList() {
    window.location.href = routes.list();
}

function navigateToPipeline(id) {
    window.location.href = routes.pipeline(id);
}
