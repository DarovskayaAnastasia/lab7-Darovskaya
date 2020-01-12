import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public class ProxyServer {
    

    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);

        // Socket facing clients
        ZMQ.Socket frontend = context.socket(SocketType.XSUB);
        frontend.bind("tcp://*:5559");

        //Socket facing services
        ZMQ.Socket backend = context.socket(SocketType.XPUB);
        backend.bind("tcp://*:5560");

        // Start the proxy
        ZMQ.proxy(frontend, backend, null);

        // We never get here but clean up anyhow
        frontend.close();
        backend.close();
        context.term();
    }
}
