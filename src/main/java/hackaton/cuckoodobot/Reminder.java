package hackaton.cuckoodobot;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Dmitry Tarasov
 *         Date: 03/18/2017
 *         Time: 11:07
 */
public class Reminder {
    private Calendar created;
    private Calendar target;

    public Reminder(Calendar target) {
        this.target = target;
        this.created = new GregorianCalendar();
    }

    public Reminder(Calendar created, Calendar target) {
        this.created = created;
        this.target = target;
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    public Calendar getTarget() {
        return target;
    }

    public void setTarget(Calendar target) {
        this.target = target;
    }
}
