import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public class DistCacheStorage {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("(DistCacheStorage message): ERROR, incorrect number of arguments");
            return;
        }

        ZMQ.Context context = ZMQ.context(0);
        ZMQ.Socket socket = context.socket(SocketType.DEALER);
        socket.connect("tcp://localhost:5556");
    }
}
