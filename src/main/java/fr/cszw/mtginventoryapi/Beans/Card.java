package fr.cszw.mtginventoryapi.Beans;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.*;


@Entity
@Table(name = "card")
@Getter
@Setter
@ToString
public class Card {

    private int id;
    private String name;
    private String lang;
    private String englishName;
    private String edition;
    private String editionNumber;
    private String scryfallID;
    private int occurences;
    private String illustration;
    private String CMLink;
    private String type;
    private Place place;
    private boolean foil;
    private String owner;
    private Double priceEur;
    private Double priceUSD;

    @Transient
    private Set set;


    public Card(Card cardToCopy) {
        this.name = cardToCopy.getName();
        this.lang = cardToCopy.getLang();
        this.englishName = cardToCopy.getEnglishName();
        this.edition = cardToCopy.getEdition();
        this.editionNumber = cardToCopy.getEditionNumber();
        this.scryfallID = cardToCopy.getScryfallID();
        this.occurences = cardToCopy.getOccurences();
        this.place = cardToCopy.getPlace();
        this.foil = cardToCopy.isFoil();
        this.owner = cardToCopy.getOwner();
        this.set = cardToCopy.getSet();
        this.priceEur = cardToCopy.getPriceEur();
        this.priceUSD = cardToCopy.getPriceUSD();
        this.illustration = cardToCopy.getIllustration();
        this.CMLink = cardToCopy.getCMLink();
        this.type = cardToCopy.getType();
    }

    public Card() {
        this.lang = "";
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

    @Column(name = "occurences", nullable = false)
    public int getOccurences() {
        return occurences;
    }

    @ManyToOne()
    @JoinColumn(name = "place_id")
    public Place getPlace() {
        return place;
    }

    @Column(name = "priceEur")
    public Double getPriceEur() {
        return priceEur;
    }

    @Column(name = "priceUSD")
    public Double getPriceUSD() {
        return priceUSD;
    }



    @Transient
    public Set getSet() {
        return this.set;
    }

}
