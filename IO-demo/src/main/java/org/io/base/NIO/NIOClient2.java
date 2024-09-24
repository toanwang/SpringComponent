package IO2Test.NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

public class NIOClient2 {
    private Selector selector;
    private SocketChannel socketChannel;
    private String username;
    private static final String IP = "127.0.0.1";
    private static final int PORT = 9999;

    public NIOClient2() {
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open(new InetSocketAddress(IP, PORT));
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
            username = socketChannel.getLocalAddress().toString().substring(1);
            System.out.println("当前客户端 " + username + " 准备完成...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readInfo() throws IOException {
        while (selector.select() > 0) {
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isReadable()) {
                    SocketChannel sc = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    sc.read(buffer);
                    System.out.println(new String(buffer.array()).trim());
                }
                iterator.remove();
            }
        }
    }

    private void sendToServer(String s) {
        s = username + " 说: " + s;
        try {
            socketChannel.write(ByteBuffer.wrap(s.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        NIOClient2 client = new NIOClient2();
        new Thread(() -> {
            try {
                client.readInfo();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            String s = sc.nextLine();
            client.sendToServer(s);
        }
    }
}