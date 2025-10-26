package Tabs;

import Utils.CommandExecutor;
import org.json.JSONObject;
import org.json.JSONArray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DataCommand extends JPanel {
    private JTabbedPane tabbedPane;
    private List<TabNode> tabNodes;
    private String selectedDeviceId;

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
        public boolean show;
        String name;
        Object commands;

        ButtonNode(String name, Object commands) {
            this.name = name;
            this.show = false;
            this.commands = commands;
        }
    }

    public DataCommand() {
        setLayout(new BorderLayout());
        initializeDataCommands();
    }

    public DataCommand(String selectedDeviceId) {
        this.selectedDeviceId = selectedDeviceId;
        setLayout(new BorderLayout());
        initializeDataCommands();
    }

    private void initializeDataCommands() {
        tabbedPane = new JTabbedPane();

        // 美化TabbedPane
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.putClientProperty("JTabbedPane.tabAreaAlignment", "leading");

        // 1. 解析cmd.json文件
        JSONObject jsonObject = parseJsonFile();
        if (jsonObject == null) {
            add(tabbedPane, BorderLayout.CENTER);
            return;
        }

        // 2. 将解析后的数据存储到自定义Node类型中
        tabNodes = new ArrayList<>();
        parseAndStoreData(jsonObject);

        // 3. 遍历所有的tabs，按照tabs的属性值创建多个tab界面
        createTabInterfaces();

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JSONObject parseJsonFile() {
        try {
            Path jsonPath = Paths.get("Data/cmd.json");
            System.out.println("尝试读取文件: " + jsonPath.toAbsolutePath());

            if (!Files.exists(jsonPath)) {
                System.err.println("JSON文件不存在: " + jsonPath.toAbsolutePath());
                JOptionPane.showMessageDialog(this, "JSON文件不存在: " + jsonPath.toString(),
                        "文件错误", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            String content = new String(Files.readAllBytes(jsonPath));
            System.out.println("文件内容长度: " + content.length());

            JSONObject jsonObject = new JSONObject(content);
            System.out.println("JSON对象键数量: " + jsonObject.length());
            return jsonObject;

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "无法读取cmd.json文件: " + e.getMessage(),
                    "文件错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "解析JSON文件出错: " + e.getMessage(),
                    "解析错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return null;
        }
    }

    private void parseAndStoreData(JSONObject jsonObject) {
        // 遍历所有第一级节点
        for (String topLevelKey : jsonObject.keySet()) {
            System.out.println("处理一级节点: " + topLevelKey);
            TabNode tabNode = new TabNode(topLevelKey);

            JSONObject secondLevelObject = jsonObject.getJSONObject(topLevelKey);

            // 遍历二级节点并存储为ButtonNode
            for (String secondLevelKey : secondLevelObject.keySet()) {
                Object commandValue = secondLevelObject.get(secondLevelKey);

                // 创建 ButtonNode 实例
                ButtonNode buttonNode = new ButtonNode(secondLevelKey, commandValue);

                // 检查是否有 show 属性并且值为 true
                if (commandValue instanceof JSONObject) {
                    JSONObject commandObj = (JSONObject) commandValue;
                    if (commandObj.has("show") && "true".equals(commandObj.getString("show"))) {
                        buttonNode.show = true;
                    }
                }

                tabNode.buttons.add(buttonNode);
                System.out.println("存储按钮节点: " + secondLevelKey);
            }

            tabNodes.add(tabNode);
            System.out.println("存储Tab节点: " + topLevelKey);
        }
    }

    private void createTabInterfaces() {
        // 遍历所有的tabs，创建多个tab界面
        for (TabNode tabNode : tabNodes) {
            JPanel subPanel = createTabPanel(tabNode);
            tabbedPane.addTab(tabNode.name, subPanel);
            System.out.println("已添加Tab界面: " + tabNode.name);
        }
    }

    private JPanel createTabPanel(TabNode tabNode) {
        // 创建一个新的 JPanel 作为按钮容器，包含所有按钮的面板
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

        // 创建滚动面板包装按钮面板，支持内容滚动
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // 平滑滚动
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // 移除边框

        JPanel container = new JPanel(new BorderLayout());
//        container.setBorder(BorderFactory.createTitledBorder(
//                BorderFactory.createLineBorder(new Color(64, 145, 220), 2), // 蓝色边框
//                tabNode.name, // 使用Tab名称作为标题
//                javax.swing.border.TitledBorder.LEFT,
//                javax.swing.border.TitledBorder.TOP,
//                new Font("Segoe UI", Font.BOLD, 16), // 字体设置
//                new Color(64, 145, 220) // 标题颜色
//        ));
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    private void createAndAddButton(JPanel panel, ButtonNode buttonNode) {
        System.out.println("创建按钮: " + buttonNode.name);
        JButton button = new JButton(buttonNode.name);

        // 美化按钮样式
        // styleButton(button);

        // 设置按钮提示信息
//        if (buttonNode.commands instanceof String) {
//            button.setToolTipText((String) buttonNode.commands);
//        } else if (buttonNode.commands instanceof JSONObject || buttonNode.commands instanceof JSONArray) {
//            button.setToolTipText(buttonNode.commands.toString());
//        }

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

    private void styleButton(JButton button) {
        // 设置按钮固定尺寸确保可见性
        button.setPreferredSize(new Dimension(160, 40));
        button.setMinimumSize(new Dimension(120, 30));
        button.setMaximumSize(new Dimension(200, 45));

        // 设置按钮样式
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setBackground(new Color(64, 145, 220)); // 蓝色背景
        button.setForeground(Color.WHITE); // 白色文字
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); // 内边距
        button.setFocusPainted(false); // 移除焦点边框
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 手型光标

        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(41, 128, 185)); // 深蓝色悬停效果
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(64, 145, 220)); // 恢复原始颜色
            }
        });
    }

    private void handleButtonClick(ButtonNode buttonNode) {
        // 当点击时，输出下所有的command的值
        System.out.println("按钮 '" + buttonNode.name + "' 被点击");

        if (buttonNode.commands instanceof JSONArray) {
            JSONArray commandArray = (JSONArray) buttonNode.commands;
            System.out.println("命令列表:");
            for (int i = 0; i < commandArray.length(); i++) {
                System.out.println("  [" + i + "] " + commandArray.get(i).toString());
            }
        } else {
            System.out.println("命令内容: " + buttonNode.commands.toString());
        }
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
