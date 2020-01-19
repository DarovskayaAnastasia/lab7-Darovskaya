import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.HashMap;
import java.util.Map;

public class DistCacheStorage {
    public static final int HEARTBEAT_TIMEOUT = 3000;
    public static final String STORAGE_ADDRESS = "tcp://localhost:5556";

    private static int start;
    private static int end;
    private static Map<Integer, String> storage = new HashMap<>();

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("(DistCacheStorage message): ERROR, incorrect number of arguments");
            return;
        }

        int startCell = Integer.parseInt(args[0]);
        int endCell = Integer.parseInt(args[1]);
        Map<Integer, Integer> storage = new HashMap<>();

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(SocketType.DEALER);
        socket.connect(STORAGE_ADDRESS);
        System.out.println("(DistCacheStorage message): Storage connected to " + STORAGE_ADDRESS);

        long heartbeatTime = System.currentTimeMillis() + HEARTBEAT_TIMEOUT;

        System.out.println("(DistCacheStorage message): Storage has been started...");

//        endless loop
        while (!Thread.currentThread().isInterrupted()) {
//            if ( /* heartbeat checkout*/) {
//                // send heartbeat
//            }


            ZMsg message = ZMsg.recvMsg(socket, false);

            if (message != null) {
                Command command = new Command((message.getLast().toString()));
                System.out.println("(DistCacheStorage message): command type is " + command.getCommandType());

                if (command.getCommandType().equals(Command.GET_TYPE)) {
                    String value = storage.get(command.getKey());

                    message.getLast().reset("RESPONSE");
                    message.send(socket);
                }

                if (command.getCommandType().equals(Command.SET_TYPE)) {

                }
            }
        }

        context.close();
        context.term();
    }
}
