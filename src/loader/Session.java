package loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Session class contains single entry of SMS exchange or call information with
 * all associated data like remote number, remote vendor if any specified, date,
 * time and duration of session and session type, which states if it is an
 * incoming/outgoind and SMS/Call.
 *
 * @author sdv
 */
public class Session {
    static final String SESSION_REGEX = "^,,\\s*([ \\S]*)\\s*,\\s*([ \\S]*)\\s*,\\s*([ \\S]*)\\s*,\\s*(\\d{2}.\\d{2}.\\d{4})\\s*,\\s*(\\d{2}:\\d{2}:\\d{2})\\s*,\\s*([ \\S]*)\\s*,\\s*([ \\S]*)\\s*,\\s*([ \\S]*)\\s*$";

    String remoteNumber = null;
    String remoteVendor = null;
    LocalDate date = null;
    LocalTime time = null;
    LocalTime duration = null;
    SessionType type = SessionType.INVALID;
    SessionDirection direction = SessionDirection.INVALID;

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public LocalTime getDuration() {
        return duration;
    }

    public String getRemoteVendor() {
        return remoteVendor;
    }

    public String getRemoteNumber() {
        return remoteNumber;
    }

    public SessionType getType() {
        return type;
    }

    public SessionDirection getDirection() {
        return direction;
    }


    /**
     * This function checks that all session fields are set and have valid
     * values
     *
     * @return true if session data is valid or false otherwise
     */
    boolean isValid() {
        switch (type) {
            case SMS:
                return (remoteVendor != null && remoteNumber != null
                        && !remoteNumber.isEmpty()
                        && direction != SessionDirection.INVALID
                        && date != null && time != null);
            case CALL:
                return (remoteVendor != null && remoteNumber != null
                        && !remoteNumber.isEmpty()
                        && direction != SessionDirection.INVALID
                        && date != null && time != null && duration != null);
            case INVALID:
            default:
                return false;
        }
    }

    ParseResult parseFromArrayList(List<String> list, Integer marker) {
        Pattern p;
        Matcher m;

        String line;

        while (marker < list.size()) {
            line = list.get(marker);
            marker++;

            try {
                // skip empty and non-session lines
                if (line.isEmpty() || !line.startsWith(",,")) {
                    continue;
                }

                p = Pattern.compile(SESSION_REGEX, Const.REGEXP_PATTERN_FLAGS);
                m = p.matcher(line);

                if (m.find()) {
                    // if session string parsed through regexp then fill data and break
                    if (m.group(1).toLowerCase().contains("повідом"))
                        type = SessionType.SMS;

                    if (m.group(1).toLowerCase().contains("дзвінки"))
                        type = SessionType.CALL;

                    if (m.group(1).toLowerCase().contains("вхідні"))
                        direction = SessionDirection.INCOMING;

                    if (m.group(1).toLowerCase().contains("вихідні"))
                        direction = SessionDirection.OUTGOING;

                    remoteVendor = m.group(2);  // extract matched groups from regexp result
                    remoteNumber = m.group(3);  //

                    remoteVendor = remoteVendor.replaceAll("^[\"'\\s]*", ""); // trim & unquote start of string
                    remoteVendor = remoteVendor.replaceAll("[\\s\"']*$", ""); // trim & unquote end of string

                    remoteNumber = remoteNumber.replaceAll("^[\"'\\s]*", ""); // trim & unquote start of string
                    remoteNumber = remoteNumber.replaceAll("[\\s\"']*$", ""); // trim & unquote end of string

                    date = LocalDate.parse(m.group(4), DateTimeFormatter.ofPattern(Const.DATE_PATTERN));
                    time = LocalTime.parse(m.group(5), DateTimeFormatter.ofPattern(Const.TIME_PATTERN));

                    // if CALL parse duration as LocalTime, otherwise it is a string 1 MSG
                    if (type == SessionType.CALL)
                        duration = LocalTime.parse(m.group(6), DateTimeFormatter.ofPattern(Const.TIME_PATTERN));

                    // return true only if data is valid
                    return new ParseResult(marker, isValid());
                }

            } catch (Throwable e) {
                // catch Exception and show malformed line
                System.out.format("error parsing: %s\n", line);
                System.out.format("exception: %s\n", e.toString());
            }
        }

        return new ParseResult(marker, isValid());
    }

