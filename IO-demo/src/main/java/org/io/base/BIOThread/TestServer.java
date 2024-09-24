package IO2Test.BIOThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer {
    public static void main(String[] args) {
        try{
            // 创建ServerSocket
            ServerSocket serverSocket = new ServerSocket(8888);
            // 创建自定义线程池
            SocketServerPoolHandler poolHandler = new SocketServerPoolHandler(3, 10);
            // 轮训
            while(true){
                // 阻塞, 等待连接
                Socket socket = serverSocket.accept();
                // 新启动一个线程, 处理此次连接
                Runnable target = new ServerRunableTarget(socket);
                // 加入到线程池中处理
                poolHandler.execute(target);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
