import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import zmq.poll.Poller;

public class ProxyServer {


    public static void main(String[] args) {
        System.out.println("(ProxyServer message): Launch Proxy...");

        ZContext context = ZСontext(1);

        // Socket facing clients
        ZMQ.Socket frontend = context.socket(SocketType.ROUTER);
        frontend.bind("tcp://localhost:5559");

        //Socket facing services
        ZMQ.Socket backend = context.socket(SocketType.ROUTER);
        backend.bind("tcp:/localhost:5560");

        System.out.println("(ProxyServer message): Proxy has been started");

//        // Start the proxy
//        ZMQ.proxy(frontend, backend, null);

        Poller poller = context.createPoller(2);

        // We never get here but clean up anyhow
        frontend.close();
        backend.close();
        context.term();
    }
}
