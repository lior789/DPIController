package Common.Protocol;

import com.google.gson.*;

import java.util.List;

/**
 * this class used to generate messages from the middlebox to the dpi-controller
 * Created by Lior on 13/11/2014.
 */
public class MiddleboxMessageFactory {
    private final String _id;
    private final String _name;


    /**
     * @param id the middlebox id we want to generate message from
     * @param name the middlebox name we want to generate message from
     */
    public MiddleboxMessageFactory(String id, String name) {
        this._id = id;
        this._name = name;
    }



    public MiddleboxRegister createRegistration() {
        return new MiddleboxRegister(_id, _name);
    }


    public MiddleboxDeregister createDeregistration() {
        return new MiddleboxDeregister(_id, _name);
    }

    public MiddleboxRulesetAdd createRulesetAdd(List<MatchRule> rules){
        MiddleboxRulesetAdd msg = new MiddleboxRulesetAdd(_id, _name);
        msg.rules  = rules;
        return msg;
    }

    public MiddleboxRulesetRemove createRulesetRemove(List<String> rulesIds){
        MiddleboxRulesetRemove msg = new MiddleboxRulesetRemove(_id, _name);
        msg.rules  = rulesIds;
        return msg;
    }

    /**
     * this static method generate a middlebox message object represent the json message string
     * @param message a json string, represent a middlebox message
     * @return sub type of MiddleboxMessage, the message object
     */
    public static MiddleboxMessage create(String message) {
        Gson gson = new Gson();
        JsonElement msgTree = new JsonParser().parse(message);
        JsonElement className = msgTree.getAsJsonObject().get("className");

        try {
            return (MiddleboxMessage) gson.fromJson(message, Class.forName(className.getAsString()));
        } catch (ClassNotFoundException e) {
           return null;
        }

    }


    /**
     * @param msg
     * @return json representation of the input msg
     */
    public static String toJson(MiddleboxMessage msg) {
        Gson gson = new Gson();
        return gson.toJson(msg);
    }
}
