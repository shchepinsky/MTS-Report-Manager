package loader;

import java.util.regex.Pattern;

/**
 * This is abstract class for some common constants
 *
 * @author sdv
 */
abstract class Const {
    static final int MAX_LINE_LEN = 1024;
    static final String DATE_PATTERN = "dd.MM.yyyy";
    static final String TIME_PATTERN = "HH:mm:ss";
    static final int REGEXP_PATTERN_FLAGS = Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE;
}
