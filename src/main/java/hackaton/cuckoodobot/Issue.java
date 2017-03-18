package hackaton.cuckoodobot;


/**
 * @author Dmitry Tarasov
 *         Date: 03/18/2017
 *         Time: 10:58
 */
public class Issue {
    private Long id;
    private Long groupId;
    private String text;
    private String assignee;
    private Repeat repeat;
    private Boolean isDone;

    public Issue(Long groupId, String text) {
        this.id = System.currentTimeMillis();
        this.groupId = groupId;
        this.text = text;
        this.assignee = "(all)";
        this.repeat = null;
        this.isDone = false;
    }

    public Issue(Long groupId, String text, String assignee) {
        this.id = System.currentTimeMillis();
        this.groupId = groupId;
        this.text = text;
        this.assignee = assignee;
        this.repeat = null;
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

    public Repeat getRepeat() {
        return repeat;
    }

    public void setRepeat(Repeat repeat) {
        this.repeat = repeat;
    }

    public boolean schedulable() {
        return repeat != null;
    }

    public Boolean getDone() {
        return isDone;
    }

    public void setDone(Boolean done) {
        isDone = done;
    }
}
