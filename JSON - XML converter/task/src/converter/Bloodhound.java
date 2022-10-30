package converter;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class Bloodhound {

    @Getter
    private class BloodhoundMatcher {
        private final Matcher matcher;

        public BloodhoundMatcher(Pattern pattern) {
            matcher = pattern
                         .matcher(source)
                        .useAnchoringBounds(true);
        }

        public boolean find() {
            if (check()) {
                cursor = matcher.end();
                return true;
            }
            return false;
        }

        public boolean check() {
            return matcher
                        .region(cursor,
                                source.length()).find();
        }

    }

    private int cursor;
    private Matcher matcher;
    private final String source;
    private final Map<Pattern, BloodhoundMatcher> bloodhoundM = new HashMap<>();

    public Bloodhound(String source) {
        this(source, 0);
    }

    public Bloodhound(String source, int cursor) {
        this.source = source;
        this.cursor = cursor;
    }

    private BloodhoundMatcher getBloodhoundMatcher(Pattern pattern) {
        if (!bloodhoundM.containsKey(pattern)) {
            bloodhoundM.put(pattern,
                            new BloodhoundMatcher(pattern));
        }
        return bloodhoundM.get(pattern);
    }

    public boolean next(Pattern pattern) {
        var findMatcher = getBloodhoundMatcher(pattern);
        matcher = findMatcher.find() ?
                  findMatcher.getMatcher() : null;
        return matcher != null;
    }

    public boolean next(String pattern) {
        var findMatcher = new BloodhoundMatcher(
                Pattern.compile(pattern));
        matcher = findMatcher.find() ?
                findMatcher.getMatcher() : null;
        return matcher != null;
    }

    public boolean check(Pattern pattern) {
        var findMatcher = getBloodhoundMatcher(pattern);
        matcher = findMatcher.check() ?
                findMatcher.getMatcher() : null;
        return matcher != null;
    }
}
