package fr.cszw.mtginventoryapi.Services;

import fr.cszw.mtginventoryapi.Beans.Card;
import fr.cszw.mtginventoryapi.Beans.CardPrice;
import fr.cszw.mtginventoryapi.Beans.Place;
import fr.cszw.mtginventoryapi.Repositories.CardRepository;
import fr.cszw.mtginventoryapi.Repositories.PlaceRepository;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@Scope("singleton")
public class CollectionService {

    private final CardRepository cardRepository;
    private final PlaceRepository placeRepository;
    private final CardService cardService;
    private final PlaceService placeService;

    public CollectionService(CardRepository cardRepository, PlaceRepository placeRepository, CardService cardService, PlaceService placeService) {
        this.cardRepository = cardRepository;
        this.placeRepository = placeRepository;
        this.cardService = cardService;
        this.placeService = placeService;
    }

    public List<Card> getAllCardOfUser(String user) {
        return this.cardRepository.findByOwner(user);
    }

    public List<Card> getCardsOfPlace(String user, int placeId) {
        Place place = this.placeRepository.findById(placeId).orElse(null);

        if (place != null && place.getUserID().equalsIgnoreCase(user)) {
            return this.cardRepository.findByPlace(place);
        } else {
            return new ArrayList<>();
        }
    }

    public void updateAllCards() {
        this.cardRepository.findAll().forEach(card -> this.cardService.cardList.stream().filter(analysedCard -> analysedCard.getScryfallID().equalsIgnoreCase(card.getScryfallID())).findFirst().ifPresent(baseCard -> this.cardRepository.save(card)));

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

                Card existing = cardRepository.getCardByOwnerAndScryfallIDAndFoilIs(user, found.getScryfallID(), card.isFoil());
                if (existing == null) {
                    cardToInsert.add(found);
                } else if (existing.isFoil() != found.isFoil()) {
                    cardToInsert.add(found);
                } else if (existing.getPlace() != found.getPlace()) {
                    cardToInsert.add(found);
                } else if (!existing.getLang().equals(found.getLang())) {
                    cardToInsert.add(found);
                } else {
                    found.setId(existing.getId());
                    found.setOccurences(found.getOccurences() + existing.getOccurences());
                    cardToUpdate.add(found);
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
