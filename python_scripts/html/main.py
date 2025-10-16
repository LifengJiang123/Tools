import pandas as pd
import plotly.graph_objects as go

# 配置文件路径
excel_file = 'test.xlsx'
output_html = 'output_sync_all_x_with_slider2.html'

# 自定义标题变量
page_title = "所有图表联动X轴"
header_title = "所有图表联动X轴"

# 读取 Excel 文件的所有 sheet 名称
xls = pd.ExcelFile(excel_file)
sheet_names = xls.sheet_names

# 读取第一个 sheet 的 X 轴数据（所有 sheet 第一列相同）
df_first = pd.read_excel(excel_file, sheet_name=sheet_names[0])
x = df_first.iloc[:, 0]

# 创建每个图表
chart_html_parts = []
chart_ids = []

for sheet_name in sheet_names:
    df = pd.read_excel(excel_file, sheet_name=sheet_name)
    y_columns = df.columns[1:]

    fig = go.Figure()

    # 设置统一的 layout
    fig.update_layout(
        title=f"Sheet: {sheet_name}",
        xaxis=dict(
            title="X Axis",
            rangeslider=dict(visible=True),
            type='linear',
            fixedrange=False,
            showspikes=True,
            spikemode='across',
            spikethickness=1,
            spikecolor='gray',
            spikesnap='cursor',
        ),
        yaxis=dict(
            title="Values",
            showspikes=True,
            spikemode='across',
            spikethickness=1,
            spikecolor='gray'
        ),
        height=400,
        margin=dict(l=50, r=50, t=80, b=50)
    )

    # 添加 trace
    for col in y_columns:
        fig.add_trace(
            go.Scatter(x=x, y=df[col], mode='lines+markers', name=col, hoverinfo='none')
        )

    # 生成 HTML 并记录 chart_id
    chart_id = f'chart_{sheet_name.replace(" ", "_")}'
    chart_html = fig.to_html(
        full_html=False,
        include_plotlyjs='cdn' if sheet_name == sheet_names[0] else False,
        div_id=chart_id
    )

    chart_html_parts.append(chart_html)
    chart_ids.append(chart_id)

# HTML 模板内容
html_content = """<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>{page_title}</title>
    <link rel="stylesheet" href="static/css/styles.css">
    <script src="https://cdn.plot.ly/plotly-2.24.1.min.js"></script>
</head>
<body>
    <header>
        <h1>{header_title}</h1>
    </header>
    <main id="chart-container">
"""

# 组装最终的 HTML 内容
charts_content = '<hr>'.join(chart_html_parts)
html_content += charts_content

html_content += """
    </main>
    <script src="static/js/chart_interactions.js"></script>
</body>
</html>"""

# 写入 HTML 文件
with open(output_html, 'w', encoding='utf-8') as f:
    f.write(html_content)

print(f"所有图表联动缩放的 HTML 已生成在 {output_html}")
