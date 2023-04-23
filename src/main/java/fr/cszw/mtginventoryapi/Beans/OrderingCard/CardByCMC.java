package fr.cszw.mtginventoryapi.Beans.OrderingCard;

import fr.cszw.mtginventoryapi.Beans.Card;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class CardByCMC {

    private int cmc;
    private List<Card> cards;
    private List<CardBySet> cardsBySet;
}
