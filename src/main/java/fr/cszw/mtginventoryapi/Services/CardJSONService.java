package fr.cszw.mtginventoryapi.Services;

import ch.qos.logback.classic.Logger;
import fr.cszw.mtginventoryapi.Beans.Card;
import fr.cszw.mtginventoryapi.Beans.Set;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Scope("singleton")
public class CardJSONService {

    private final SetJSONService setJSONService;

    public static final String CARD_ID = "cardmarket_id";
    public static final String CARD_NAME = "name";
    public static final String CARD_LANG = "lang";
    public static final String CARD_PRINTED_NAME = "printed_name";
    public static final String CARD_EDITION = "set";
    public static final String CARD_EDITION_NUMBER = "collector_number";
    public static final String CARD_SCRYFALL_ID = "id";
    public static final String CARD_ILLUSTRATION_LINK = "normal";
    public static final String CARD_LINKS = "purchase_uris";
    public static final String CARD_TYPE = "type_line";


    public List<Card> cardList;



    public CardJSONService(SetJSONService setJSONService) {
        this.setJSONService = setJSONService;
        cardList = new ArrayList<>();

        try {
            InputStream jsonFile = Model.class.getClassLoader().getResourceAsStream("all-cards.json");
            JsonFactory jsonfactory = new JsonFactory(); //init factory
            int numberOfRecords = 0;

            JsonParser jsonParser = jsonfactory.createJsonParser(jsonFile); //create JSON parser
            JsonToken jsonToken = jsonParser.nextToken();

            Card card = new Card();
            int objectDepth = 0;

            while (jsonToken != null) { //Iterate all elements of array
                String fieldname = jsonParser.getCurrentName();

                if (CARD_SCRYFALL_ID.equals(fieldname)) {
                    jsonToken = jsonParser.nextToken(); //read next token
                    card.setScryfallID(jsonParser.getText());
                } else if (CARD_NAME.equals(fieldname)) {
                    jsonToken = jsonParser.nextToken();
                    card.setName(jsonParser.getText());
                } else if (CARD_LANG.equals(fieldname)) {
                    jsonToken = jsonParser.nextToken();
                    card.setLang(jsonParser.getText());
                } else if (CARD_PRINTED_NAME.equals(fieldname)) {
                    jsonToken = jsonParser.nextToken();
                    card.setEnglishName(jsonParser.getText());
                } else if (CARD_EDITION.equals(fieldname)) {
                    jsonToken = jsonParser.nextToken();
                    String txt = jsonParser.getText();
                    card.setEdition(txt);
                    card.setSet(getSet(card.getEdition()));
                } else if (CARD_EDITION_NUMBER.equals(fieldname)) {
                    jsonToken = jsonParser.nextToken();
                    card.setEditionNumber(jsonParser.getText());
                } else if (CARD_ILLUSTRATION_LINK.equals(fieldname)) {
                    jsonToken = jsonParser.nextToken();
                    card.setIllustration(jsonParser.getText());
                } else if (CARD_LINKS.equals(fieldname)) {
                    jsonToken = jsonParser.nextToken();
                    card.setCMLink(jsonParser.getText());
                } else if (CARD_TYPE.equals(fieldname)) {
                    jsonToken = jsonParser.nextToken();
                    card.setType(jsonParser.getText());
                }

                if (jsonToken == JsonToken.START_OBJECT) objectDepth++;

                if (jsonToken == JsonToken.END_OBJECT) {
                    objectDepth--;
                    if (objectDepth == 0) {
                        //do some processing, Indexing, saving in DB etc..
                        if (card.getLang().equals("en") || card.getLang().equals("fr")) {
                            if (card.getLang().equals("fr")) {
                                String en = card.getName();
                                if (card.getEnglishName() != null) card.setName(card.getEnglishName());
                                card.setEnglishName(en);
                                card.setCMLink("https://www.cardmarket.com/en/Magic/Products/Search?searchString=" + en);
                            } else {
                                card.setCMLink("https://www.cardmarket.com/en/Magic/Products/Search?searchString=" + card.getName());

                            }

                            cardList.add(card);
                            numberOfRecords++;
                        }

                        card = new Card();
                    }
                }

                jsonToken = jsonParser.nextToken();
            }

            if (jsonFile != null) jsonFile.close();

            System.out.println("Total Cards Records Found : " + numberOfRecords);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set getSet(String setExt) {
        return setJSONService.setList.stream()
                .filter(set -> set.getSet().equals(setExt))
                .findFirst()
                .orElse(null);
    }

    public List<Card> findCardByName(String sequence) {
        return this.cardList.stream()
                .filter(card ->
                {
                    if (card.getName() == null) return false;
                    return card.getName().toLowerCase().contains(sequence.toLowerCase());
                })
                .toList();
    }
    public Card findCardByScryfallId(String sequence) {
        return this.cardList.stream()
                .filter(card ->
                {
                    if (card.getScryfallID() == null) return false;
                    return card.getScryfallID().toLowerCase().contains(sequence.toLowerCase());
                })
                .findFirst().orElse(null);
    }

}
