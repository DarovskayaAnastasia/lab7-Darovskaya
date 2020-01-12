import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public class ProxyServer {
    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);

//        //socket to talk to server
//        ZMQ.Socket requester = context.socket((SocketType.REQ));
//        requester.connect("tcp://localhost:5559");
    }
}
