import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Scanner;

public class Client {
    public static void main(String[] args) {

        ZMQ.Context context = ZMQ.context(1);

        // socket to talk to server
        ZMQ.Socket requester = context.socket(SocketType.REQ);
        requester.connect("tcp://localhost:5559");

        System.out.println("Launch and connect client...");

        Scanner input = new Scanner(System.in);

        // endless loop
        while(true) {
            String command = input.nextLine();
            requester.send(command, 0);
            String response = requester.recvStr();

            System.out.println("response: " + response);
        }

        // We never get here but clean up anyhow
        requester.close();
        context.term();
    }
}
