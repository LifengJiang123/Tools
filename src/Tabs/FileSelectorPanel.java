package Tabs;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import interfaces.DeviceSelectionListener;

public class FileSelectorPanel extends JPanel implements DeviceSelectionListener{
    private List<String> selectedFiles;
    private JPanel checkBoxPanel;
    private JTextField folderPathField;
    private String selectedDeviceId = "";
    private String defaultPath = "D:\\install\\lingma\\Lingma\\bin";

    public FileSelectorPanel() {
        this.selectedFiles = new ArrayList<>();
        initializeUI();
        // 首次加载时自动加载默认路径的结果
        loadFilesFromPath(defaultPath);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // 顶部面板：文件夹路径输入和按钮
        JPanel topPanel = new JPanel(new FlowLayout());
        folderPathField = new JTextField(30);
        folderPathField.setText(defaultPath);

        JButton browseButton = new JButton("浏览");
        JButton loadButton = new JButton("加载文件");
        JButton printButton = new JButton("安装");
        JButton selectAllButton = new JButton("取消所有选中");

        browseButton.addActionListener(e -> browseFolder());
        loadButton.addActionListener(e -> loadFiles());
        printButton.addActionListener(e -> printSelectedFiles());
        selectAllButton.addActionListener(e -> processSelectAll());

        topPanel.add(new JLabel("文件夹路径:"));
        topPanel.add(folderPathField);
        topPanel.add(browseButton);
        topPanel.add(loadButton);
        topPanel.add(printButton);
        topPanel.add(selectAllButton);

        // 中部面板：复选框区域
        checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(checkBoxPanel);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void browseFolder() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = fileChooser.getSelectedFile();
            folderPathField.setText(selectedFolder.getAbsolutePath());
        }
    }

    private void loadFiles() {
        String folderPath = folderPathField.getText();
        if (folderPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择文件夹路径", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        loadFilesFromPath(folderPath);
    }

    private void loadFilesFromPath(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            JOptionPane.showMessageDialog(this, "无效的文件夹路径: " + folderPath, "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 更新路径文本框
        folderPathField.setText(folderPath);

        // 清空之前的复选框
        checkBoxPanel.removeAll();

        // 遍历文件夹中的所有文件
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    JCheckBox checkBox = new JCheckBox(file.getName());
                    String filePath = file.getAbsolutePath();

                    checkBox.addActionListener(e -> {
                        if (checkBox.isSelected()) {
                            selectedFiles.add(filePath);
                        } else {
                            selectedFiles.remove(filePath);
                        }
                    });

                    checkBoxPanel.add(checkBox);
                }
            }
        }

        checkBoxPanel.revalidate();
        checkBoxPanel.repaint();
    }

    private void printSelectedFiles() {
//        StringBuilder message = new StringBuilder("选中的文件:\n");
        for (String filePath : selectedFiles) {
            System.out.println("adb -s " + selectedDeviceId + " install " + filePath);
//            message.append(filePath).append("\n");
        }
//        System.out.println(message.toString());
    }

    private void processSelectAll() {
        selectedFiles.clear(); // 清空选中文件列表

        // 取消所有复选框的选中状态
        Component[] components = checkBoxPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JCheckBox) {
                ((JCheckBox) component).setSelected(false);
            }
        }

        // 可选：显示确认消息
        // JOptionPane.showMessageDialog(this, "已清除所有选中的文件", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    // 提供设置默认路径的方法
    public void setDefaultPath(String defaultPath) {
        this.defaultPath = defaultPath;
        if (folderPathField != null) {
            folderPathField.setText(defaultPath);
            // 自动加载新路径的内容
            loadFilesFromPath(defaultPath);
        }
    }

    // 获取当前默认路径
    public String getDefaultPath() {
        return this.defaultPath;
    }

    @Override
    public void onDeviceSelected(String newDeviceId) {
        this.selectedDeviceId = newDeviceId;
    }
}
