package fr.cszw.mtginventoryapi.Services;

import fr.cszw.mtginventoryapi.Beans.Card;
import fr.cszw.mtginventoryapi.Beans.CardPrice;
import fr.cszw.mtginventoryapi.Beans.Paginator;
import fr.cszw.mtginventoryapi.Beans.Place;
import fr.cszw.mtginventoryapi.Repositories.CardRepository;
import fr.cszw.mtginventoryapi.Repositories.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope("singleton")
@RequiredArgsConstructor
@Slf4j
public class CollectionService {

    private final CardRepository cardRepository;
    private final PlaceRepository placeRepository;
    private final CardService cardService;
    private final PlaceService placeService;
    private final SetJSONService setJSONService;



    private final int PAGE_SIZE = 100;

    public List<Card> getAllCardOfUser(String user) {
        return this.cardRepository.findByOwner(user);
    }

    public List<Card> getAllCardOfUserPaginated(String user, int page, String search, String order) {
        List<Card> cards = this.cardRepository.findByOwner(user);
        cards = order(cards, order);
        cards = filter(cards, search);
        int total = cards.size();
        List<Card> sublist;
        if (page * PAGE_SIZE < total)
            sublist = cards.subList((page - 1) * PAGE_SIZE, page * PAGE_SIZE);
        else
            sublist = cards.subList((page - 1) * PAGE_SIZE, total);


        return sublist;
    }

    public List<Card> filter(List<Card> cards, String search) {
        if(search.equals("")) return cards;
        List<Card> filtered = new ArrayList<>(cards).stream().filter(card -> card.getName().toLowerCase().contains(search.toLowerCase()) ||
                (card.getEnglishName() != null && card.getEnglishName().toLowerCase().contains(search.toLowerCase())) ||
                (card.getEdition() != null && card.getEdition().toLowerCase().contains(search.toLowerCase())) ||
                (card.getType() != null && card.getType().toLowerCase().contains(search.toLowerCase())) ||
                (card.getPlace() != null && card.getPlace().getName().toLowerCase().contains(search.toLowerCase())) ||
                (card.getLang() != null && card.getLang().toLowerCase().contains(search.toLowerCase())) ||
                (card.getEditionNumber() != null && card.getEditionNumber().toLowerCase().contains(search.toLowerCase()))).collect(Collectors.toList());

        return filtered;
    }

    public List<Card> order(List<Card> cards, String order) {
        if (order.split("/").length != 2) return new ArrayList<>();

        String field = order.split("/")[0];
        boolean direction = order.split("/")[1].equalsIgnoreCase("ASC");

        if (field.equalsIgnoreCase("name")) {
            if(direction) {
                cards.sort(Comparator.comparing(Card::getName));
            } else {
                cards.sort(Comparator.comparing(Card::getName).reversed());
            }
        }
        if (field.equalsIgnoreCase("number")) {
            if(direction) {
                cards.sort(Comparator.comparing(Card::getOccurences));
            } else {
                cards.sort(Comparator.comparing(Card::getOccurences).reversed());
            }
        }
        if (field.equalsIgnoreCase("lang")) {
            if(direction) {
                cards.sort(Comparator.comparing(Card::getLang));
            } else {
                cards.sort(Comparator.comparing(Card::getLang).reversed());
            }
        }
        if (field.equalsIgnoreCase("collection")) {
            if(direction) {
                cards.sort(Comparator.comparing(Card::getEdition));
            } else {
                cards.sort(Comparator.comparing(Card::getEdition).reversed());
            }
        }
        if (field.equalsIgnoreCase("collectionNumber")) {
            if(direction) {
                cards.sort(Comparator.comparing(Card::getEditionNumber));
            } else {
                cards.sort(Comparator.comparing(Card::getEditionNumber).reversed());
            }
        }
        if (field.equalsIgnoreCase("collection")) {
            if(direction) {
                cards.sort(Comparator.comparing(Card::getEdition));
            } else {
                cards.sort(Comparator.comparing(Card::getEdition).reversed());
            }
        }
        if (field.equalsIgnoreCase("type")) {
            if(direction) {
                cards.sort(Comparator.comparing(Card::getType));
            } else {
                cards.sort(Comparator.comparing(Card::getType).reversed());
            }
        }
        if (field.equalsIgnoreCase("foiled")) {
            if(direction) {
                cards.sort(Comparator.comparing(Card::isFoil));
            } else {
                cards.sort(Comparator.comparing(Card::isFoil).reversed());
            }
        }
        if (field.equalsIgnoreCase("eur")) {
            if(direction) {
                cards.sort(Comparator.comparing(Card::getPriceEur, Comparator.nullsFirst(Comparator.naturalOrder())));
            } else {
                cards.sort(Comparator.comparing(Card::getPriceEur, Comparator.nullsFirst(Comparator.naturalOrder())).reversed());
            }
        }
        if (field.equalsIgnoreCase("usd")) {
            if(direction) {
                cards.sort(Comparator.comparing(Card::getPriceUSD, Comparator.nullsFirst(Comparator.naturalOrder())));
            } else {
                cards.sort(Comparator.comparing(Card::getPriceUSD, Comparator.nullsFirst(Comparator.naturalOrder())).reversed());
            }
        }
        if (field.equalsIgnoreCase("place")) {
            if(direction) {
                cards.sort(Comparator.comparing(obj -> obj.getPlace().getName()));
            } else {
                cards.sort(Comparator.comparing(obj -> obj.getPlace().getName(), Comparator.reverseOrder()));
            }
        }
        return cards;
    }

