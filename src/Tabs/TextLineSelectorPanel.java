package Tabs;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextLineSelectorPanel extends JPanel {
    private List<String> selectedLines;
    private JPanel checkBoxPanel;
    private JTextField filePathField;
    private String defaultPath;

    public TextLineSelectorPanel() {
        selectedLines = new ArrayList<>();
        // 设置默认路径为Data目录下的a.txt
        this.defaultPath = "D:\\tools\\tools\\src\\Data\\a.txt";
        initializeUI();
        // 启动时自动加载默认路径的文件
        loadTextLinesFromFile(defaultPath);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // 顶部面板：文件路径输入和按钮
        JPanel topPanel = new JPanel(new FlowLayout());
        filePathField = new JTextField(30);
        filePathField.setText(defaultPath); // 显示默认路径

        JButton browseButton = new JButton("浏览");
        JButton loadButton = new JButton("加载文本行");
        JButton printButton = new JButton("打印选中文本行");

        browseButton.addActionListener(e -> browseFile());
        loadButton.addActionListener(e -> loadTextLines());
        printButton.addActionListener(e -> printSelectedLines());

        topPanel.add(new JLabel("文件路径:"));
        topPanel.add(filePathField);
        topPanel.add(browseButton);
        topPanel.add(loadButton);
        topPanel.add(printButton);

        // 中部面板：复选框区域
        checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(checkBoxPanel);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void loadTextLines() {
        String filePath = filePathField.getText();
        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择文件路径", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        loadTextLinesFromFile(filePath);
    }

    private void loadTextLinesFromFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            // 如果默认文件不存在，不弹出错误提示，但可以在控制台记录
            if (!filePath.equals(defaultPath)) { // 只有用户主动加载时才提示错误
                JOptionPane.showMessageDialog(this, "无效的文件路径: " + filePath, "错误", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        // 更新路径文本框
        filePathField.setText(filePath);

        // 清空之前的复选框
        checkBoxPanel.removeAll();

        // 逐行读取文件内容
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                JCheckBox checkBox = new JCheckBox("Line " + lineNumber + ": " + line);
                String lineContent = line;

                checkBox.addActionListener(e -> {
                    if (checkBox.isSelected()) {
                        selectedLines.add(lineContent);
                    } else {
                        selectedLines.remove(lineContent);
                    }
                });

                checkBoxPanel.add(checkBox);
                lineNumber++;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "读取文件出错: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        checkBoxPanel.revalidate();
        checkBoxPanel.repaint();
    }

    private void printSelectedLines() {
        StringBuilder message = new StringBuilder("选中的文本行:\n");
        for (String line : selectedLines) {
            message.append(line).append("\n");
        }
        JOptionPane.showMessageDialog(this, message.toString(), "选中文本行列表", JOptionPane.INFORMATION_MESSAGE);
    }
}
