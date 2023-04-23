package fr.cszw.mtginventoryapi.Beans.OrderingCard;

import fr.cszw.mtginventoryapi.Beans.Card;
import fr.cszw.mtginventoryapi.Beans.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class CardBySet {
    public Set set;
    public List<Card> cards;
    public List<CardByRarity> cardsByRarity;
}
