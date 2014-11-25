package Controller;

/**
 * Created by Lior on 24/11/2014.
 */
class ServiceInstance {
    public String id;
    public String name;

    public ServiceInstance(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public ServiceInstance(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceInstance that = (ServiceInstance) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
