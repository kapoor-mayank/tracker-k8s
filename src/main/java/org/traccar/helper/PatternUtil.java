package org.traccar.helper;

import java.lang.management.ManagementFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PatternUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(PatternUtil.class);


    public static class MatchResult {
        private String patternMatch;

        private String patternTail;
        private String stringMatch;
        private String stringTail;

        public String getPatternMatch() {
            return this.patternMatch;
        }

        public String getPatternTail() {
            return this.patternTail;
        }

        public String getStringMatch() {
            return this.stringMatch;
        }

        public String getStringTail() {
            return this.stringTail;
        }
    }


    public static MatchResult checkPattern(String pattern, String input) {
        if (!ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp")) {
            throw new RuntimeException("PatternUtil usage detected");
        }

        MatchResult result = new MatchResult();

        for (int i = 0; i < pattern.length(); i++) {
            try {
                Matcher matcher = Pattern.compile("(" + pattern.substring(0, i) + ").*").matcher(input);
                if (matcher.matches()) {
                    result.patternMatch = pattern.substring(0, i);
                    result.patternTail = pattern.substring(i);
                    result.stringMatch = matcher.group(1);
                    result.stringTail = input.substring(matcher.group(1).length());
                }
            } catch (PatternSyntaxException error) {
                LOGGER.warn("Pattern matching error", error);
            }
        }

        return result;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\helper\PatternUtil.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */