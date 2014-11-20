package Common.Protocol;

/**
 * this class represent an abstract message from the Middlebox to the dpi-controller
 * it should be inherit from by a specific message
 * this class serialized to/from json message
 * Created by Lior on 13/11/2014.
 */
public abstract class MiddleboxMessage {
    private String middleboxId;
    private String middleboxName;
    private String className = this.getClassName();


    public MiddleboxMessage(String middleboxId, String middleboxName) {
        this.middleboxId = middleboxId;
        this.middleboxName = middleboxName;
    }

    public String getMiddleboxId() {
        return middleboxId;
    }

    public String getClassName(){
        return this.getClass().getName();
    }

    public String getMiddleboxName() {
        return middleboxName;
    }

}
