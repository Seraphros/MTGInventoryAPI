package fr.cszw.mtginventoryapi.Beans.OrderingCard;

import fr.cszw.mtginventoryapi.Beans.Card;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class CardByRarity {

    public int rarity;
    public List<Card> cards;
    public List<CardByExtensionNumber> cardsByExtensionNumber;
}
