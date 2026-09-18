function renderPipelineGraph(containerId, data, onNodeSelect) {
    const container = document.getElementById(containerId);
    if (!container || !data) return;
    const width = container.clientWidth || 800;
    const height = 300;
    d3.select('#' + containerId).selectAll('*').remove();
    const svg = d3.select('#' + containerId).append('svg')
        .attr('width', width).attr('height', height);
    
    const zoomLayer = svg.append('g');
    const zoom = d3.zoom()
        .scaleExtent([0.1, 3])
        .on('zoom', (event) => zoomLayer.attr('transform', event.transform));
    
    svg.call(zoom);

    const controls = d3.select('#' + containerId).append('div').attr('class', 'zoom-controls');
    controls.append('button').attr('class', 'zoom-btn').html('+').on('click', () => svg.transition().call(zoom.scaleBy, 1.3));
    controls.append('button').attr('class', 'zoom-btn').html('-').on('click', () => svg.transition().call(zoom.scaleBy, 0.7));

    const init = data.init ? { id: 'init', name: data.init.id || 'Initializer', type: 'init', data: data.init } : null;
    const steps = (data.steps || []).map(s => ({ id: s.id, name: s.id, type: 'step', data: s }));
    const sinks = (data.sinks || []).map(s => ({ id: s.id, name: s.id, type: 'sink', data: s }));

    const computeWidth = (n) => {
        const textWidth = n.name.length * 8;
        return Math.min(240, Math.max(120, textWidth + 50));
    };
    if (init) init.width = computeWidth(init);
    steps.forEach(s => s.width = computeWidth(s));
    sinks.forEach(s => s.width = computeWidth(s));

    const nodes = [];
    const links = [];

    let horizontalStages = (init ? 1 : 0) + steps.length + (sinks.length > 0 ? 1 : 0);
    if (horizontalStages === 0) return;

    const paddingX = 100;
    const gapX = 150;
    let currentX = paddingX;

    if (init) {
        init.x = currentX + init.width / 2;
        init.y = height / 2;
        nodes.push(init);
        currentX += init.width + gapX;
    }
    
    steps.forEach((s, i) => {
        s.x = currentX + s.width / 2;
        s.y = height / 2;
        nodes.push(s);
        if (i === 0 && init) links.push({ source: init, target: s, isAsync: false });
        else if (i > 0) links.push({ source: steps[i-1], target: s, isAsync: false });
        currentX += s.width + gapX;
    });

    if (sinks.length > 0) {
        const source = steps.length > 0 ? steps[steps.length - 1] : init;

        const paddingY = 40;
        const stepY = sinks.length > 1 ? (height - paddingY * 2) / (sinks.length - 1) : 0;
        let maxSinkWidth = 0;
        sinks.forEach((s, i) => {
            s.x = currentX + s.width / 2;
            s.y = sinks.length > 1 ? paddingY + stepY * i : height / 2;
            nodes.push(s);
            if (source) links.push({ source: source, target: s, isAsync: s.data.isAsync });
            maxSinkWidth = Math.max(maxSinkWidth, s.width);
        });
        currentX += maxSinkWidth + gapX;
    }

    const contentWidth = currentX - gapX + paddingX;
    const initialScale = Math.min(1.2, Math.max(0.75, (width - 40) / contentWidth));
    const fitScale = Math.min(1, (width - 40) / contentWidth);
    const centerX = (width - contentWidth * fitScale) / 2;
    const centerY = (height - height * fitScale) / 2;
    
    svg.call(zoom.transform, d3.zoomIdentity.translate(40, (height - height * initialScale) / 2).scale(initialScale));
    
    controls.append('button')
        .attr('class', 'zoom-btn')
        .attr('title', 'Fit to View')
        .html('⟲')
        .on('click', () => {
            svg.transition().duration(750).call(zoom.transform, d3.zoomIdentity.translate(centerX, centerY).scale(fitScale));
        });

    const defs = svg.append('defs');
    const createArrow = (id) => {
        defs.append('marker')
            .attr('id', id)
            .attr('viewBox', '-0 -5 10 10')
            .attr('refX', 10)
            .attr('refY', 0)
            .attr('orient', 'auto')
            .attr('markerWidth', 6)
            .attr('markerHeight', 6)
            .append('svg:path')
            .attr('d', 'M 0,-5 L 10 ,0 L 0,5')
            .attr('fill', 'var(--nasa-grey)');
    };
    createArrow('arrowhead-sync');
    createArrow('arrowhead-async');

    zoomLayer.append('g').selectAll('path').data(links).enter().append('path')
        .attr('class', d => 'link ' + (d.isAsync ? 'async' : 'sync'))
        .attr('marker-end', d => d.isAsync ? 'url(#arrowhead-async)' : 'url(#arrowhead-sync)')
        .attr('d', d => {
            const s = d.source;
            const t = d.target;
            const x1 = s.x + s.width / 2;
            const y1 = s.y;
            const x2 = t.x - t.width / 2;
            const y2 = t.y;
            const cx = (x1 + x2) / 2;
            return `M${x1},${y1} C${cx},${y1} ${cx},${y2} ${x2},${y2}`;
        });

    const node = zoomLayer.append('g').selectAll('g').data(nodes).enter().append('g')
        .attr('class', d => 'node ' + d.type)
        .attr('transform', d => `translate(${d.x},${d.y})`)
        .on('click', (event, d) => {
            if (typeof onNodeSelect === 'function') {
                onNodeSelect(d);
            }
        });

    node.append('rect')
        .attr('x', d => -d.width / 2).attr('y', -20)
        .attr('width', d => d.width).attr('height', 40)
        .attr('rx', 2).attr('ry', 2);

    const getIconPath = (d) => {
        const left = -d.width / 2;
        const icons = {
            'init': `M${left+10},-7 L${left+10},7 L${left+22},0 Z`,
            'step': `M${left+8},0 L${left+12},-4 L${left+16},4 L${left+20},-4 L${left+24},4 L${left+28},0`,
            'sink': `M${left+10},-4 L${left+26},-4 L${left+18},6 Z`
        };
        return icons[d.type];
    };
    
    node.append('path')
        .attr('class', 'node-icon')
        .attr('d', d => getIconPath(d));

    node.append('text')
        .attr('dx', 10)
        .attr('dy', 5)
        .attr('text-anchor', 'middle')
        .style('font-family', 'monospace')
        .style('font-size', '12px')
        .text(d => {
            const maxChars = Math.floor((d.width - 50) / 8);
            return d.name.length > maxChars ? d.name.substring(0, maxChars - 3) + '...' : d.name;
        });
}

