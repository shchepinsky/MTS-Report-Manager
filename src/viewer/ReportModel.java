package viewer;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import loader.Report;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;


/**
 * ReportModel class is used to load and access report data from Controller
 */
public class ReportModel {
    SimpleObjectProperty<Report> report;

    ReportModel() {
        this.report = new SimpleObjectProperty<>();
    }

    public String getCompanyName() {
        return getReport() != null ? getReport().getEntityNames().get(getAccountName()) : null;
    }

    public String getAccountName() {
        return getReport() != null ? getReport().getAccountName() : null;
    }

    public LocalDate getPeriodT1() {
        return getReport() != null ? getReport().getPeriodT1() : null;
    }

    public LocalDate getPeriodT2() {
        return getReport() != null ? getReport().getPeriodT2() : null;
    }

    public String getPeriod() {
        if ((getPeriodT1() == null) || (getPeriodT2()) == null) return null;

        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);
        return getPeriodT1().format(formatter) + " - " + getPeriodT2().format(formatter);
    }

    public SimpleObjectProperty<Report> reportProperty() { return report; }

    public Report getReport() { return report.getValue(); }

    public String getFilename() { return getReport() == null ? null : getReport().getFilename(); }

    /**
     * This function manages underlying report loader object and uses it to loads data
     *
     * @param filename filename to load data from
     */
    public void loadFromFile(String filename) {
        Report reportLoader = new Report();
        reportLoader.loadFromFile(filename);

        report.setValue(reportLoader);
    }

    public void loadEntityNames(String filename) {
        if (report.getValue() == null)
            throw new AssertionError("Can not load entity names into null report");

        // i don't know how to fire listener code without assigning property
        // so this is probably a dirty workaround
        Report temp = report.getValue();
        temp.loadEntityNames(filename);
        report.setValue(temp);
    }

    /**
     * closes underlying data model
     */
    public void close() {
        if (report.getValue() != null)
            report.getValue().close();

        report.setValue(null);
    }

    public ObservableList<String> getRawData() {
        if (report.getValue() == null)
            return null;

        ObservableList<String> result = FXCollections.observableArrayList();
        // add to result by passing method reference
        report.getValue().getRawReportData().forEach(result::add);
        return result;
    }
}
