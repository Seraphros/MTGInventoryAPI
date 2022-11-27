package fr.cszw.mtginventoryapi.Services;

import fr.cszw.mtginventoryapi.Beans.Card;
import fr.cszw.mtginventoryapi.Beans.Set;
import fr.cszw.mtginventoryapi.Repositories.CardRepository;
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
    private final CardJSONService cardJSONService;
    private final PlaceService placeService;

    public CollectionService(CardRepository cardRepository, CardJSONService cardJSONService, PlaceService placeService) {
        this.cardRepository = cardRepository;
        this.cardJSONService = cardJSONService;
        this.placeService = placeService;
    }

    public List<Card> getAllCardOfUser(String user) {
        return this.cardRepository.findByOwner(user);
    }

    public boolean addCardsToCollection(List<Card> cards, String user) throws Exception {
        List<Card> cardToInsert = new ArrayList<>();
        List<Card> cardToUpdate = new ArrayList<>();
        cards.forEach(card -> {
            Card found = new Card(cardJSONService.findCardByScryfallId(card.getScryfallID()));
            if (found == null) return;

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
        });

        cardRepository.saveAll(cardToUpdate);
        cardRepository.saveAll(cardToInsert);

        return true;
    }

    public List<Card> evaluateCardsPrice(String user) {

        final String EUR_PRICE = "eur";
        final String EUR_PRICE_FOILED = "eur_foil";
        final String USD_PRICE = "usd";
        final String USD_PRICE_FOILED = "usd_foil";

        List<Card> cards = this.cardRepository.findByOwner(user);

        cards.forEach(card -> {
            try {
                URL url = new URL("https://api.scryfall.com/cards/" + card.getEdition() + "/" + card.getEditionNumber());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                JsonFactory jsonfactory = new JsonFactory(); //init factory
                int numberOfRecords = 0;


                JsonParser jsonParser = jsonfactory.createJsonParser(con.getInputStream());
                JsonToken jsonToken = jsonParser.nextToken();

                Set set = new Set();
                int objectDepth = 0;

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

        });

        this.cardRepository.saveAll(cards);
        return cards;
    }
}