    public List<Card> getCardsOfPlace(String user, int placeId) {
        Place place = this.placeRepository.findById(placeId).orElse(null);

        if (place != null && place.getUserID().equalsIgnoreCase(user)) {
            return this.cardRepository.findByPlace(place);
        } else {
            return new ArrayList<>();
        }
    }

    public List<Card> getOrderedCardsOfPlaces(String user, String placesId) {
        if (placesId == null || placesId.equals("")) return new ArrayList<>();

        String[] placesIdParsed = placesId.split(";");
        List<Card> cards = new ArrayList<>();
        Arrays.stream(placesIdParsed).forEach(placeId -> {
            cards.addAll(this.cardRepository.findByPlace(this.placeRepository.findById(Integer.parseInt(placeId)).orElse(null)));
            cards.forEach(card -> {
                card.setSet(this.setJSONService.getSetFromList(card.getEdition()));
                if (card.getRarity().equals("mythic")) card.setRarityValue(3);
                if (card.getRarity().equals("rare")) card.setRarityValue(2);
                if (card.getRarity().equals("uncommon")) card.setRarityValue(1);
                if (card.getRarity().equals("common")) card.setRarityValue(0);
            });
        });

        List<Card> cardOrdered = new ArrayList<>();

        List<Card> creatureCards = new ArrayList<>();
        List<Card> instantCards = new ArrayList<>();
        List<Card> sorceryCards = new ArrayList<>();
        List<Card> enchantmentCards = new ArrayList<>();
        List<Card> planeswalkerCards = new ArrayList<>();
        List<Card> artifactCards = new ArrayList<>();
        List<Card> siegeCards = new ArrayList<>();
        List<Card> landCards = new ArrayList<>();

        cards.forEach(card -> {
            String firstType = card.getType().toLowerCase().split("//")[0];
            if (firstType.contains("creature")) {
                creatureCards.add(card);
            } else if (firstType.contains("instant")) {
                instantCards.add(card);
            } else if (firstType.contains("sorcery")) {
                sorceryCards.add(card);
            } else if (firstType.contains("battle")) {
                siegeCards.add(card);
            } else if (firstType.contains("planeswalker")) {
                planeswalkerCards.add(card);
            } else if (firstType.contains("enchant")) {
                enchantmentCards.add(card);
            } else if (firstType.contains("artifact")) {
                artifactCards.add(card);
            } else if (firstType.contains("land")) {
                landCards.add(card);
            } else {
                log.error("Could not sort : " + card.getName());
            }
        });





        OrderingService.classicalCreatureOrdering(creatureCards);
        OrderingService.classicalOtherOrdering(instantCards);
        OrderingService.classicalOtherOrdering(sorceryCards);
        OrderingService.classicalOtherOrdering(enchantmentCards);
        OrderingService.classicalOtherOrdering(planeswalkerCards);
        OrderingService.classicalOtherOrdering(artifactCards);
        OrderingService.classicalOtherOrdering(siegeCards);
        OrderingService.classicalOtherOrdering(landCards);

        cardOrdered.addAll(creatureCards);
        cardOrdered.addAll(instantCards);
        cardOrdered.addAll(sorceryCards);
        cardOrdered.addAll(enchantmentCards);
        cardOrdered.addAll(artifactCards);
        cardOrdered.addAll(planeswalkerCards);
        cardOrdered.addAll(siegeCards);
        cardOrdered.addAll(landCards);


        return cardOrdered;
    }

