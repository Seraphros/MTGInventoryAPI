package fr.cszw.mtginventoryapi.Beans;

import lombok.*;

import javax.persistence.*;

@Table(name = "place")
@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Setter

public class Place {
    private int id;
    private String name;
    private String userID;

    public Place(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    @Column(name = "userID")
    public String getUserID() {
        return userID;
    }

}
