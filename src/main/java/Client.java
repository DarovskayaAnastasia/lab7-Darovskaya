import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Scanner;
import java.util.regex.Pattern;

public class Client {
    public static void main(String[] args) {

        ZMQ.Context context = ZMQ.context(1);

        // socket to talk to server
        ZMQ.Socket requester = context.socket(SocketType.REQ);
        requester.connect("tcp://localhost:5559");

        System.out.println("Launch and connect client...");

        Scanner input = new Scanner(System.in);

        // endless loop
        while (true) {
            Command command = new Command(input.nextLine());

            if (command.getCommandType().equals(Command.INCORRECT_TYPE)) {
                System.out.println("incorrect command");
                break;
            }

            requester.send(command.toString(), 0);
            String response = requester.recvStr(0);

            System.out.println("response: " + response);
        }

        // We never get here but clean up anyhow
        requester.close();
        context.term();
    }
}

class Command {
    static final String INCORRECT_TYPE = "INCORRECT";
    static final String NOTIFY_TYPE = "NOTIFY";
    static final String GET_TYPE = "GET";
    static final String SET_TYPE = "SET";

    private String commandType;
    private int key;
    private String value;

    public Command(String commandType, int key, String value) {
        this.commandType = commandType;
        this.key = key;
        this.value = value;
    }

    public Command(String command) {
        String[] parsedCommand = command.split(" ");
        String keyword = parsedCommand[0];

        if (keyword.equals("SET")) {
            if (parsedCommand.length == 3 && Pattern.compile("\\d+$").matcher(parsedCommand[1]).find()) {
                commandType = SET_TYPE;
                key = Integer.parseInt(parsedCommand[1]);
                value = parsedCommand[2];
            }
        } else if (keyword.equals("GET")) {
            if (parsedCommand.length == 2 && Pattern.compile("\\d+$").matcher(parsedCommand[1]).find()) {
                commandType = GET_TYPE;
                key = Integer.parseInt(parsedCommand[1]);
            }
        } else {
            commandType = INCORRECT_TYPE;
        }
    }

    public String getCommandType() {
        return commandType;
    }

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
