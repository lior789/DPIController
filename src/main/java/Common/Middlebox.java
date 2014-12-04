package Common;

/**
 * this is a data class used to store and find the middlebox within the repository
 */
public class Middlebox {
    public String id;
    public String name;

    public Middlebox(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Middlebox(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Middlebox that = (Middlebox) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