    public Paginator getCollectionPaginator(String user, String search) {
        List<Card> cards = this.cardRepository.findByOwner(user);
        cards.sort(Comparator.comparing(Card::getName));
        cards = filter(cards, search);
        int total = cards.size();
        return new Paginator(total);
    }

    public void updateAllCards() {
        List<Card> cardToUpdate = new ArrayList<>();

        this.cardRepository.findAll().forEach(card -> {
            try {

            Card listed = this.cardService.cardList.stream().filter(cardListed -> cardListed.getScryfallID().equals(card.getScryfallID())).findFirst().orElse(null);
            if (listed != null) {
                if ((card.getColor() == null || !card.getColor().equals(listed.getColor())) ||
                        (card.getMana() == null || !card.getMana().equals(listed.getMana())) ||
                        (card.getRarity() == null || !card.getRarity().equals(listed.getRarity())) ||
                        (card.getIllustration() == null || !card.getIllustration().equals(listed.getIllustration())) ||
                        (card.getCmc() == null || !card.getCmc().equals(listed.getCmc()))) {
                    card.setColor(listed.getColor());
                    card.setMana(listed.getMana());
                    card.setCmc(listed.getCmc());
                    card.setRarity(listed.getRarity());
                    card.setIllustration(listed.getIllustration());
                    cardToUpdate.add(card);
                }
            } }
            catch (Exception e) {
                log.error("Error found updating card with id :" + card.getId() + " and name : " + card.getName() + ", error : " + e);
            }

        });

        this.cardRepository.saveAll(cardToUpdate);

    }

    public List<Card> addCardsToCollection(List<Card> cards, String user) {
        List<Card> cardToInsert = new ArrayList<>();
        List<Card> cardToUpdate = new ArrayList<>();

        cards.forEach(card -> {
            if (card.getId() != 0) {
                if (card.getOwner().equals(user)) {
                    cardToUpdate.add(card);
                }
            } else {
                Card found = new Card(cardService.findCardByScryfallId(card.getScryfallID()));

                found.setOwner(user);
                found.setFoil(card.isFoil());
                found.setPlace(placeService.getPlaceById(card.getPlace().getId()));
                if (found.getPlace() == null) return;
                if (!found.getPlace().getUserID().equals(user)) throw new RuntimeException("Place ownership is wrong");
                found.setOccurences(card.getOccurences());

                Card existing = cardRepository.getCardByOwnerAndScryfallIDAndFoilIsAndPlace(user, found.getScryfallID(), card.isFoil(), card.getPlace());


                if (existing == null || existing.isFoil() != found.isFoil() || existing.getPlace() != found.getPlace() || !existing.getLang().equals(found.getLang())) {
                    Card existingInInsert = cardToInsert.stream().filter(
                            cardToCheck -> cardToCheck.getEdition().equals(found.getEdition()) &&
                                    cardToCheck.getEditionNumber().equals(found.getEditionNumber()) &&
                                    cardToCheck.isFoil() == found.isFoil() &&
                                    cardToCheck.getPlace() == found.getPlace() &&
                                    cardToCheck.getLang().equals(found.getLang())).findFirst().orElse(null);
                    if (existingInInsert != null) {
                        existingInInsert.setOccurences(existingInInsert.getOccurences() + found.getOccurences());
                    } else {
                        cardToInsert.add(found);
                    }
                } else {
                    Card existingInUpdate = cardToUpdate.stream().filter(
                            cardToCheck -> cardToCheck.getEdition().equals(found.getEdition()) &&
                            cardToCheck.getEditionNumber().equals(found.getEditionNumber()) &&
                            cardToCheck.isFoil() == found.isFoil() &&
                            cardToCheck.getPlace() == found.getPlace() &&
                            cardToCheck.getLang().equals(found.getLang())).findFirst().orElse(null);

                    if (existingInUpdate != null) {
                        existingInUpdate.setOccurences(existingInUpdate.getOccurences() + found.getOccurences());
                    } else {
                        found.setId(existing.getId());
                        found.setOccurences(found.getOccurences() + existing.getOccurences());
                        cardToUpdate.add(found);
                    }
                }
            }
        });

        cardRepository.saveAll(cardToUpdate);
        cardRepository.saveAll(cardToInsert);
        cardToUpdate.addAll(cardToInsert);
        return cardToUpdate;
    }

