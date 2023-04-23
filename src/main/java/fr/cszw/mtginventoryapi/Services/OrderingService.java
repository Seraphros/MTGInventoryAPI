package fr.cszw.mtginventoryapi.Services;

import fr.cszw.mtginventoryapi.Beans.Card;
import fr.cszw.mtginventoryapi.Beans.OrderingCard.CardByCMC;
import fr.cszw.mtginventoryapi.Beans.OrderingCard.CardByRarity;
import fr.cszw.mtginventoryapi.Beans.OrderingCard.CardBySet;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class OrderingService {

    public static void classicalCreatureOrdering(List<Card> cardList) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<CardByCMC> cardsByCMC = new ArrayList<>();

        cardList.sort(Comparator.comparingInt(Card::getCmc));

        cardList.forEach(card -> {
            CardByCMC cardByCMC = cardsByCMC.stream().filter(cmc -> cmc.getCmc() == card.getCmc()).findFirst().orElse(null);
            if (cardByCMC == null) {
                cardByCMC = new CardByCMC();
                cardByCMC.setCmc(card.getCmc());
                cardByCMC.setCards(new ArrayList<>());
                cardByCMC.setCardsBySet(new ArrayList<>());
                cardsByCMC.add(cardByCMC);
            }
            cardByCMC.getCards().add(card);
        });
        cardsByCMC.sort(Comparator.comparingInt(CardByCMC::getCmc));

        cardsByCMC.forEach(cardByCMC -> {
            cardByCMC.getCards().forEach(card -> {
                CardBySet cardBySet = cardByCMC.getCardsBySet().stream().filter(set -> set.getSet().equals(card.getSet())).findFirst().orElse(null);

                if (cardBySet == null) {
                    cardBySet = new CardBySet();
                    cardBySet.setSet(card.getSet());
                    cardBySet.setCards(new ArrayList<>());
                    cardBySet.setCardsByRarity(new ArrayList<>());
                    cardByCMC.getCardsBySet().add(cardBySet);
                }
                cardBySet.getCards().add(card);

            });

            cardByCMC.getCardsBySet().sort((c1, c2) -> {
                try {
                    return sdf.parse(c1.getSet().getReleasedDate()).compareTo(sdf.parse(c2.getSet().getReleasedDate()));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        cardsByCMC.forEach(cardByCMC -> {
            cardByCMC.getCardsBySet().forEach(cardBySet -> {
                cardBySet.getCards().forEach(card -> {
                    CardByRarity cardByRarity = cardBySet.getCardsByRarity().stream().filter(rarity -> rarity.getRarity() == card.getRarityValue()).findFirst().orElse(null);
                    if (cardByRarity == null) {
                        cardByRarity = new CardByRarity();
                        cardByRarity.setRarity(card.getRarityValue());
                        cardByRarity.setCards(new ArrayList<>());
                        cardByRarity.setCardsByExtensionNumber(new ArrayList<>());
                        cardBySet.getCardsByRarity().add(cardByRarity);
                    }
                    cardByRarity.getCards().add(card);

                });
                cardBySet.getCardsByRarity().forEach(cardByRarity -> cardByRarity.getCards().sort((c1,c2) -> {
                    try {
                        int c1int = Integer.parseInt(c1.getEditionNumber());
                        int c2int = Integer.parseInt(c2.getEditionNumber());
                        return Integer.compare(c1int,c2int);
                    } catch (Exception e) {
                        return 0;
                    }
                }));
                cardBySet.getCardsByRarity().sort(Comparator.comparingInt(CardByRarity::getRarity).reversed());
            });
        });


        cardList.clear();
        cardsByCMC.forEach(cardByCMC ->
                cardByCMC.getCardsBySet().forEach(cardBySet ->
                        cardBySet.getCardsByRarity().forEach(cardByRarity ->
                                cardList.addAll(cardByRarity.getCards()))));

        Iterator<Card> i = cardList.iterator();
        while (i.hasNext()) {
            Card card = i.next();
            Card cardFoundLang = cardList.stream().filter(card1 -> card1.getSet().equals(card.getSet()) && card1.getEditionNumber().equals(card.getEditionNumber()) && !card1.getLang().equals(card.getLang())).findFirst().orElse(null);
            if (cardFoundLang != null && !card.getLang().equals("fr")) {
                cardFoundLang.setOccurences(cardFoundLang.getOccurences() + card.getOccurences());
                i.remove();
            } else if (card.isFoil()) {
                AtomicBoolean found = new AtomicBoolean(false);
                cardList.stream().filter(card1 -> card1.getSet().equals(card.getSet()) && card1.getEditionNumber().equals(card.getEditionNumber()) && card1.isFoil() != card.isFoil()).forEach(cardFound -> {
                    cardFound.setOccurences(cardFound.getOccurences() + card.getOccurences());
                    found.set(true);
                });
                if (found.get()) i.remove();
            }
        }
    }

    public static void classicalOtherOrdering(List<Card> cardList) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<CardBySet> cardsBySet = new ArrayList<>();


        cardList.forEach(card -> {
            CardBySet cardBySet = cardsBySet.stream().filter(set -> set.getSet().equals(card.getSet())).findFirst().orElse(null);

            if (cardBySet == null) {
                cardBySet = new CardBySet();
                cardBySet.setSet(card.getSet());
                cardBySet.setCards(new ArrayList<>());
                cardBySet.setCardsByRarity(new ArrayList<>());
                cardsBySet.add(cardBySet);
            }
            cardBySet.getCards().add(card);

        });

        cardsBySet.sort((c1, c2) -> {
            try {
                return sdf.parse(c1.getSet().getReleasedDate()).compareTo(sdf.parse(c2.getSet().getReleasedDate()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });

        cardsBySet.forEach(cardBySet -> {
            cardBySet.getCards().forEach(card -> {
                CardByRarity cardByRarity = cardBySet.getCardsByRarity().stream().filter(rarity -> rarity.getRarity() == card.getRarityValue()).findFirst().orElse(null);
                if (cardByRarity == null) {
                    cardByRarity = new CardByRarity();
                    cardByRarity.setRarity(card.getRarityValue());
                    cardByRarity.setCards(new ArrayList<>());
                    cardByRarity.setCardsByExtensionNumber(new ArrayList<>());
                    cardBySet.getCardsByRarity().add(cardByRarity);
                }
                cardByRarity.getCards().add(card);

            });
            cardBySet.getCardsByRarity().forEach(cardByRarity -> cardByRarity.getCards().sort((c1,c2) -> {
                try {
                    int c1int = Integer.parseInt(c1.getEditionNumber());
                    int c2int = Integer.parseInt(c2.getEditionNumber());
                    return Integer.compare(c1int,c2int);
                } catch (Exception e) {
                    return 0;
                }
            }));
            cardBySet.getCardsByRarity().sort(Comparator.comparingInt(CardByRarity::getRarity).reversed());
        });

        cardList.clear();
        cardsBySet.forEach(cardBySet ->
                cardBySet.getCardsByRarity().forEach(cardByRarity ->
                        cardList.addAll(cardByRarity.getCards())));

        Iterator<Card> i = cardList.iterator();
        while (i.hasNext()) {
            Card card = i.next();
            Card cardFoundLang = cardList.stream().filter(card1 -> card1.getSet().equals(card.getSet()) && card1.getEditionNumber().equals(card.getEditionNumber()) && !card1.getLang().equals(card.getLang())).findFirst().orElse(null);
            if (cardFoundLang != null && !card.getLang().equals("fr")) {
                cardFoundLang.setOccurences(cardFoundLang.getOccurences() + card.getOccurences());
                i.remove();
            } else if (card.isFoil()) {
                AtomicBoolean found = new AtomicBoolean(false);
                cardList.stream().filter(card1 -> card1.getSet().equals(card.getSet()) && card1.getEditionNumber().equals(card.getEditionNumber()) && card1.isFoil() != card.isFoil()).forEach(cardFound -> {
                    cardFound.setOccurences(cardFound.getOccurences() + card.getOccurences());
                    found.set(true);
                });
                if (found.get()) i.remove();
            }
        }
    }
}
