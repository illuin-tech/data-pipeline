function renderGraph(data) {
    const container = document.getElementById('pipeline-graph');
    if (!container) return;
    const width = container.clientWidth || 800;
    const height = 300;
    d3.select("#pipeline-graph").selectAll("*").remove();
    const svg = d3.select("#pipeline-graph").append("svg")
        .attr("width", width).attr("height", height);
    
    const zoomLayer = svg.append("g");
    const zoom = d3.zoom()
        .scaleExtent([0.1, 3])
        .on("zoom", (event) => zoomLayer.attr("transform", event.transform));
    
    svg.call(zoom);

    const controls = d3.select("#pipeline-graph").append("div").attr("class", "zoom-controls");
    controls.append("button").attr("class", "zoom-btn").html("+").on("click", () => svg.transition().call(zoom.scaleBy, 1.3));
    controls.append("button").attr("class", "zoom-btn").html("-").on("click", () => svg.transition().call(zoom.scaleBy, 0.7));

    const init = data.init ? {id: 'init', name: data.init.id || 'Initializer', type: 'init', data: data.init} : null;
    const steps = (data.steps || []).map(s => ({id: s.id, name: s.id, type: 'step', data: s}));
    const sinks = (data.sinks || []).map(s => ({id: s.id, name: s.id, type: 'sink', data: s}));

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
        if (i === 0 && init) links.push({source: init, target: s, isAsync: false});
        else if (i > 0) links.push({source: steps[i-1], target: s, isAsync: false});
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
            if (source) links.push({source: source, target: s, isAsync: s.data.isAsync});
            maxSinkWidth = Math.max(maxSinkWidth, s.width);
        });
        currentX += maxSinkWidth + gapX;
    }

    const contentWidth = currentX - gapX + paddingX;
    const initialScale = Math.min(1.2, Math.max(0.75, (width - 40) / contentWidth));
    const fitScale = Math.min(1, (width - 40) / contentWidth);
    const centerX = (width - contentWidth * fitScale) / 2;
    const centerY = (height - height * fitScale) / 2;
    
    // Start from the left
    svg.call(zoom.transform, d3.zoomIdentity.translate(40, (height - height * initialScale) / 2).scale(initialScale));
    
    controls.append("button")
        .attr("class", "zoom-btn")
        .attr("title", "Fit to View")
        .html("⟲")
        .on("click", () => {
            svg.transition().duration(750).call(zoom.transform, d3.zoomIdentity.translate(centerX, centerY).scale(fitScale));
        });

    const defs = svg.append("defs");
    defs.append("marker")
        .attr("id", "arrowhead-sync")
        .attr("viewBox", "-0 -5 10 10")
        .attr("refX", 10)
        .attr("refY", 0)
        .attr("orient", "auto")
        .attr("markerWidth", 6)
        .attr("markerHeight", 6)
        .append("svg:path")
        .attr("d", "M 0,-5 L 10 ,0 L 0,5")
        .attr("fill", "var(--nasa-grey)");

    defs.append("marker")
        .attr("id", "arrowhead-async")
        .attr("viewBox", "-0 -5 10 10")
        .attr("refX", 10)
        .attr("refY", 0)
        .attr("orient", "auto")
        .attr("markerWidth", 6)
        .attr("markerHeight", 6)
        .append("svg:path")
        .attr("d", "M 0,-5 L 10 ,0 L 0,5")
        .attr("fill", "var(--nasa-grey)");


    const link = zoomLayer.append("g").selectAll("path").data(links).enter().append("path")
        .attr("class", d => "link " + (d.isAsync ? "async" : "sync"))
        .attr("marker-end", d => d.isAsync ? "url(#arrowhead-async)" : "url(#arrowhead-sync)")
        .attr("d", d => {
            const s = d.source;
            const t = d.target;
            const x1 = s.x + s.width / 2;
            const y1 = s.y;
            const x2 = t.x - t.width / 2;
            const y2 = t.y;
            const cx = (x1 + x2) / 2;
            return `M${x1},${y1} C${cx},${y1} ${cx},${y2} ${x2},${y2}`;
        });

    const node = zoomLayer.append("g").selectAll("g").data(nodes).enter().append("g")
        .attr("class", d => "node " + d.type)
        .attr("transform", d => `translate(${d.x},${d.y})`)
        .on("click", (event, d) => renderNodeDetails(d));

    node.append("rect")
        .attr("x", d => -d.width / 2).attr("y", -20)
        .attr("width", d => d.width).attr("height", 40)
        .attr("rx", 2).attr("ry", 2);

    const getIconPath = (d) => {
        const left = -d.width / 2;
        const icons = {
            'init': `M${left+10},-7 L${left+10},7 L${left+22},0 Z`,
            'step': `M${left+8},0 L${left+12},-4 L${left+16},4 L${left+20},-4 L${left+24},4 L${left+28},0`,
            'sink': `M${left+10},-4 L${left+26},-4 L${left+18},6 Z`
        };
        return icons[d.type];
    };
    
    node.append("path")
        .attr("class", "node-icon")
        .attr("d", d => getIconPath(d));

    node.append("text")
        .attr("dx", 10)
        .attr("dy", 5)
        .attr("text-anchor", "middle")
        .style("font-family", "monospace")
        .style("font-size", "12px")
        .text(d => {
            const maxChars = Math.floor((d.width - 50) / 8);
            return d.name.length > maxChars ? d.name.substring(0, maxChars - 3) + '...' : d.name;
        });
}
