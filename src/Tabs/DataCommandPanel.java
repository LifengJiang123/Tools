package Tabs;

import Utils.CommandExecutor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import interfaces.DeviceSelectionListener;

public class DataCommandPanel extends JPanel implements DeviceSelectionListener{
    private JTabbedPane tabbedPane;
    private List<TabNode> tabNodes;
    private String selectedDeviceId;
    private JTextArea outputArea; // 添加输出区域
    private JSplitPane mainSplitPane; // 用于分割tab页面和输出窗口

    @Override
    public void onDeviceSelected(String newDeviceId) {
        this.selectedDeviceId = newDeviceId;
    }

    // 自定义Node类型
    static class TabNode {
        String name;
        List<ButtonNode> buttons;

        TabNode(String name) {
            this.name = name;
            this.buttons = new ArrayList<>();
        }
    }

    static class ButtonNode {
        String name;
        boolean show;
        List<String> commands;

         ButtonNode(String name) {
            this.name = name;
            this.show = false;
            this.commands = new ArrayList<>();
        }
    }

    public DataCommandPanel() {
        setLayout(new BorderLayout());
        initializeComponents();
    }

    private void initializeComponents() {
        // 创建主分割面板
        mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setResizeWeight(0.5); // 上下各占50%空间
        mainSplitPane.setDividerLocation(0.5);

        // 初始化tab页面
        initializeDataCommands();

        // 初始化输出区域和清除按钮
        initializeOutputArea();

        add(mainSplitPane, BorderLayout.CENTER);
    }

    private void initializeDataCommands() {
        tabbedPane = new JTabbedPane();
        tabNodes = new ArrayList<>();

        // 1. 先解析XML文件
        Document document = parseXmlFile();
        if (document == null) {
            add(tabbedPane, BorderLayout.CENTER);
            return;
        }

        // 2. 将解析后的数据存储到自定义Node类型中
        parseAndStoreData(document);

        // 3. 遍历所有的tabs，按照tabs的属性值创建多个tab界面
        createTabInterfaces();

        // 将tabbedPane设置为分割面板的上半部分
        mainSplitPane.setTopComponent(tabbedPane);
    }

    private void initializeOutputArea() {
        // 创建输出文本区域
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
//        outputArea.setBackground(Color.BLACK);
//        outputArea.setForeground(Color.GREEN);

        // 创建带滚动条的面板
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        outputScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // 创建清除按钮
        JButton clearButton = new JButton("清除所有内容");
        clearButton.addActionListener(e -> clearOutput());

        // 创建底部面板放置清除按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(clearButton);

        // 创建输出区域的整体面板
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.add(outputScrollPane, BorderLayout.CENTER);
        outputPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 将输出面板设置为分割面板的下半部分
        mainSplitPane.setBottomComponent(outputPanel);
    }

    private void clearOutput() {
        outputArea.setText("");
    }

