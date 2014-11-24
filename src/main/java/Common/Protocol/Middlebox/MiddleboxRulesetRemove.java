package Common.Protocol.Middlebox;

import java.util.List;

/**
 * Created by Lior on 13/11/2014.
 */
public class MiddleboxRulesetRemove extends MiddleboxMessage {
    public List<String> rules;
    public MiddleboxRulesetRemove(String middleboxId, String middleboxName) {
        super(middleboxId);
    }
}
