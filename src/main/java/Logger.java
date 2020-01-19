public class Logger {
    private String prefix;

    public Logger(String prefix) {
        this.prefix = prefix + ":";
    }

    public void withPrefix(String prefix, Object... objs){
        StringBuilder sb = new StringBuilder(this.prefix);
        if(!prefix.equals("")){
            sb.append(" ").append(prefix).append(":");
        }
        for (Object o : objs) {
            sb.append(" ").append(o);
        }

        System.out.println(sb.toString());
    }

//    private void log(String... strs) {
//        withPrefix("", strs);
//    }

    public void err(Object... strs) {
        withPrefix("ERROR", strs);
    }

    public void info(Object... strs) {
        withPrefix("INFO", strs);
    }

    public void warn(Object... strs) {
        withPrefix("WARN", strs);
    }
}


