package Tabs;

import Utils.CommandExecutor;
import javax.swing.*;
import java.awt.*;

public class CommandButtonPanel extends JPanel {
    private JTextArea outputArea;

    public CommandButtonPanel() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("命令操作"));

        JButton dateButton = new JButton("显示日期");
        JButton listDirButton = new JButton("列出目录内容");
        JButton systemInfoButton = new JButton("系统信息");
        JButton clearButton = new JButton("清空输出");

        // 统一按钮样式
        Dimension buttonSize = new Dimension(120, 30);
        Font buttonFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

        JButton[] buttons = {dateButton, listDirButton, systemInfoButton, clearButton};
        for (JButton button : buttons) {
            button.setPreferredSize(buttonSize);
            button.setFont(buttonFont);
        }

        dateButton.addActionListener(e -> showDate());
        listDirButton.addActionListener(e -> listDirectory());
        systemInfoButton.addActionListener(e -> showSystemInfo());
        clearButton.addActionListener(e -> clearOutput());

        buttonPanel.add(dateButton);
        buttonPanel.add(listDirButton);
        buttonPanel.add(systemInfoButton);
        buttonPanel.add(clearButton);

        // 创建输出区域
        outputArea = new JTextArea(20, 50);
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputArea.setBackground(Color.BLACK);
        outputArea.setForeground(Color.GREEN);
        outputArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("命令输出"));

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void showDate() {
        String os = System.getProperty("os.name").toLowerCase();
        String command = os.contains("win") ? "date /t" : "date";
        executeAndDisplayCommand(command);
    }

    private void listDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String command = os.contains("win") ? "dir" : "ls -la";
        executeAndDisplayCommand(command);
    }

    private void showSystemInfo() {
        String os = System.getProperty("os.name").toLowerCase();
        String command = os.contains("win") ? "systeminfo" : "uname -a";
        executeAndDisplayCommand(command);
    }

    private void executeAndDisplayCommand(String command) {
        outputArea.append("执行命令: " + command + "\n");
        outputArea.append("输出结果:\n");
        outputArea.append(CommandExecutor.executeCommand(command));
        outputArea.append("\n" + "=".repeat(50) + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    private void clearOutput() {
        outputArea.setText("");
    }
}
