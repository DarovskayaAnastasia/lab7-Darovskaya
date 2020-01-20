import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.HashMap;
import java.util.Map;

public class DistCacheStorage {
    public static final int HEARTBEAT_TIMEOUT = 3000;
    public static final String STORAGE_ADDRESS = "tcp://localhost:5560";

    private static int start;
    private static int end;
    private static Map<Integer, String> storage = new HashMap<>();

    private final static Logger log = new Logger("(DistCacheStorage message)");


    public static void main(String[] args) {
        if (args.length < 2) {
            log.err("incorrect number of arguments");
            return;
        }

        int startCell = Integer.parseInt(args[0]);
        int endCell = Integer.parseInt(args[1]);
        Map<Integer, String> storage = new HashMap<>();

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(SocketType.DEALER);
        socket.connect(STORAGE_ADDRESS);
        log.info("Storage connected to", STORAGE_ADDRESS, "with cache in range", startCell, endCell);

        long heartbeatTime = System.currentTimeMillis() + HEARTBEAT_TIMEOUT;

        socket.send(new Command(Command.CONNECT_TYPE, startCell, endCell).encode(), 0);

        log.info("Storage has been started...");

//        endless loop
        while (!Thread.currentThread().isInterrupted()) {
            if (System.currentTimeMillis() >= heartbeatTime) {
                heartbeatTime = System.currentTimeMillis() + HEARTBEAT_TIMEOUT / 2;
                socket.send(new Command(Command.NOTIFY_TYPE, startCell, endCell).encode(), 0);
                log.info("heartbeat sent");
            }


            ZMsg message = ZMsg.recvMsg(socket, false);

            if (message != null) {
                Command command = new Command((message.getLast().toString()));
                log.info("received command ", command);

                if (command.typeCheck(Command.GET_TYPE)) {
                    String value = storage.get(command.getKey());

                    message.getLast().reset(new Command(Command.RESPONSE_TYPE, value).encode());
                    message.send(socket);
                }

                if (command.typeCheck(Command.SET_TYPE)) {
                    storage.put(command.getKey(), command.getValue());
                }
            }
        }

        context.close();
        context.term();
    }
}
