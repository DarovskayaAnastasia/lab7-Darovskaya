import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        System.out.println("Client started...");
        ZContext context = new ZСontext();

        //socket to talk to server
        ZMQ.Socket requester = context.createSocket(SocketType.REQ);
        requester.connect("tcp://localhost:5559");

        Scanner input = new Scanner(System.in);
        System.out.println("Client ready");

        //endless loop
        while(true) {
            String command = input.nextLine();
            requester.send(command, 0);
        }
    }
}
