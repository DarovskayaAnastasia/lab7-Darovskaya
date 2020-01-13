import org.zeromq.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyServer {
    private static final int FRONTEND_INDEX = 0;
    private static final int BACKEND_INDEX = 1;

    private static ZMQ.Socket frontend;
    private static ZMQ.Socket backend;

//    private static final Map<String, StorageInfo> storages = new HashMap<>();
    private static final List<StorageInfo> storages = new ArrayList<>();

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
        System.out.println("(ProxyServer message): Launch Proxy...");

        ZMQ.Context context = ZMQ.context(0);

//        Socket facing clients
        frontend = context.socket(SocketType.ROUTER);
        frontend.bind("tcp://localhost:5559");

//        Socket facing services
        backend = context.socket(SocketType.ROUTER);
        backend.bind("tcp:/localhost:5560");

//        ZMQ.proxy(frontend, backend, null);

//        Start the proxy
        ZMQ.Poller poller = context.poller(2);
        poller.register(frontend, ZMQ.Poller.POLLIN);
        poller.register(backend, ZMQ.Poller.POLLIN);
        System.out.println("(ProxyServer message): Proxy has been started");

//        Endless loop
        while (!Thread.currentThread().isInterrupted()) {
            poller.poll();

//          here we'll remove idle storages

            if (poller.pollin(FRONTEND_INDEX)) {
                ZMsg message = ZMsg.recvMsg(frontend);

                Command command = new Command(message.getLast().toString());

                if (command.getCommandType().equals(Command.GET_TYPE)) {
                    if (!sendRequest(command, message)) {
                        message.getLast().reset();
                    }
                }

                if (command.getCommandType().equals(Command.SET_TYPE)) {

                }
            }

            if (poller.pollin(BACKEND_INDEX)) {
                ZMsg message = ZMsg.recvMsg(frontend);

                Command command = new Command(message.getLast().toString());

                if (command.getCommandType().equals(Command.NOTIFY_TYPE)) {

                } else if (!command.getCommandType().equals(Command.INCORRECT_TYPE)) {

                }
            }

        }

//        We never get here but clean up anyhow
        frontend.close();
        backend.close();
        context.term();
    }
}
