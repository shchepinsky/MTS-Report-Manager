package viewer;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import loader.Session;
import loader.SessionDirection;
import loader.SessionType;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * SessionModel used to load and access individual session data in PersonModel's sessions
 */
public class SessionModel {
    private final Session session;

    private final SimpleObjectProperty<LocalDate> date;
    private final SimpleObjectProperty<LocalTime> time;
    private final SimpleObjectProperty<LocalTime> duration;
    private final SimpleObjectProperty<SessionDirection> direction;
    private final SimpleObjectProperty<SessionType> type;

    private final SimpleStringProperty remoteNumber;
    private final SimpleStringProperty remoteVendor;

    SessionModel(Session session) {
        this.session = session;

        date = new SimpleObjectProperty<>(session.getDate());
        time = new SimpleObjectProperty<>(session.getTime());
        duration = new SimpleObjectProperty<>(session.getDuration());
        direction = new SimpleObjectProperty<>(session.getDirection());
        type = new SimpleObjectProperty<>(session.getType());

        remoteNumber = new SimpleStringProperty(session.getRemoteNumber());
        remoteVendor = new SimpleStringProperty(session.getRemoteVendor());
    }

    public LocalDate getDate() {
        return date.getValue();
    }

    public LocalTime getTime() {
        return time.getValue();
    }

    public LocalTime getDuration() {
        return time.getValue();
    }

    public SessionDirection getDirection() {
        return direction.getValue();
    }

    public SessionType getType() {
        return type.getValue();
    }

    public String getRemoteNumber() {
        return remoteNumber.getValue();
    }

    public String getRemoteVendor() {
        return remoteVendor.getValue();
    }
}
