package hakaton.cuckoobot;


/**
 * @author Dmitry Tarasov
 *         Date: 03/18/2017
 *         Time: 10:58
 */
public class Issue {
    private String id;
    private String text;
    private String owner;
    private Repeat repeat;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Repeat getRepeat() {
        return repeat;
    }

    public void setRepeat(Repeat repeat) {
        this.repeat = repeat;
    }
    
    public boolean schedulable(){
        return repeat!=null;
    }
}
