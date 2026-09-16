function adminDashboard() {
    return {
        routes,
        navigateTo,
        kpis: {
            pipelineCount: 0,
            totalRuns: 0,
            successRate: 0
        },
        pipelines: [],
        loading: true,
        _pollTimer: null,

        async init() {
            await this.refresh();
            this.loading = false;
            if (this._pollTimer) clearInterval(this._pollTimer);
            this._pollTimer = setInterval(() => this.refresh(), 5000);
        },

        async refresh() {
            try {
                const [kpis, pipelines] = await Promise.all([
                    fetchGlobalKpis(),
                    fetchPipelinesList()
                ]);
                this.kpis = kpis;
                this.pipelines = pipelines;
            } catch (e) {
                console.error('Failed to refresh dashboard data', e);
            }
        },

        destroy() {
            if (this._pollTimer) {
                clearInterval(this._pollTimer);
                this._pollTimer = null;
            }
        },

        getPipelineTotal(p) {
            return getMetricTotal(p.metrics, 'pipeline.run.total');
        },

        getPipelineSuccess(p) {
            return getMetricTotal(p.metrics, 'pipeline.run.success');
        },

        getPipelineError(p) {
            return getMetricTotal(p.metrics, 'pipeline.run.error.total');
        },

        getPipelineRate(p) {
            const total = this.getPipelineTotal(p);
            const success = this.getPipelineSuccess(p);
            return total > 0 ? success / total : 0;
        },

        getPipelineRateClass(p) {
            return getSuccessRateClass(this.getPipelineRate(p));
        },

        getSuccessRateFormatted(rate) {
            return ((rate || 0) * 100).toFixed(2) + '%';
        },

        getPipelineSuccessRateFormatted(p) {
            return (this.getPipelineRate(p) * 100).toFixed(1) + '%';
        }
    };
}
