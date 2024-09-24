package IO2Test.NIO;

import java.nio.ByteBuffer;

public class BufferTest {
    /**
     * 重要参数
     * 1. position 下一次读写元素的索引
     * 2. limit 缓冲区当前终点，可以修改
     * 3. capacity 最大数据量，不能改变
     */
    public static void main(String[] args) {
//        test1();
        test2();
    }

    public static void test2(){
        //      新建缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(10);
        String name = "hello";
        buffer.put(name.getBytes());
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());
        System.out.println("================");

        buffer.clear();
        /** 读->写
         * 清除缓冲区数据
         * 1. position 设置为0
         */
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());
        System.out.println("================");
        byte[] b = new byte[2];
        buffer.put(name.getBytes());
        /**
         * 写->读
         */
        buffer.flip();
        buffer.get(b);
        System.out.println(new String(b));
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());
        System.out.println("================");

        buffer.compact();
        /**
         * 读->写
         */
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());
        System.out.println("================");
        buffer.put("world".getBytes());
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());
    }

    public static void test1(){
//      新建缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(10);
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());
        System.out.println("================");

//      缓冲区加数据
        String name = "hello";
        buffer.put(name.getBytes());
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());
        System.out.println("================");

//      设置缓冲区界限
        buffer.flip();
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());
        System.out.println("================");

//      读数据
        char ch = (char) buffer.get();
        char ch2 = (char) buffer.get();
        System.out.println(ch);
        System.out.println(ch2);
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());
        System.out.println("================");
    }
}
