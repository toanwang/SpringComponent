package IO2Test.NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class NIOServer {
    private Selector selector;
    private ServerSocketChannel ssChannel;
    private static final int PORT = 9999;

    public NIOServer() throws IOException {
        // 获取选择器
        selector = Selector.open();
        // 获取通道
        ssChannel = ServerSocketChannel.open();
        // 通道绑定端口号
        ssChannel.bind(new InetSocketAddress(PORT));
        // 通道设置为非阻塞
        ssChannel.configureBlocking(false);
        // 通道注册到选择器上，关注OP_ACCEPT事件(建立连接)
        ssChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void listen() throws IOException {
        // 选择器轮训注册的通道中 就绪的事件
        while (selector.select() > 0) {
            // 获取通道中就绪的事件
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            // 遍历事件
            while (iterator.hasNext()) {
                // 获取事件
                SelectionKey sk = iterator.next();
                // 判断事件类别
                if (sk.isAcceptable()) {
                    // 获取到有链接的channel
                    SocketChannel sChannel = ssChannel.accept();
                    // 设置为非阻塞
                    sChannel.configureBlocking(false);
                    System.out.println(sChannel.getRemoteAddress() + " 上线");
                    // 通道注册到选择器上, 关注OP_READ事件(读就绪事件)
                    sChannel.register(selector, SelectionKey.OP_READ);
                }else if(sk.isReadable()){
                    readClientData(sk);
                }
                iterator.remove();
            }
        }
    }

    public void readClientData(SelectionKey sk) throws IOException {
        SocketChannel sChannel = (SocketChannel) sk.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int count = sChannel.read(buffer);
        if (count > 0) {
            buffer.flip();
            String msg = new String(buffer.array(), 0, buffer.remaining());
            System.out.println("接收到了客户端消息: " + msg);
            sendInfoToOtherClients(msg, sChannel);
        }
    }

    private void sendInfoToOtherClients(String msg, SocketChannel sChannel) throws IOException {
        System.out.println("服务器转发消息中...");
        // 遍历所有注册到selector上的SocketChannel并排除sChannel
        for (SelectionKey key : selector.keys()) {
            Channel targetChannel = key.channel();
            // 排除自己
            if (targetChannel instanceof SocketChannel && targetChannel != sChannel) {
                SocketChannel dest = (SocketChannel) targetChannel;
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                dest.write(buffer);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        // 实例化server
        NIOServer server = new NIOServer();
        // 开始监听
        server.listen();
    }
}
