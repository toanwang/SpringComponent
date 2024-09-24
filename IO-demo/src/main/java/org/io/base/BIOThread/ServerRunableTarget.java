package IO2Test.BIOThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerRunableTarget implements Runnable {
    private Socket socket;

    public ServerRunableTarget(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try{
            // 获取输入流
            InputStream is = socket.getInputStream();
            // 读取输入流
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String str;
            while ((str = br.readLine()) != null) {
                System.out.println("服务端收到:" + str);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
