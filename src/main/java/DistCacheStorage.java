import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

public class DistCacheStorage {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("(DistCacheStorage message): ERROR, incorrect number of arguments");
            return;
        }

        ZMQ.Context context = ZMQ.context(0);
        ZMQ.Socket socket = context.socket(SocketType.DEALER);
        socket.connect("tcp://localhost:5556");

        System.out.println("(DistCacheStorage message): Storage has been started...");

//        endless loop
        while (!Thread.currentThread().isInterrupted()) {
            if ( // heartbeat checkout
            ) {
                // send heartbeat
            }

            ZMsg message = ZMsg.recvMsg(socket, false);

            if (message != null) {
                Command command = new Command((message.getLast().toString()));
                System.out.println("(DistCacheStorage message): command type is " + command.getCommandType());

                if (command.getCommandType().equals(Command.GET_TYPE)) {

                }
                if (command.getCommandType().equals(Command.SET_TYPE)) {

                }
            }
        }
    }
}