    public void removeCardsOfCollection(List<Card> cards, String user) {
        cards.forEach(card -> {
            if (card.getOwner().equals(user)) {
                this.cardRepository.delete(card);
            }
        });
    }

    public List<Card> evaluateCardsPrice(String user) {


        List<Card> cards = this.cardRepository.findByOwner(user);
        List<CardPrice> cardPrices = cardService.priceList;

        cards.forEach(card -> {

            CardPrice found;
            if (card.getLang().equalsIgnoreCase("en")) {
                found = cardPrices.stream().filter(cardPrice -> (cardPrice.getName().equalsIgnoreCase(card.getName())
                                && cardPrice.getEdition().equalsIgnoreCase(card.getEdition())
                                && cardPrice.getEditionNumber().equalsIgnoreCase(card.getEditionNumber())))
                        .findFirst()
                        .orElse(null);
            } else {
                found = cardPrices.stream().filter(cardPrice -> (cardPrice.getName().equalsIgnoreCase(card.getEnglishName())
                                && cardPrice.getEdition().equalsIgnoreCase(card.getEdition())
                                && cardPrice.getEditionNumber().equalsIgnoreCase(card.getEditionNumber())))
                        .findFirst()
                        .orElse(null);
            }

            if (found != null) {
                if (!card.isFoil()) {
                    card.setPriceEur(found.getPriceEur());
                    card.setPriceUSD(found.getPriceUSD());
                } else {
                    card.setPriceEur(found.getPriceFoilEur());
                    card.setPriceUSD(found.getPriceFoilUSD());
                }
            }

        });

        this.cardRepository.saveAll(cards);
        return cards;
    }

    public Card evaluateCardPrice(int id, String user) {
        Card card = this.cardRepository.findById(id).orElse(null);
        if (card != null && card.getOwner().equalsIgnoreCase(user)) {
            final String EUR_PRICE = "eur";
            final String EUR_PRICE_FOILED = "eur_foil";
            final String USD_PRICE = "usd";
            final String USD_PRICE_FOILED = "usd_foil";

            try {
                URL url = new URL("https://api.scryfall.com/cards/" + card.getEdition() + "/" + card.getEditionNumber());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                JsonFactory jsonfactory = new JsonFactory(); //init factory
                JsonParser jsonParser = jsonfactory.createJsonParser(con.getInputStream());
                JsonToken jsonToken = jsonParser.nextToken();

                while (jsonToken != null) { //Iterate all elements of array
                    String fieldname = jsonParser.getCurrentName();

                    if (EUR_PRICE.equals(fieldname) && !card.isFoil()) {
                        jsonToken = jsonParser.nextToken(); //read next token
                        String price = jsonParser.getText();
                        if (!price.equals("null")) card.setPriceEur(Double.parseDouble(price));
                    } else if (EUR_PRICE_FOILED.equals(fieldname) && card.isFoil()) {
                        jsonToken = jsonParser.nextToken(); //read next token
                        String price = jsonParser.getText();
                        if (!price.equals("null")) card.setPriceEur(Double.parseDouble(price));
                    } else if (USD_PRICE.equals(fieldname) && !card.isFoil()) {
                        jsonToken = jsonParser.nextToken(); //read next token
                        String price = jsonParser.getText();
                        if (!price.equals("null")) card.setPriceUSD(Double.parseDouble(price));
                    } else if (USD_PRICE_FOILED.equals(fieldname) && card.isFoil()) {
                        jsonToken = jsonParser.nextToken(); //read next token
                        String price = jsonParser.getText();
                        if (!price.equals("null")) card.setPriceUSD(Double.parseDouble(price));
                    }

                    jsonToken = jsonParser.nextToken();
                }

                con.disconnect();
                Thread.sleep(50);
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.cardRepository.save(card);
        }
        return card;
    }

    public Long getAllNumberCardsOfUser(String user) {
        return this.cardRepository.getSum(user);
    }
}
