package IO2Test.NIO;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ChannelTest {
    public static void main(String[] args) {
//        write();
        read();
    }
    public static void write(){
        try{
            // 字节流输出到目标文件
            FileOutputStream fos = new FileOutputStream("channel.txt");
            // 获取字节流输出的channel
            FileChannel channel = fos.getChannel();
            // 分配缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            // 写入缓冲区
            buffer.put("Hello World".getBytes());
            // 缓冲区切换到写出模式
            buffer.flip();
            // 缓冲区写入通道
            channel.write(buffer);
            // 关闭通道
            channel.close();
            System.out.println("写数据到文件");
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static void read(){
        try{
            FileInputStream fis = new FileInputStream("channel.txt");
            FileChannel channel = fis.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(2);
            String res = "";
            while(channel.read(buffer) != -1){
//                System.out.println(buffer.capacity());
                buffer.flip();
                String str = new String(buffer.array(), 0, buffer.remaining());
                System.out.println(str);
                res = res + str;
                buffer.clear();
            }
//            System.out.println(channel.read(buffer));
//            System.out.println(channel.read(buffer));
//            channel.read(buffer);
//            buffer.flip();
//            String str = new String(buffer.array(), 0, buffer.remaining());
            System.out.println(res);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
