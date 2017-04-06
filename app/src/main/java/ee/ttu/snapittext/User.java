package ee.ttu.snapittext;

public class User {

    public String name;
    public String dateCreated;
    public String email;

    public User(String name, String dateCreated, String email) {
        this.name = name;
        this.dateCreated = dateCreated;
        this.email = email;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
