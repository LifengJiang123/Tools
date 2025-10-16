// 图表联动和交互功能
document.addEventListener('DOMContentLoaded', function() {
    // 获取所有图表容器
    const chartDivs = Array.from(document.getElementsByClassName('js-plotly-plot'));

    // 为每个图表绑定 relayout 事件，实现联动缩放
    chartDivs.forEach(div => {
        div.on('plotly_relayout', function(eventData) {
            if (eventData['xaxis.range']) {
                const [x0, x1] = eventData['xaxis.range'];

                // 更新所有图表的 xaxis.range
                chartDivs.forEach(targetDiv => {
                    if (targetDiv !== div) {
                        Plotly.relayout(targetDiv, {
                            'xaxis.range[0]': x0,
                            'xaxis.range[1]': x1
                        });
                    }
                });
            }
        });
    });

    // 数据悬浮功能
    chartDivs.forEach((div, index) => {
        div.on('plotly_hover', function (data) {
            const xVal = data.points[0].x;

            // 清除之前的提示
            chartDivs.forEach(d => {
                d.querySelectorAll('.hover-tooltip').forEach(el => el.remove());
            });

            // 创建提示框
            const tooltip = document.createElement('div');
            tooltip.className = 'hover-tooltip';
            tooltip.style.left = `${data.event.pageX + 10}px`;
            tooltip.style.top = `${data.event.pageY - 30}px`;

            // 构建提示内容
            let tooltipText = `<strong>time: ${xVal}</strong><br>`;
            let hasData = false;

            const plotlyInstance = div._fullLayout ? div : null;
            if (plotlyInstance) {
                const traces = plotlyInstance._fullData;
                traces.forEach(trace => {
                    const xData = trace.x;
                    const yData = trace.y;
                    const idx = xData.indexOf(xVal);

                    if (idx !== -1) {
                        hasData = true;
                        const color = trace.line.color || '#000';
                        tooltipText += `<span style="color: ${color};">${trace.name}: ${yData[idx]}</span><br>`;
                    }
                });
            }

            if (!hasData) {
                tooltipText += "没有找到对应的数据点。";
            }

            tooltip.innerHTML = tooltipText;
            div.appendChild(tooltip);
        });

        div.on('plotly_unhover', function () {
            chartDivs.forEach(d => {
                d.querySelectorAll('.hover-tooltip').forEach(el => el.remove());
            });
        });
    });
});
