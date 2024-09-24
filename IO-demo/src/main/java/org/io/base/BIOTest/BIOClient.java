package IO2Test.BIOTest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

public class BIOClient {
    public static void main(String[] args) {
        System.out.println("===客户端启动===");
        try{
//            创建socket连接对象
            Socket socket = new Socket("127.0.0.1", 9999);
//            获取socker的输出字节流，用于写数据发向远端主机
            OutputStream os = socket.getOutputStream();
//            把字节输出流包装成打印流
//            PrintStream想比OutputStream优点:使用更便捷;自动转换数据类型;代码可读性更高
            PrintStream ps = new PrintStream(os);
            ps.println("hello, 服务端");
//            输出流缓冲区数据立即写到目标设备
            ps.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
