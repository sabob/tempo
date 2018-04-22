package za.sabob.tempo.domain;

import javax.persistence.*;

@Entity
public class Person {

    @Id
    private long id;

    private String name;

    public long getId() {
        return id;
    }

    public void setId( long id ) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

}
