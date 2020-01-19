public class Logger {
    private String prefix;

    public Logger(String prefix){
        this.prefix = prefix+":";
    }

    public void log(String ... strs) {
        StringBuilder sb = new StringBuilder(prefix);
        for (String str : strs) {
            sb.append(" ").append(str);
        }
    }
}


