public class Logger {
    private String prefix;

    public Logger(String prefix) {
        this.prefix = prefix + ":";
    }

    public void withPrefix(String prefix, Object... objs) {
        StringBuilder sb = new StringBuilder(this.prefix);
        if (!prefix.equals("")) {
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

    public void err(Object... objs) {
        withPrefix("ERROR", objs);
    }

    public void info(Object... objs) {
        withPrefix("INFO", objs);
    }

    public void warn(Object... objs) {
        withPrefix("WARN", objs);
    }
}


