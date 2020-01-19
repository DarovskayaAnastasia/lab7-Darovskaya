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
                String response = requester.recvStr(0);

                log.info("response:", response);

            } else {
                log.info("incorrect command");
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
    static final String RESPONSE_TYPE = "RESPONSE";

    private String commandType;
    private String[] args;

    private Pattern intPattern = Pattern.compile("\\d+$");

    private boolean isInt(String s) {
        return intPattern.matcher(s).find();
    }

    private void parseSET(String cmd) {
        if (cmd.equals(SET_TYPE) && args.length == 2 && isInt(args[0]))
            commandType = SET_TYPE;
    }

    private void parseGET(String cmd) {
        if (cmd.equals(GET_TYPE) && args.length == 1 && isInt(args[0]))
            commandType = GET_TYPE;
    }

    private void parseNOTIFY(String cmd) {
        if (cmd.equals(NOTIFY_TYPE) && args.length == 2 && isInt(args[0]) && isInt(args[1]))
            commandType = NOTIFY_TYPE;
    }

    private void parseCONNECT(String cmd) {
        if (cmd.equals(CONNECT_TYPE) && args.length == 2 && isInt(args[0]) && isInt(args[1]))
            commandType = CONNECT_TYPE;
    }

    private void parseRESPONSE(String cmd) {
        if (cmd.equals(RESPONSE_TYPE) && args.length == 1)
            commandType = RESPONSE_TYPE;
    }

    private void parse(String keyword, String... args) {
        this.args = args;
        commandType = INCORRECT_TYPE;
        parseSET(keyword);
        parseGET(keyword);
        parseNOTIFY(keyword);
        parseCONNECT(keyword);
    }

    public Command(String command) {
        String[] parsedCommand = command.split(" ");
        parse(parsedCommand[0], Arrays.copyOfRange(parsedCommand, 1, parsedCommand.length));
    }

    public Command(String keyword, String... args) {
        parse(keyword, args);
    }

    public Command(String keyword, Object... args) {
        String[] strs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            strs[i] = args[i].toString();
        }
        System.out.println(keyword+ Arrays.toString(args));
        parse(keyword, strs);
    }

    public boolean typeCheck(String... types) {
        for (String t : types) {
            if (this.commandType.equals(t)) return true;
        }
        return false;
    }

    public int getKey() {
        if (!typeCheck(SET_TYPE, GET_TYPE)) return -1;
        return Integer.parseInt(args[0]);
    }

    public String getValue() {
        if (!typeCheck(SET_TYPE)) return "";
        return args[1];
    }

    public int getBegin() {
        if (!typeCheck(CONNECT_TYPE, NOTIFY_TYPE)) return -1;
        return Integer.parseInt(args[0]);
    }

    public int getEnd() {
        if (!typeCheck(CONNECT_TYPE, NOTIFY_TYPE)) return -1;
        return Integer.parseInt(args[1]);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(commandType).append(": ");
        for (String arg : args) {
            sb.append(arg).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    public String encode() {
        StringBuilder sb = new StringBuilder(commandType).append(" ");
        for (String arg : args) {
            sb.append(arg).append(" ");
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }
}
