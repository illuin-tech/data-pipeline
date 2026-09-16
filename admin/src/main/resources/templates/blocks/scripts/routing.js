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

function navigateTo(url) {
    if (typeof url !== 'string') {
        throw new Error('navigateTo expects a string URL');
    }
    window.location.href = url;
}
