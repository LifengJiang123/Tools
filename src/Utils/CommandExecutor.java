package Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandExecutor {
    /**
     * 执行CMD命令的公共函数，兼容Windows和Linux
     * @param command 要执行的命令
     * @return 命令执行结果
     */
    public static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        Process process = null;
        
        try {
            // 根据操作系统类型选择执行方式
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;
            
            if (os.contains("win")) {
                // Windows系统
                processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
            } else {
                // Linux/Unix/Mac系统
                processBuilder = new ProcessBuilder("/bin/sh", "-c", command);
            }
            
            process = processBuilder.start();
            
            // 读取命令执行结果
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            // 等待进程执行完成
            process.waitFor();
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "执行命令出错: " + e.getMessage();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        
        return output.toString();
    }
}
