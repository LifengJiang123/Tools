// CommandTabPanel.java
package Tabs;

import Utils.CommandExecutor;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.awt.*;
import java.io.File;

public class CommandTabPanel extends JPanel {
    private JTextArea outputArea;
    private JPanel buttonPanel;
    
    public CommandTabPanel() {
        initializeUI();
        loadCommands();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 上半部分：动态按钮区域
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("命令按钮"));
        
        JScrollPane buttonScrollPane = new JScrollPane(buttonPanel);
        buttonScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        buttonScrollPane.setPreferredSize(new Dimension(0, 200));
        
        // 下半部分：输出区域
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("输出结果"));
        
        outputArea = new JTextArea(10, 50);
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        
        // 清空按钮
        JButton clearButton = new JButton("清空输出");
        clearButton.addActionListener(e -> clearOutput());
        
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.add(clearButton);
        
        outputPanel.add(outputScrollPane, BorderLayout.CENTER);
        outputPanel.add(controlPanel, BorderLayout.SOUTH);
        
        // 添加到主面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buttonScrollPane, outputPanel);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.3);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private void loadCommands() {
        try {
            // 构建XML文件路径（基于当前工作目录）
            String xmlPath = "data/command/command.xml";
            File xmlFile = new File(xmlPath);
            
            if (!xmlFile.exists()) {
                outputArea.append("错误：找不到命令配置文件 " + xmlPath + "\n");
                return;
            }
            
            // 解析XML文件
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();
            
            // 获取所有command元素
            NodeList commandNodes = document.getElementsByTagName("command");
            
            // 为每个command创建按钮
            for (int i = 0; i < commandNodes.getLength(); i++) {
                Element commandElement = (Element) commandNodes.item(i);
                
                // 获取button_name和cmd属性
                String buttonName = commandElement.getAttribute("button_name");
                String cmd = commandElement.getAttribute("cmd");
                
                if (buttonName != null && !buttonName.isEmpty() && cmd != null && !cmd.isEmpty()) {
                    JButton commandButton = new JButton(buttonName);
                    commandButton.addActionListener(e -> executeCommand(cmd));
                    buttonPanel.add(commandButton);
                }
            }
            
            // 重新验证面板以显示新添加的按钮
            buttonPanel.revalidate();
            buttonPanel.repaint();
            
            outputArea.append("成功加载 " + commandNodes.getLength() + " 个命令按钮\n");
            
        } catch (Exception e) {
            outputArea.append("加载命令时出错: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
    
    private void executeCommand(String cmd) {
        outputArea.append("执行命令: " + cmd + "\n");
        try {
            String result = CommandExecutor.executeCommand(cmd);
            outputArea.append(result);
        } catch (Exception e) {
            outputArea.append("执行命令出错: " + e.getMessage() + "\n");
        }
        outputArea.append("\n" + "=".repeat(50) + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }
    
    private void clearOutput() {
        outputArea.setText("");
    }
}
