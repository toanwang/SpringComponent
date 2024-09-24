package IO2Test.NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

public class NIOClient {
    // client需要的选择器
    private Selector selector;
    // client需要的channel
    private SocketChannel socketChannel;
    private String username;
    private static final String IP = "127.0.0.1";
    private static final int PORT = 9999;

    public NIOClient() {
        try {
            // 获取选择器
            selector = Selector.open();
            // 获取通道绑定 IP+PORT
            socketChannel = SocketChannel.open(new InetSocketAddress(IP, PORT));
            // 通道设置为 非阻塞
            socketChannel.configureBlocking(false);
            // 选择器绑定通道: 消息类型是是有新数据
            socketChannel.register(selector, SelectionKey.OP_READ);
            username = socketChannel.getLocalAddress().toString().substring(1);
            System.out.println("当前客户端 " + username + " 准备完成...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readInfo() throws IOException {
        // 选择器是否有监听的事件到达
        while (selector.select() > 0) {
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            // 遍历所有的事件
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                // 是需要的类型
                if (key.isReadable()) {
                    // 拿到channel
                    SocketChannel sc = (SocketChannel) key.channel();
                    // 分配buffer
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    // 读取通道内的数据
                    sc.read(buffer);
                    // 打印消息
                    System.out.println(new String(buffer.array()).trim());
                }
                iterator.remove();
            }
        }
    }

    private void sendToServer(String s) {
        s = username + " 说: " + s;
        try {
            // 指定的channel写数据 (通过buffer)
            socketChannel.write(ByteBuffer.wrap(s.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        NIOClient client = new NIOClient();
        // 起一个进程一直去读消息
        new Thread(() -> {
            try {
                Thread.currentThread().sleep(30000);
                client.readInfo();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            String s = sc.nextLine();
            client.sendToServer(s);
        }
    }
}