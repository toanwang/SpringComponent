package IO2Test.BIOThread;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TestClient {
    public static void main(String[] args) {
        try{
            Socket socket = new Socket("127.0.0.1", 8888);
            OutputStream os = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(os);
            Scanner sc = new Scanner(System.in);
            while(true){
                System.out.println("请说:");
                String input = sc.nextLine();
                pw.println(input);
                pw.flush();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
