import org.zeromq.SocketType;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.ArrayList;
import java.util.List;

public class ProxyServer {
    private static final int FRONTEND_INDEX = 0;
    private static final int BACKEND_INDEX = 1;

    private static ZMQ.Socket frontend;
    private static ZMQ.Socket backend;

    //    private static final Map<String, StorageInfo> storages = new HashMap<>();
    private static final List<StorageInfo> storages = new ArrayList<>();

    private static final Logger log = new Logger("(ProxyServer message)");


    private static boolean sendRequest(Command command, ZMsg message) {
        int key = command.getKey();

//        for (Map.Entry<String, StorageInfo> record : storages.entrySet()) {
//            StorageInfo info = record.getValue();
        for (StorageInfo info : storages) {
            if (info.getStart() <= key && key <= info.getEnd()) {
                info.getAddress().send(backend, ZFrame.REUSE + ZFrame.MORE);
                message.send(backend, false);
                return true;
            }
        }

        return false;
    }

    public static void main(String[] args) {
        log.info("Launch Proxy...");

        ZMQ.Context context = ZMQ.context(1);

//        Socket facing clients
        frontend = context.socket(SocketType.ROUTER);
        frontend.bind("tcp://localhost:5559");

//        Socket facing services
        backend = context.socket(SocketType.ROUTER);
        backend.bind("tcp://localhost:5560");

//        ZMQ.proxy(frontend, backend, null);

//        Start the proxy
        ZMQ.Poller poller = context.poller(2);
        poller.register(frontend, ZMQ.Poller.POLLIN);
        poller.register(backend, ZMQ.Poller.POLLIN);
        log.info("Proxy has been started");

//        Endless loop
        while (!Thread.currentThread().isInterrupted()) {
            poller.poll();

//          here we'll remove idle storages

            if (poller.pollin(FRONTEND_INDEX)) {
                ZMsg message = ZMsg.recvMsg(frontend);
                Command command = new Command(message.getLast().toString());

                if (command.typeCheck(Command.GET_TYPE)) {
                    if (!sendRequest(command, message)) {
                        message.getLast().reset("Out of bounds cache");
                        message.send(frontend);
                    }
                }

                if (command.typeCheck(Command.SET_TYPE)) {
                    if (!sendRequest(command, message)) {
                        log.warn("Out of bounds cache");
                    }

                    ZMsg responseMessage = new ZMsg();
                    responseMessage.add(new ZFrame("DATA recorded"));
                    responseMessage.wrap(message.getFirst());
                    responseMessage.send(frontend);
                    log.info("SET is done");

                }
            }

            if (poller.pollin(BACKEND_INDEX)) {

                ZMsg message = ZMsg.recvMsg(backend);

                ZFrame id = message.unwrap();
//                String id = new String(address.getData(), ZMQ.CHARSET);

                Command command = new Command(message.getLast().toString());
                log.info("adsa", command);
                if (command.typeCheck(Command.NOTIFY_TYPE)) {
                    for (StorageInfo info : storages) {
                        if (info.getAddress().equals(id)) {
                            info.setTimer(System.currentTimeMillis());
                        }
                    }

                } else if (!command.typeCheck(Command.INCORRECT_TYPE)) {
                    message.send(frontend);
                }

                if (command.typeCheck(Command.CONNECT_TYPE)) {
                    log.info("new cache by cmd", command.toString());
                    for (StorageInfo info : storages) {
                        if (info.getAddress().equals(id)) {
                            info.setTimer(System.currentTimeMillis());
                        }
                    }

                } else if (!command.typeCheck(Command.INCORRECT_TYPE)) {
                    message.send(frontend);
                }
            }

            // storages.removeIf(StorageInfo::isDead);

        }

//        We never get here but clean up anyhow
        frontend.close();
        backend.close();
        context.term();
    }
}
