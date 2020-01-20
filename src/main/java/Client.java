import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Client {
    private static final String PROXY_ADDR = "tcp://localhost:5559";
    private static final Logger log = new Logger("(Client message)");

    public static void main(String[] args) {

        ZMQ.Context context = ZMQ.context(1);

        // socket to talk to server
        ZMQ.Socket requester = context.socket(SocketType.REQ);
        requester.connect(PROXY_ADDR);

        log.info("Launch and connect client on", PROXY_ADDR);
        System.out.println("--- Enter QUIT for exit");

        Scanner input = new Scanner(System.in);

        // endless loop
        while (true) {
            String cmd = input.nextLine();
            if (cmd.equals("QUIT")) {
                System.out.println("--- QUIT");
                break;
            }
            Command command = new Command(cmd);

            if (command.typeCheck(Command.SET_TYPE, Command.GET_TYPE)) {
                log.info(command, "command accepted for processing");

                requester.send(command.encode(), 0);
                String raw = requester.recvStr(0);
                Command response = new Command(raw);
                if (response.typeCheck(Command.RESPONSE_TYPE))
                    log.info("response:", response.getResponse());
                else
                    log.warn("incorrect response:", raw);

            } else {
                log.info("incorrect command");
            }
        }

        // We never get here but clean up anyhow
        requester.close();
        context.term();
    }
}