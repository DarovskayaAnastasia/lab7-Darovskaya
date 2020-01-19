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
        Logger log = new Logger("(DistCacheStorage message)");
        if (args.length < 2) {
            log.err("incorrect number of arguments");
            return;
        }

        int startCell = Integer.parseInt(args[0]);
        int endCell = Integer.parseInt(args[1]);
        Map<Integer, Integer> storage = new HashMap<>();

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(SocketType.DEALER);
        socket.connect(STORAGE_ADDRESS);
        log.info("Storage connected to", STORAGE_ADDRESS);

        long heartbeatTime = System.currentTimeMillis() + HEARTBEAT_TIMEOUT;

        socket.send()

        log.info("Storage has been started...");

//        endless loop
        while (!Thread.currentThread().isInterrupted()) {
//            if ( /* heartbeat checkout*/) {
//                // send heartbeat
//            }


            ZMsg message = ZMsg.recvMsg(socket, false);

            if (message != null) {
                Command command = new Command((message.getLast().toString()));
                log.info("command is", command.toString());

                if (command.typeCheck(Command.GET_TYPE)) {
                    // String value = storage.get(command.getKey());

                    message.getLast().reset("RESPONSE");
                    message.send(socket);
                }

                if (command.typeCheck(Command.SET_TYPE)) {

                }
            }
        }

        context.close();
        context.term();
    }
}
