package loader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Report class contains global, companyName-wide information like name,
 * accountName number, etc along with persons list.
 *
 * @author sdv
 */
public class Report implements AutoCloseable {
    static final String PERSONS_FILE_ENCODING_NAME = "CP1251";
    static final String REPORT_FILE_ENCODING_NAME = "CP1251";

    static final String ACCOUNT_REGEX = "^особовий рахунок:\\s*(\\d*\\.\\d*)";
    static final String PERIOD_REGEX = "^період:\\s*(\\d{2}\\.\\d{2}\\.\\d{4})\\s*-\\s*(\\d{2}\\.\\d{2}\\.\\d{4})";

    static final String HEADER_END_REGEX = Person.CONTRACT_REGEX;

    // raw report data
    List<String> rawReportData = new ArrayList<>();

    // filename
    String filename = null;

    // fields
    String accountName = null;

    LocalDate periodT1 = null; // report period start date
    LocalDate periodT2 = null; // report period end date

    /**
     * Persons field contains HashMap with person.number as Key and person itself as Value
     */
    HashMap<String, Person> persons = new HashMap<>();

    /**
     * This map contains name lookup table for each phone number.
     * Table is optional and can be null. Use loadEntityNames() to load this map from file.
     */
    HashMap<String, String> entityNames = new HashMap<>();

    public List<String> getRawReportData() { return rawReportData; }

    public String getAccountName() { return accountName; }

    public String getFilename() { return filename; }

    public LocalDate getPeriodT1() { return periodT1; }

    public LocalDate getPeriodT2() { return periodT2; }

    public HashMap<String, Person> getPersons() { return persons; }

    public HashMap<String, String> getEntityNames() {return entityNames; }

    /**
     * this method dumps data to standard console and is was created for debugging purposes
     */
    public void dumpToConsole() {
        persons.forEach((k, v) -> System.out.format("%s %s %.2f\n", k, v.planName, v.planPrice));
    }

    /**
     * This method loads report data from a CSV file
     *
     * @param filename file name to load report data from
     */
    public void loadFromFile(String filename) {
        clear();// clear previous data

        try {   // load all lines into RawReportData field
            rawReportData = Files.readAllLines(Paths.get(filename), Charset.forName(REPORT_FILE_ENCODING_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.filename = filename;

        // parse loaded lines
        String line;        // current line to be parsed
        Integer marker = 0; // current position in list

        Matcher m;  // RegExp matcher helper
        // read all file lines from list
        while (marker < rawReportData.size()) {
            line = rawReportData.get(marker);
            marker++;
            try {
                // load global report data
                m = Pattern.compile(ACCOUNT_REGEX, Const.REGEXP_PATTERN_FLAGS).matcher(line);
                if (m.find()) {
                    accountName = m.group(1);
                    continue;
                }

                m = Pattern.compile(PERIOD_REGEX, Const.REGEXP_PATTERN_FLAGS).matcher(line);
                if (m.find()) {
                    periodT1 = LocalDate.parse(m.group(1), DateTimeFormatter.ofPattern(Const.DATE_PATTERN));
                    periodT2 = LocalDate.parse(m.group(2), DateTimeFormatter.ofPattern(Const.DATE_PATTERN));
                    continue;
                }

                // check for global data block end
                m = Pattern.compile(HEADER_END_REGEX, Const.REGEXP_PATTERN_FLAGS).matcher(line);
                if (m.find()) {
                    marker--;   // roll back to previous line

                    // load per-person data
                    Person person = new Person();
                    ParseResult result = person.loadFromList(rawReportData, marker);
                    if (result.success) {
                        persons.put(person.contract, person);
                        // System.out.println("loaded data for: " + person.number);
                        marker = result.marker;
                    }

                }
                // mark line for possible roll back
                // br.mark(Const.MAX_LINE_LEN);
            } catch (DateTimeException e) {
                // catch Exception and show malformed line
                System.out.format("error parsing: %s\n", line);
                System.out.format("exception: %s\n", e.toString());
            }
        }

        // after loading report, check for entity names if available
        File entitiesMapFile = new File(new File(filename).getParent(), "entities.txt");
        if (entitiesMapFile.exists()) {
            System.out.format("auto-loading entity name file: %s\n", entitiesMapFile.getAbsolutePath());
            loadEntityNames(entitiesMapFile.getAbsolutePath());
        }
    }

    /**
     * This function loads person names associated with particular numbers
     * Associations must be in form number:name
     * Empty lines are skipped as well as lines without colon
     *
     * @param filename full file path to load associations from
     * @throws IOException
     */
    public void loadEntityNames(String filename) {
        // clear existing lookup table data
        entityNames.clear();
        // using stream with new lambda syntax to make code less readable ;-)
        try {
            Files.readAllLines(Paths.get(filename), Charset.forName(PERSONS_FILE_ENCODING_NAME)).stream().forEach(s -> {
                String[] parts = s.split(":");
                if ((parts.length == 2) && (!parts[0].trim().isEmpty()) && (!parts[1].trim().isEmpty()))
                    entityNames.put(parts[0].trim(), parts[1].trim());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cleans out any existing data resulting in empty report
     */
    public void clear() {
        accountName = null;

        filename = null;

        periodT1 = null;
        periodT2 = null;

        rawReportData.clear();

        if (persons != null) {
            persons.forEach((k, v) -> v.clear());
            persons.clear();
        }
    }

    @Override
    public void close() {
        clear();
    }
}
