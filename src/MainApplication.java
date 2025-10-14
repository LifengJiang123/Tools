// MainApplication.java
import Tabs.CommandButtonPanel;
import Tabs.FileSelectorPanel;
import Tabs.TextLineSelectorPanel;
import Tabs.PBTXProcessorPanel;
import Utils.CommandExecutor;

import javax.swing.*;
import java.awt.*;

public class MainApplication {
    private String selectedDeviceId = ""; // 全局变量存储选中的设备ID
    private JComboBox<String> deviceComboBox; // 设备选择下拉列表
    private DefaultComboBoxModel<String> deviceModel; // 下拉列表模型

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainApplication().createAndShowGUI();
        });
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("多功能工具");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);

        // 创建主分割面板
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(150);
        mainSplitPane.setResizeWeight(0.1);

        // 左侧面板：设备选择在上方，功能列表在下方
        JPanel leftPanel = new JPanel(new BorderLayout());

        // 创建设备选择面板（放在上方）
        JPanel devicePanel = new JPanel(new BorderLayout());
        devicePanel.setBorder(BorderFactory.createTitledBorder("设备选择"));

        // 创建设备下拉列表
        deviceModel = new DefaultComboBoxModel<>();
        deviceComboBox = new JComboBox<>(deviceModel);
        deviceComboBox.addActionListener(e -> {
            // 更新全局变量
            String selected = (String) deviceComboBox.getSelectedItem();
            if (selected != null && !selected.isEmpty()) {
                selectedDeviceId = selected.split("\\s+")[0]; // 提取设备ID（第一列）
                System.out.println("选中的设备ID: " + selectedDeviceId);
            }
        });

        // 创建刷新按钮
        JButton refreshButton = new JButton("刷新");
        refreshButton.setPreferredSize(new Dimension(60, refreshButton.getPreferredSize().height));
        refreshButton.addActionListener(e -> refreshDeviceList());

        // 设备选择和刷新按钮的布局
        JPanel deviceControlPanel = new JPanel(new BorderLayout());
        deviceControlPanel.add(deviceComboBox, BorderLayout.CENTER);
        deviceControlPanel.add(refreshButton, BorderLayout.EAST);

        devicePanel.add(deviceControlPanel, BorderLayout.NORTH);

        // 创建功能列表项（放在下方）
        JPanel featurePanel = new JPanel(new BorderLayout());
        featurePanel.setBorder(BorderFactory.createTitledBorder("功能模块"));

        String[] featureNames = {"文件选择器", "文本行选择器", "命令按钮面板", "PBTX处理器", "命令"};
        JList<String> featureList = new JList<>(featureNames);
        featureList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        featureList.setSelectedIndex(0);

        JScrollPane listScrollPane = new JScrollPane(featureList);
        featurePanel.add(listScrollPane, BorderLayout.CENTER);

        // 组合左侧面板：设备选择在上，功能列表在下
        leftPanel.add(devicePanel, BorderLayout.NORTH);
        leftPanel.add(featurePanel, BorderLayout.CENTER);

        // 右侧TabbedPane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("文件选择器", new FileSelectorPanel());
        tabbedPane.addTab("文本行选择器", new TextLineSelectorPanel());
        tabbedPane.addTab("命令按钮面板", new CommandButtonPanel());
        tabbedPane.addTab("PBTX处理器", new PBTXProcessorPanel());
        tabbedPane.addTab("命令", new CommandButtonPanel());

        // 实现列表与Tab页的联动
        featureList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = featureList.getSelectedIndex();
                if (selectedIndex >= 0) {
                    tabbedPane.setSelectedIndex(selectedIndex);
                }
            }
        });

        // 实现Tab页与列表的联动
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex >= 0) {
                featureList.setSelectedIndex(selectedIndex);
            }
        });

        mainSplitPane.add(leftPanel);
        mainSplitPane.add(tabbedPane);

        frame.add(mainSplitPane);
        frame.setVisible(true);

        // 初始化时加载设备列表
        refreshDeviceList();
    }

    /**
     * 刷新设备列表
     */
    private void refreshDeviceList() {
        try {
            // 执行adb devices命令
            String result = CommandExecutor.executeCommand("adb devices");

            // 清空现有列表
            deviceModel.removeAllElements();

            // 解析命令输出
            String[] lines = result.split("\n");
            boolean isFirstLine = true;

            for (String line : lines) {
                line = line.trim();
                // 跳过标题行和空行
                if (isFirstLine || line.isEmpty() || line.contains("List of devices")) {
                    isFirstLine = false;
                    continue;
                }

                // 添加有效设备行
                if (!line.startsWith("*") && line.contains("\t")) {
                    deviceModel.addElement(line);
                }
            }

            // 如果有设备，默认选择第一个
            if (deviceModel.getSize() > 0) {
                deviceComboBox.setSelectedIndex(0);
            } else {
                selectedDeviceId = "";
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "刷新设备列表失败: " + e.getMessage(),
                                        "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
