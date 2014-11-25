package Controller;

/**
 * this is a data class used to store and find the middlebox within the repository
 */
public class Middlebox {
    String _id;
    String _name;

    public Middlebox(String id, String name) {
        this._id = id;
        this._name = name;
    }

    public Middlebox(String id) {
        _id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Middlebox that = (Middlebox) o;

        if (!_id.equals(that._id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return _id.hashCode();
    }
}
