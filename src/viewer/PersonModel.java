package viewer;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import loader.Person;
import loader.Report;
import loader.Session;

import java.time.LocalDate;
import java.util.HashMap;

/**
 * PersonModel class is used to load and access person data in ReportModel
 *
 * @author sdv
 */
public class PersonModel {
    Person person;
    HashMap<String, String> entityNames;

    SimpleStringProperty number;
    SimpleStringProperty contract;
    SimpleStringProperty planName;
    SimpleFloatProperty planPrice;
    SimpleFloatProperty total;
    SimpleFloatProperty overdraft;
    SimpleObjectProperty<LocalDate> periodT1;
    SimpleObjectProperty<LocalDate> periodT2;

    PersonModel(Person person, Report report) {
        this.person = person;

        this.number = new SimpleStringProperty(person.getNumber());
        this.contract = new SimpleStringProperty(person.getContract());
        this.planName = new SimpleStringProperty(person.getPlanName());
        this.planPrice = new SimpleFloatProperty(person.getPlanPrice());
        this.total = new SimpleFloatProperty(person.getTotal());
        this.entityNames = report.getEntityNames();
        this.overdraft = new SimpleFloatProperty(person.getTotal() - person.getPlanPrice());
    }

    public String getNumber() { return number.getValue(); }

    public SimpleStringProperty numberProperty() { return number; }

    public String getContract() { return contract.getValue(); }

    public SimpleStringProperty contractProperty() { return contract; }

    public String getPlanName() { return planName.getValue(); }

    public SimpleStringProperty planNameProperty() { return planName; }

    public Float getPlanPrice() { return planPrice.getValue(); }
    public SimpleFloatProperty planPriceProperty() { return planPrice; }

    public Float getTotal() {
        return total.getValue();
    }

    public SimpleFloatProperty totalProperty() { return total; }

    public Float getOverdraft() { return overdraft.getValue(); }

    public SimpleFloatProperty overdraftProperty() { return overdraft; }

    public LocalDate getPeriodT1() { return periodT1.getValue(); }

    public SimpleObjectProperty<LocalDate> periodT1Property() { return periodT1; }

    public LocalDate getPeriodT2() { return periodT2.getValue(); }

    public SimpleObjectProperty<LocalDate> periodT2Property() { return periodT2; }

    public String getEntityName() { return entityNames != null ? entityNames.get(contract.getValue()) : "n/a"; }

    public ObservableList<Session> getSessions() {
        ObservableList<Session> result = FXCollections.observableArrayList();
        // pass method reference
        person.getSessions().forEach(result::add);
        return result;
    }
}