function pipelineDetails(pipelineId) {
    return {
        routes,
        navigateTo,
        pipelineId,
        kpis: {
            totalRuns: 0,
            successRate: 0
        },
        data: null,
        components: [],
        selectedNode: null,
        selectedNodeId: null,
        selectedNodeType: null,
        loading: true,
        _pollTimer: null,
        _graphRendered: false,

        async init() {
            await this.refresh();
            this.loading = false;
            if (!this._graphRendered && this.data) {
                renderPipelineGraph('pipeline-graph', this.data, (node) => this.selectNode(node, true));
                this._graphRendered = true;
            }
            if (this._pollTimer) clearInterval(this._pollTimer);
            this._pollTimer = setInterval(() => this.refresh(), 15000);
        },

        async refresh() {
            try {
                const [kpis, data] = await Promise.all([
                    fetchPipelineKpis(this.pipelineId),
                    fetchPipelineDetails(this.pipelineId)
                ]);
                this.kpis = kpis;
                this.data = data;
                this.updateComponents();
                if (this.selectedNodeId) {
                    const updated = findNodeInData(this.data, this.selectedNodeId, this.selectedNodeType);
                    if (updated) this.selectedNode = updated;
                }
            } catch (e) {
                console.error('Failed to refresh pipeline details', e);
            }
        },

        destroy() {
            if (this._pollTimer) {
                clearInterval(this._pollTimer);
                this._pollTimer = null;
            }
        },

        updateComponents() {
            if (!this.data) {
                this.components = [];
                return;
            }
            const list = [];
            if (this.data.init) {
                list.push({ type: 'init', name: this.data.init.id || 'Initializer', data: this.data.init });
            }
            if (this.data.steps) {
                this.data.steps.forEach(s => list.push({ type: 'step', name: s.id, data: s }));
            }
            if (this.data.sinks) {
                this.data.sinks.forEach(s => list.push({ type: 'sink', name: s.id, data: s }));
            }
            this.components = list;
        },

        selectNode(node, scroll = false) {
            this.selectedNodeId = node.type === 'init' ? 'init' : (node.data.id || node.name);
            this.selectedNodeType = node.type;
            this.selectedNode = node;
            if (scroll) {
                setTimeout(() => {
                    const el = document.getElementById('component-details-pane');
                    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
                }, 50);
            }
        },

        getSuccessRateFormatted(rate) {
            return ((rate || 0) * 100).toFixed(2) + '%';
        },

        getPipelineRunTotal() {
            return this.data && this.data.metrics ? getMetricTotal(this.data.metrics, 'pipeline.run.total') : 0;
        },

        getPipelineRunSuccess() {
            return this.data && this.data.metrics ? getMetricTotal(this.data.metrics, 'pipeline.run.success') : 0;
        },

        getPipelineRunFailure() {
            return this.data && this.data.metrics ? getMetricTotal(this.data.metrics, 'pipeline.run.failure') : 0;
        },

        getPipelineRunError() {
            return this.data && this.data.metrics ? getMetricTotal(this.data.metrics, 'pipeline.run.error.total') : 0;
        },

        getCustomMetrics() {
            if (!this.data || !this.data.metrics) return [];
            const skipKeys = ['pipeline.run.total', 'pipeline.run.success', 'pipeline.run.failure', 'pipeline.run.error.total', 'pipeline.run'];
            const list = [];
            Object.entries(this.data.metrics).forEach(([key, metric]) => {
                if (skipKeys.some(sk => key.startsWith(sk))) return;
                const val = getMetricTotal(this.data.metrics, key);
                list.push({ key: key, value: val });
            });
            return list;
        },

        getComponentStats(comp) {
            if (!comp || !comp.data) return { total: 0, success: 0, failure: 0, error: 0, rate: 0, rateClass: 'success-red', rateFormatted: '0.0%' };
            const prefix = comp.type === 'init' ? 'pipeline.initialization' : `pipeline.${comp.type}`;
            const total = getMetricTotal(comp.data.metrics, `${prefix}.run.total`);
            const success = getMetricTotal(comp.data.metrics, `${prefix}.run.success`);
            const failure = getMetricTotal(comp.data.metrics, `${prefix}.run.failure`);
            const error = getMetricTotal(comp.data.metrics, `${prefix}.error.total`);
            const rate = total > 0 ? success / total : 0;
            return {
                total,
                success,
                failure,
                error,
                rate,
                rateClass: getSuccessRateClass(rate),
                rateFormatted: (rate * 100).toFixed(1) + '%'
            };
        },

        getComponentJavaType(data) {
            if (!data) return 'Unknown';
            const t = data.step || data.sink || data.initializer || 'Unknown';
            return typeof t === 'string' ? t : JSON.stringify(t);
        },

        getComponentTypeColor(type) {
            if (type === 'step') return '#9c27b0';
            if (type === 'sink') return 'var(--success-color)';
            return 'var(--nasa-blue)';
        },

        getComponentModifiers(data) {
            if (!data) return [];
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
            return modifiers;
        },

        getNodeResultMetric(node) {
            if (!node || !node.data || !node.data.metrics) return null;
            return node.data.metrics['pipeline.step.result.total'] || null;
        },

        getNodeErrorMetric(node) {
            if (!node || !node.data || !node.data.metrics) return null;
            const prefix = node.type === 'init' ? 'pipeline.initialization' : `pipeline.${node.type}`;
            return node.data.metrics[`${prefix}.error.total`] || null;
        },

        getRawJson(data) {
            return JSON.stringify(data, null, 2);
        }
    };
}
