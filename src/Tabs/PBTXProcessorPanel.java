package Tabs;

import Utils.CommandExecutor;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;

public class PBTXProcessorPanel extends JPanel {
    private JTextField pbtxFilePathField;
    private JTextField intValueField;
    private int globalIntValue = 10;

    public PBTXProcessorPanel() {
        initializeUI();
        loadDefaultPBXTFile(); // 系统启动时加载默认文件
    }

    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 上半部分：perfetto
        JPanel perfettoPanel = new JPanel(new BorderLayout());
        perfettoPanel.setBorder(BorderFactory.createTitledBorder("perfetto"));

        // perfetto 配置面板
        JPanel perfettoConfigPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // PBTX文件选择
        gbc.gridx = 0; gbc.gridy = 0;
        perfettoConfigPanel.add(new JLabel("PBTX文件:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        pbtxFilePathField = new JTextField(30);
        perfettoConfigPanel.add(pbtxFilePathField, gbc);

        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JButton browsePBTXButton = new JButton("浏览");
        browsePBTXButton.addActionListener(e -> browsePBTXFile());
        perfettoConfigPanel.add(browsePBTXButton, gbc);

        // 整数输入框
        gbc.gridx = 0; gbc.gridy = 1;
        perfettoConfigPanel.add(new JLabel("整数值:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        intValueField = new JTextField(30);
        intValueField.setText(String.valueOf(globalIntValue));
        perfettoConfigPanel.add(intValueField, gbc);

        // 执行ADB命令按钮（原执行转换按钮）
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.CENTER;
        JButton executeADBButton = new JButton("执行ADB");
        executeADBButton.setPreferredSize(new Dimension(120, 35));
        executeADBButton.addActionListener(e -> executeADBCommand());
        perfettoConfigPanel.add(executeADBButton, gbc);

        perfettoPanel.add(perfettoConfigPanel, BorderLayout.NORTH);

        // 下半部分：systrace（占位）
        JPanel systracePanel = new JPanel();
        systracePanel.setBorder(BorderFactory.createTitledBorder("systrace"));
        systracePanel.add(new JLabel("systrace 功能区域"));

        // 添加两个部分到主面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, perfettoPanel, systracePanel);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.5);

        add(splitPane, BorderLayout.CENTER);
    }

    // 新增方法：加载默认的pbtx文件
    private void loadDefaultPBXTFile() {
        // 获取当前工作目录
        String currentDir = Paths.get("").toAbsolutePath().toString();
        // 构建data/trace目录路径
        String traceDirPath = Paths.get(currentDir, "data", "trace").toString();
        File traceDir = new File(traceDirPath);

        // 检查目录是否存在
        if (traceDir.exists() && traceDir.isDirectory()) {
            // 查找第一个.pbtx文件
            File[] pbtxFiles = traceDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pbtx"));
            if (pbtxFiles != null && pbtxFiles.length > 0) {
                String defaultFilePath = pbtxFiles[0].getAbsolutePath();
                pbtxFilePathField.setText(defaultFilePath);
                System.out.println("Loaded default PBTX file: " + defaultFilePath);

                // 执行adb devices命令
                executeADBDevicesCommand();
            }
        }
    }

    private void browsePBTXFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "PBTX Files (*.pbtx)", "pbtx"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            pbtxFilePathField.setText(filePath);

            // 选择文件后执行adb devices命令并打印文件地址
            executeADBDevicesCommand();
            System.out.println("Selected PBTX file path: " + filePath);
        }
    }

    private void executeADBDevicesCommand() {
        String command = "adb devices";
        CommandExecutor.executeCommand(command);
    }

    private void executeADBCommand() {
        // 获取并验证整数值
        try {
            globalIntValue = Integer.parseInt(intValueField.getText().trim());
        } catch (NumberFormatException e) {
            globalIntValue = 10;
            intValueField.setText("10");
        }

        // 执行adb命令
        String command = "adb shell getprop"; // 示例命令
        CommandExecutor.executeCommand(command);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
    }
}
