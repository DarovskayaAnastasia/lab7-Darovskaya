public class Logger {
    private String prefix;

    public Logger(String prefix) {
        this.prefix = prefix + ":";
    }

    public void withPrefix(String prefix, String... strs){
        StringBuilder sb = new StringBuilder(this.prefix);
        if(!prefix.equals("")){
            sb.append(" ").append(prefix).append(":");
        }
        for (String str : strs) {
            sb.append(" ").append(str);
        }

        System.out.println(sb.toString());
    }

    public void log(String... strs) {
        withPrefix("", strs);
    }

    public void err(String... strs) {
        withPrefix("ERROR", strs);
    }

    public void info(String... strs) {
        withPrefix("INFO", strs);
    }
}


