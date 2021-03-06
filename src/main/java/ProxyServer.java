import org.zeromq.SocketType;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.ArrayList;
import java.util.List;

public class ProxyServer {
    private static final int FRONTEND_INDEX = 0;
    private static final int BACKEND_INDEX = 1;
    private static final String FRONTEND_ADDR = "tcp://localhost:5559";
    private static final String BACKEND_ADDR = "tcp://localhost:5560";
    private static ZMQ.Socket frontend;
    private static ZMQ.Socket backend;

    //    private static final Map<String, StorageInfo> storages = new HashMap<>();
    private static final List<StorageInfo> storages = new ArrayList<>();

    private static final Logger log = new Logger("(ProxyServer message)");


    private static boolean sendRequest(Command command, ZMsg message) {
        int key = command.getKey();

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
        frontend.bind(FRONTEND_ADDR);

//        Socket facing services
        backend = context.socket(SocketType.ROUTER);
        backend.bind(BACKEND_ADDR);

//        ZMQ.proxy(frontend, backend, null);

//        Start the proxy
        ZMQ.Poller poller = context.poller(2);
        poller.register(frontend, ZMQ.Poller.POLLIN);
        poller.register(backend, ZMQ.Poller.POLLIN);
        log.info("Proxy has been started");

//        Endless loop
        while (!Thread.currentThread().isInterrupted()) {
            poller.poll();

            if (poller.pollin(FRONTEND_INDEX)) {
                ZMsg message = ZMsg.recvMsg(frontend);
                Command command = new Command(message.getLast().toString());
                log.info("received command from client", command);

                if (command.typeCheck(Command.GET_TYPE, Command.SET_TYPE)) {
                    if (!sendRequest(command, message)) {
                        log.warn("Out of bounds cache");
                        message.getLast().reset(new Command(Command.RESPONSE_TYPE, "Out of bounds cache").encode());
                        message.send(frontend);
                    } else if (command.typeCheck(Command.SET_TYPE)) {
                        message.getLast().reset(new Command(Command.RESPONSE_TYPE, "DATA recorded").encode());
                        message.send(frontend);
                    }
                }
            }

            if (poller.pollin(BACKEND_INDEX)) {
                ZMsg message = ZMsg.recvMsg(backend);
                ZFrame id = message.unwrap();
                Command command = new Command(message.getLast().toString());
                log.info("received command from cache", command);

                if (command.typeCheck(Command.NOTIFY_TYPE)) {
                    for (StorageInfo info : storages) {
                        if (info.getAddress().equals(id)) {
                            info.setTimer(System.currentTimeMillis());
                        }
                    }
                }

                if (command.typeCheck(Command.CONNECT_TYPE)) {
                    log.info("new cache by cmd", command);
                    storages.add(new StorageInfo(id, command.getBegin(), command.getEnd(), System.currentTimeMillis()));
                    log.info("added", id);
                }

                if (command.typeCheck(Command.RESPONSE_TYPE)) {
                    message.send(frontend);
                }
            }

//          here we'll remove idle storages

            storages.removeIf(StorageInfo::isDead);
        }

//        We never get here but clean up anyhow
        frontend.close();
        backend.close();
        context.term();
    }
}
