import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import java.util.Scanner;
import java.util.regex.Pattern;

public class Client {
    public static void main(String[] args) {

        ZMQ.Context context = ZMQ.context(1);

        // socket to talk to server
        ZMQ.Socket requester = context.socket(SocketType.REQ);
        requester.connect("tcp://localhost:5559");

        System.out.println("(Client message): Launch and connect client...");
        System.out.println("--- Enter QUIT for exit");

        Scanner input = new Scanner(System.in);

        // endless loop
        while (true) {
            String cmd = input.nextLine();
            if(cmd.equals("QUIT")){
                System.out.println("--- QUIT");
                break;
            }
            Command command = new Command(cmd);

            if (command.getCommandType().equals(Command.INCORRECT_TYPE)) {
                System.out.println("(Client message): incorrect command");

            } else if (command.getCommandType().equals(Command.SET_TYPE)) {
                System.out.println("(Client message): " + command.getCommandType() + " command accepted for processing");

                requester.send(command.toString(), 0);
                String response = requester.recvStr(0);

                System.out.println("(Client message): response: " + response);

            }
        }

        // We never get here but clean up anyhow
        requester.close();
        context.term();
    }
}

class Command {
    static final String INCORRECT_TYPE = "INCORRECT";
    static final String NOTIFY_TYPE = "NOTIFY";
    static final String SET_TYPE = "SET";
    static final String GET_TYPE = "GET";

    private String commandType;
    private int key;
    private String value;

    public Command(String commandType, int key, String value) {
        this.commandType = commandType;
        this.key = key;
        this.value = value;
    }

    private boolean parseSET(String ...args) {
        if (args.length == 3 && Pattern.compile("\\d+$").matcher(args[1]).find()) {
            commandType = SET_TYPE;
            key = Integer.parseInt(args[1]);
            value = args[2];
            return true;
        }
        return false;
    }

    public Command(String command) {
        String[] parsedCommand = command.split(" ");
        String keyword = parsedCommand[0];
        commandType = INCORRECT_TYPE;

        if (keyword.equals(SET_TYPE)) {
            if (parsedCommand.length == 3 && Pattern.compile("\\d+$").matcher(parsedCommand[1]).find()) {
                commandType = SET_TYPE;
                key = Integer.parseInt(parsedCommand[1]);
                value = parsedCommand[2];
            }
        } else if (keyword.equals(GET_TYPE)) {
            if (parsedCommand.length == 2 && Pattern.compile("\\d+$").matcher(parsedCommand[1]).find()) {
                commandType = GET_TYPE;
                key = Integer.parseInt(parsedCommand[1]);
            }
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

    @Override
    public String toString() {
        return commandType + " " + key + " " + value;
    }
}
