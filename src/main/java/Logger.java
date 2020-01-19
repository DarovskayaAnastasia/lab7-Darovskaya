public class Logger {
    private String prefix;

    public Logger(String prefix){
        this.prefix = prefix;
    }

    public void log(String s) {
        System.out.println(prefix + ": " + s);
    }
}


