package icy.lib.client.book;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Template {
    private static final Pattern KEY = Pattern.compile("\\$\\{([a-z0-9_]+)}");

    private Template() {}

    static String fill(String raw, Function<String, String> state) {
        if (raw.indexOf('$') < 0) {
            return raw;
        }
        Matcher m = KEY.matcher(raw);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(out, Matcher.quoteReplacement(state.apply(m.group(1))));
        }
        m.appendTail(out);
        return out.toString();
    }

    static Set<String> expand(String raw, Map<String, Set<String>> domain) {
        Set<String> out = new LinkedHashSet<>();
        Matcher m = KEY.matcher(raw);
        if (!m.find()) {
            out.add(raw);
            return out;
        }
        String key = m.group(1);
        for (String v : domain.getOrDefault(key, Set.of())) {
            out.addAll(expand(raw.replace("${" + key + "}", v), domain));
        }
        return out;
    }
}
