package Common.Protocol;

/**
 * Created by Lior on 13/11/2014.
 */

public class MiddleboxRegister extends MiddleboxMessage {
    private String siblingId;
    private boolean flow;
    private boolean stealth;

    public MiddleboxRegister(String _middleboxId, String _middleboxName) {
        super(_middleboxId, _middleboxName);
    }

    public String getSiblingId() {
        return siblingId;
    }

    public boolean isFlow() {
        return flow;
    }

    public boolean isStealth() {
        return stealth;
    }

    public void setSiblingId(String siblingId) {
        this.siblingId = siblingId;
    }

    public void setFlow(boolean flow) {
        this.flow = flow;
    }

    public void setStealth(boolean stealth) {
        this.stealth = stealth;
    }
}
