package loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Person class contains person-wide information like phone number, contract number, call details, etc
 *
 * @author sdv
 */
public class Person {
    static final String CONTRACT_REGEX = "^контракт №\\s*(\\d*)\\s*";
    static final String NUMBER_REGEX = "^контракт №\\s*\\d*\\s*номер телефону:\\s*(\\d*)";
    static final String TOTAL_EXPENSES_REGEX = "^загалом за контрактом[^,]*,*(-?\\d*.\\d*),*";
    static final String PLAN_PRICE_REGEX = "^вартість пакета/щомісячна \\S*:,\\-?\\d*.?\\d*,\\-?\\d*.?\\d*,(\\-?\\d*.?\\d*)";
    static final String PLAN_NAME_REGEX = "^ціновий пакет:\\s*([^,]*)\\s*(,*)";

    String number = null;
    String contract = null;
    String planName = null;

    Float planPrice = null;
    Float total = null;

    ArrayList<Session> sessions = new ArrayList<>();

    public String getNumber() {
        return number;
    }

    public String getContract() {
        return contract;
    }

    public String getPlanName() {
        return planName;
    }

    public Float getPlanPrice() {
        return planPrice;
    }

    public Float getTotal() {
        return total;
    }

    /**
     * getter for sessions property value
     *
     * @return sessions property value
     */
    public ArrayList<Session> getSessions() {
        return sessions;
    }

    /**
     * dumps person data to standard out
     */
    public void dumpToConsole() {
        System.out.format(
                "%s | %s | %s | %.2f | %.2f\n",
                contract, number, planName, planPrice, total);
    }

    /**
     * Loads person block from string list
     *
     * @param list   provides data to be loaded and parsed
     * @param marker indicates position in list to start reading from
     * @return
     */
    ParseResult loadFromList(List<String> list, Integer marker) {
        Matcher m;
        String line;

        // mark this line for possible reset
        //br.mark(Const.MAX_LINE_LEN);
        while (marker < list.size()) {
            line = list.get(marker);
            marker++;

            m = Pattern.compile(CONTRACT_REGEX, Const.REGEXP_PATTERN_FLAGS).matcher(line);
            if (m.find()) {
                if ((contract == null)) {
                    // this is first occurrence of contract - parse them
                    contract = m.group(1);
                } else {
                    // second occurrence of contract - this is probably a start of next
                    // person block, so step back one line and return
                    marker--;
                    return new ParseResult(marker, isValid());
                }
            }

            m = Pattern.compile(NUMBER_REGEX, Const.REGEXP_PATTERN_FLAGS).matcher(line);
            if (m.find()) {
                if ((number == null)) {
                    number = m.group(1);
                    continue;
                } else {
                    // second occurrence of contract and number - this is probably
                    // a start of next person block, so step back one line and return
                    marker--;
                    return new ParseResult(marker, isValid());
                }
            }

            m = Pattern.compile(PLAN_NAME_REGEX, Const.REGEXP_PATTERN_FLAGS).matcher(line);
            if (m.find()) {
                planName = m.group(1);
                continue;
            }

            // call/sms sessions are prefixed with double comma
            if (line.startsWith(",,")) {
                marker--;   // step back to previous line and pass buffer further

                // try parsing from list
                Session session = new Session();
                ParseResult result = session.parseFromArrayList(list, marker);
                marker = result.marker;

                if (result.success)
                    sessions.add(session);
            }

            try {
                m = Pattern.compile(TOTAL_EXPENSES_REGEX, Const.REGEXP_PATTERN_FLAGS).matcher(line);
                if (m.find()) {
                    total = Float.parseFloat(m.group(1));
                    continue;
                }

                m = Pattern.compile(PLAN_PRICE_REGEX, Const.REGEXP_PATTERN_FLAGS).matcher(line);
                if (m.find()) {
                    planPrice = Float.parseFloat(m.group(1));
                    continue;
                }
            } catch (NumberFormatException e) {
                // catch exceptions and show malformed line
                System.out.format("error parsing: %s\n", line);
                System.out.format("exception: %s\n", e.toString());
            }
        }

        return new ParseResult(marker, isValid());
    }

    /**
     * This function checks data fields to be non-empty and set to proper values
     *
     * @return true if all required fields are valid and set or false otherwise
     */
    boolean isValid() {
        return (contract != null && planPrice != null && total != null);
    }

    /**
     * Removes all data contained in sessions list resulting in empty person sessions
     */
    void clear() {
        number = null;
        contract = null;
        planName = null;

        planPrice = Float.NaN;
        total = Float.NaN;

        if (sessions != null) {
            sessions.forEach(session -> session.close());
        }

        sessions.clear();
        sessions = null;
    }
}