    private Document parseXmlFile() {
        try {
            Path xmlPath = Paths.get("Data/cmd.xml");
            System.out.println("尝试读取文件: " + xmlPath.toAbsolutePath());

            if (!Files.exists(xmlPath)) {
                System.err.println("XML文件不存在: " + xmlPath.toAbsolutePath());
                JOptionPane.showMessageDialog(this, "XML文件不存在: " + xmlPath.toString(),
                        "文件错误", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            // 使用Java内置XML解析器解析XML文件
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlPath.toFile());
            document.getDocumentElement().normalize();

            System.out.println("XML文件解析成功");
            return document;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "解析XML文件出错: " + e.getMessage(),
                    "解析错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return null;
        }
    }

    private void parseAndStoreData(Document document) {
        try {
            // 解析XML并存储到自定义Node类型中
            Element root = document.getDocumentElement();
            System.out.println("根元素名称: " + root.getNodeName());

            // 遍历所有一级节点（tabs）
            NodeList topLevelNodes = root.getChildNodes();
            for (int i = 0; i < topLevelNodes.getLength(); i++) {
                Node topLevelNode = topLevelNodes.item(i);
                if (topLevelNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element topLevelElement = (Element) topLevelNode;
                    // 优化：优先获取name属性值，如果没有则使用标签名
                    String topLevelName = topLevelElement.hasAttribute("name") ?
                            topLevelElement.getAttribute("name") : topLevelElement.getNodeName();
                    System.out.println("处理一级节点: " + topLevelName);

                    TabNode tabNode = new TabNode(topLevelName);

                    // 遍历每个一级节点下的二级节点（buttons）
                    NodeList secondLevelNodes = topLevelElement.getChildNodes();
                    for (int j = 0; j < secondLevelNodes.getLength(); j++) {
                        Node secondLevelNode = secondLevelNodes.item(j);
                        if (secondLevelNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element secondLevelElement = (Element) secondLevelNode;
                            // 优化：优先获取name属性值，如果没有则使用标签名
                            String secondLevelName = secondLevelElement.hasAttribute("name") ?
                                    secondLevelElement.getAttribute("name") : secondLevelElement.getNodeName();
                            System.out.println("  处理二级节点: " + secondLevelName);

                            ButtonNode buttonNode = new ButtonNode(secondLevelName);

                            // 解析二级节点下的命令列表
                            NodeList commandNodes = secondLevelElement.getChildNodes();
                            for (int k = 0; k < commandNodes.getLength(); k++) {
                                Node commandNode = commandNodes.item(k);
                                if (commandNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element commandElement = (Element) commandNode;
                                    String command = commandElement.getTextContent().trim();
                                    if (!command.isEmpty()) {
                                        buttonNode.commands.add(command);
                                        System.out.println("    添加命令: " + command);
                                    }
                                } else if (commandNode.getNodeType() == Node.TEXT_NODE) {
                                    String command = commandNode.getTextContent().trim();
                                    if (!command.isEmpty()) {
                                        buttonNode.commands.add(command);
                                        System.out.println("    添加命令: " + command);
                                    }
                                }
                            }

                            // 解析 show 属性，默认为 false
                            if (secondLevelElement.hasAttribute("show")) {
                                String showValue = secondLevelElement.getAttribute("show");
                                buttonNode.show = "true".equalsIgnoreCase(showValue);
                            }

                            tabNode.buttons.add(buttonNode);
                        }
                    }

                    tabNodes.add(tabNode);
                    System.out.println("存储Tab节点: " + tabNode.name + "，包含按钮数: " + tabNode.buttons.size());
                }
            }
        } catch (Exception e) {
            System.err.println("解析XML数据时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void createTabInterfaces() {
        // 遍历所有的tabs，按照tabs的属性值创建多个tab界面
        for (TabNode tabNode : tabNodes) {
            JPanel subPanel = createTabPanel(tabNode);
            tabbedPane.addTab(tabNode.name, subPanel);
            System.out.println("222已添加Tab界面: " + tabNode.name);
        }
    }

    private JPanel createTabPanel(TabNode tabNode) {
        JPanel panel = new JPanel();
        panel.setLayout(new WrapLayout());
        panel.setBackground(new Color(248, 249, 250)); // 浅灰色背景
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 添加边距

        System.out.println("开始创建Tab面板: " + tabNode.name + "，包含按钮数量: " + tabNode.buttons.size());

        // 遍历每一个tabs下的button，按照button的属性值创建button按钮
        for (ButtonNode buttonNode : tabNode.buttons) {
            createAndAddButton(panel, buttonNode);
        }

        System.out.println("Tab面板创建完成: " + tabNode.name + "，实际按钮数量: " + panel.getComponentCount());

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // 平滑滚动
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // 移除边框

        JPanel container = new JPanel(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    private void createAndAddButton(JPanel panel, ButtonNode buttonNode) {
        System.out.println("创建按钮: " + buttonNode.name);
        JButton button = new JButton(buttonNode.name);

        // 设置按钮提示信息
        StringBuilder tooltip = new StringBuilder();
        for (int i = 0; i < buttonNode.commands.size(); i++) {
            tooltip.append("[").append(i).append("] ").append(buttonNode.commands.get(i)).append("\n");
        }
        button.setToolTipText(tooltip.toString());

        // 添加按钮点击事件
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleButtonClick(buttonNode);
            }
        });

        panel.add(button);
        System.out.println("按钮已添加到面板: " + buttonNode.name + "，面板当前组件数: " + panel.getComponentCount());
    }

    private void handleButtonClick(ButtonNode buttonNode) {
        // 当点击时，输出下所有的command的值
        if (buttonNode.show) {
            appendToOutput("按钮 '" + buttonNode.name + "' 被点击\n");
            appendToOutput("命令列表:\n");
        }

        for (int i = 0; i < buttonNode.commands.size(); i++) {
            System.out.println("adb -s " + selectedDeviceId + " " + buttonNode.commands.get(i));
        }

        if (buttonNode.show) {
            appendToOutput("\n");
        }
    }

    // 添加文本到输出区域
    private void appendToOutput(String text) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }

    // 自定义布局管理器，使组件能够自动换行
    class WrapLayout extends FlowLayout {
        public WrapLayout() {
            super();
            setHgap(15);
            setVgap(15);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return doLayout(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            return doLayout(target, false);
        }

        private Dimension doLayout(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;

                // 如果目标宽度为0，使用合理的默认值
                if (targetWidth <= 0) {
                    targetWidth = 600;
                }

                Insets insets = target.getInsets();
                int maxWidth = targetWidth;
                int maxHeight = 0;
                int x = insets.left;
                int y = insets.top;
                int rowHeight = 0;

                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

                        // 确保组件有合理的最小尺寸
                        if (d.width <= 0) d.width = 120;
                        if (d.height <= 0) d.height = 30;

                        // 检查是否需要换行
                        if (x + d.width > targetWidth - insets.right && x > insets.left) {
                            // 换行
                            y += rowHeight + getVgap();
                            x = insets.left;
                            rowHeight = 0;
                        }

                        rowHeight = Math.max(rowHeight, d.height);
                        x += d.width + getHgap();
                    }
                }

                // 计算最终高度
                maxHeight = y + rowHeight + insets.bottom;

                // 确保最小高度
                if (maxHeight < 60) {
                    maxHeight = 100;
                }

                return new Dimension(maxWidth, maxHeight);
            }
        }

        @Override
        public void layoutContainer(Container target) {
            synchronized (target.getTreeLock()) {
                Insets insets = target.getInsets();
                int targetWidth = target.getSize().width;
                if (targetWidth <= 0) targetWidth = 600;

                int x = insets.left;
                int y = insets.top;
                int rowHeight = 0;

                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = m.getPreferredSize();

                        // 确保组件尺寸合理
                        if (d.width <= 0) d.width = 120;
                        if (d.height <= 0) d.height = 30;

                        // 换行检查
                        if (x + d.width > targetWidth - insets.right && x > insets.left) {
                            y += rowHeight + getVgap();
                            x = insets.left;
                            rowHeight = 0;
                        }

                        m.setBounds(x, y, d.width, d.height);
                        rowHeight = Math.max(rowHeight, d.height);
                        x += d.width + getHgap();
                    }
                }
            }
        }
    }
}
