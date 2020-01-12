import org.zeromq.*;

public class ProxyServer {
    private static final int FRONTEND_INDEX = 0;
    private static final int BACKEND_INDEX = 1;


    public static void main(String[] args) {
        System.out.println("(ProxyServer message): Launch Proxy...");

        ZMQ.Context context = ZMQ.context(0);

//        Socket facing clients
        ZMQ.Socket frontend = context.socket(SocketType.ROUTER);
        frontend.bind("tcp://localhost:5559");

//        Socket facing services
        ZMQ.Socket backend = context.socket(SocketType.ROUTER);
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

            }

            if (poller.pollin(BACKEND_INDEX)) {

            }

        }

//        We never get here but clean up anyhow
        frontend.close();
        backend.close();
        context.term();
    }
}
