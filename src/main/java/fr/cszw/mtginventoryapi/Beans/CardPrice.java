package fr.cszw.mtginventoryapi.Beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CardPrice {

    private String scryfallId;
    private String name;
    private String edition;
    private String editionNumber;
    private Double priceEur;
    private Double priceFoilEur;
    private Double priceUSD;
    private Double priceFoilUSD;

}
