package Common.Protocol.Service;

import Common.Protocol.Middlebox.MiddleboxRegister;

/**
 * Created by Lior on 20/11/2014.
 */
public class InstanceMessageFactory {
    private final String _id;
    private final String _name;

    public InstanceMessageFactory(String id, String name) {
        this._id = id;
        this._name = name;
    }

    public InstanceRegister createRegistration() {
        InstanceRegister register = new InstanceRegister(_id);
        register.name = _name;
        return register;
    }
}
