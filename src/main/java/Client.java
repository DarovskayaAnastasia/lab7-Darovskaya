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
            if (cmd.equals("QUIT")) {
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
    static final String CONNECT_TYPE = "CONNECT";
    static final String SET_TYPE = "SET";
    static final String GET_TYPE = "GET";

    private String commandType;
    private String[] args;

    private Pattern intPattern = Pattern.compile("\\d+$");

    private boolean isInt(String s) {
        return intPattern.matcher(s).find();
    }

    private void parseSET(String... args) {
        if (args.length == 2 && isInt(args[0])) {
            commandType = SET_TYPE;
        }
    }

    private void parseGET(String... args) {
        if (args.length == 1 && isInt(args[0])) {
            commandType = GET_TYPE;
            key = Integer.parseInt(args[0]);
        }
    }

    private void parseNOTIFY(String... args) {
        if (args.length == 2 && isInt(args[0]) && isInt(args[1])) {
            commandType = NOTIFY_TYPE;
            begin = Integer.parseInt(args[0]);
            end = Integer.parseInt(args[1]);
        }
    }

    private void parseCONNECT(String... args) {
        if (args.length == 2 && isInt(args[0]) && isInt(args[1])) {
            commandType = CONN;
            begin = Integer.parseInt(args[0]);
            end = Integer.parseInt(args[1]);
        }
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

    private boolean typeCheck( String ...types){
        for (String t: types) {
            if (this.commandType.equals(t)) return true;
        }
        return false;
    }


    private defaultIfType()

    public int getKey() {
        if (!typeCheck(SET_TYPE, GET_TYPE)){
            return 0;
        }
        return Integer.parseInt(args[0]);
    }

    public String getValue() {
        return args[1];
    }

    public int getBegin() {
        return Integer.parseInt(args[0]);
    }
    public int getEnd() {
        return Integer.parseInt(args[1]);
    }


    @Override
    public String toString() {
        return commandType + " " + key + " " + value;
    }
}
