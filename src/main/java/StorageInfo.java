import org.zeromq.ZFrame;

public class StorageInfo {
    public static final int HEARTBEAT_TIMEOUT = 3000;

    private ZFrame address;
    private int start;
    private int end;
    private long timer;

    public StorageInfo(ZFrame address, int start, int end, long timer) {
        this.address = address;
        this.start = start;
        this.end = end;
        this.timer = timer;
    }

    public ZFrame getAddress() {
        return address;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public long getTimer() {
        return timer;
    }

    public void setTimer(long timer) {
        this.timer = timer;
    }

    public boolean isDead() {
        return timer + HEARTBEAT_TIMEOUT < System.currentTimeMillis();
    }
}