    /**
     * Loads session data from given buffer
     *
     * @param br buffer to read from
     * @return returns true if valid data has been read or false otherwise
     * @throws IOException
     */
    boolean loadFromBuffer(BufferedReader br) throws IOException {
        Pattern p;
        Matcher m;

        String line;
        br.mark(Const.MAX_LINE_LEN);
        while ((line = br.readLine()) != null) {
            try {
                // skip empty and non-session lines
                if (line.isEmpty() || !line.startsWith(",,")) {
                    continue;
                }

                p = Pattern.compile(SESSION_REGEX, Const.REGEXP_PATTERN_FLAGS);
                m = p.matcher(line);

                if (m.find()) {
                    // if session string parsed through regexp then fill data and break
                    if (m.group(1).toLowerCase().contains("повідом"))
                        type = SessionType.SMS;

                    if (m.group(1).toLowerCase().contains("дзвінки"))
                        type = SessionType.CALL;

                    if (m.group(1).toLowerCase().contains("вхідні"))
                        direction = SessionDirection.INCOMING;

                    if (m.group(1).toLowerCase().contains("вихідні"))
                        direction = SessionDirection.OUTGOING;

                    remoteVendor = m.group(2);  // extract matched groups from regexp result
                    remoteNumber = m.group(3);  //

                    remoteVendor = remoteVendor.replaceAll("^[\"'\\s]*", ""); // trim & unquote start of string
                    remoteVendor = remoteVendor.replaceAll("[\\s\"']*$", ""); // trim & unquote end of string

                    remoteNumber = remoteNumber.replaceAll("^[\"'\\s]*", ""); // trim & unquote start of string
                    remoteNumber = remoteNumber.replaceAll("[\\s\"']*$", ""); // trim & unquote end of string

                    date = LocalDate.parse(m.group(4), DateTimeFormatter.ofPattern(Const.DATE_PATTERN));
                    time = LocalTime.parse(m.group(5), DateTimeFormatter.ofPattern(Const.TIME_PATTERN));

                    // if CALL parse duration as LocalTime, otherwise it is a string 1 MSG
                    if (type == SessionType.CALL)
                        duration = LocalTime.parse(m.group(6), DateTimeFormatter.ofPattern(Const.TIME_PATTERN));

                    // return true only if data is valid
                    return isValid();
                }

            } catch (Throwable e) {
                // catch Exception and show malformed line
                System.out.format("error parsing: %s\n", line);
                System.out.format("exception: %s\n", e.toString());
            }
        }

        return isValid();
    }

    /**
     * Prints session data to standard console out. Used mainly for debugging purposes.
     */
    void dumpToConsole() {
        switch (type) {
            case CALL:
                System.out.format("%-4s | %-18s | %-14s | %-10s | %-8s | %-8s\n",
                        type.toString(), remoteVendor, remoteNumber,
                        date.format(DateTimeFormatter.ofPattern(Const.DATE_PATTERN)),
                        time.format(DateTimeFormatter.ofPattern(Const.TIME_PATTERN)),
                        duration.format(DateTimeFormatter.ofPattern(Const.TIME_PATTERN)));
                return;
            case SMS:
                System.out.format("%-4s | %-18s | %-14s | %-10s | %-8s | %-8s\n",
                        type.toString(), remoteVendor, remoteNumber,
                        date.format(DateTimeFormatter.ofPattern(Const.DATE_PATTERN)),
                        time.format(DateTimeFormatter.ofPattern(Const.TIME_PATTERN)),
                        "1 SMS");
                return;
            case INVALID:
            default:
                return;
        }
    }

    /**
     * closes
     */
    void close() {
        remoteNumber = null;
        remoteVendor = null;
        date = null;
        time = null;
        duration = null;
        type = SessionType.INVALID;
        direction = SessionDirection.INVALID;
    }
}