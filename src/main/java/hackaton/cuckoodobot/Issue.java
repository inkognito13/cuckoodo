package hackaton.cuckoodobot;


/**
 * @author Dmitry Tarasov, Anastasia Yarunina(@crazzysun)
 *         Date: 03/18/2017
 *         Time: 10:58
 */
public class Issue {
    private Long id;
    private Long groupId;
    private String text;
    private String assignee;
    private Reminder reminder;
    private Boolean isDone;

    public Issue(Long groupId, String text) {
        this.groupId = groupId;
        this.text = text;
        this.assignee = "all";
        this.reminder = null;
        this.isDone = false;
    }

    public Issue(Long groupId, String text, String assignee) {
        this.groupId = groupId;
        this.text = text;
        this.assignee = assignee;
        this.reminder = null;
        this.isDone = false;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
    }

    public boolean schedulable() {
        return reminder != null;
    }

    public Boolean getDone() {
        return isDone;
    }

    public void setDone(Boolean done) {
        isDone = done;
    }

    @Override
    public String toString() {
        return this.getText() + " @" + this.getAssignee();
    }
}
