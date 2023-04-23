package fr.cszw.mtginventoryapi.Services;

import fr.cszw.mtginventoryapi.Beans.Card;
import fr.cszw.mtginventoryapi.Beans.CardPrice;
import fr.cszw.mtginventoryapi.Beans.Set;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@Scope("singleton")
@Slf4j
public class CardService {

    private final SetJSONService setJSONService;


    public static final String CARD_NAME = "name";
    public static final String CARD_LANG = "lang";
    public static final String CARD_PRINTED_NAME = "printed_name";
    public static final String CARD_EDITION = "set";
    public static final String CARD_EDITION_NUMBER = "collector_number";
    public static final String CARD_SCRYFALL_ID = "id";
    public static final String CARD_ILLUSTRATION_LINK = "normal";
    public static final String CARD_LINKS = "purchase_uris";
    public static final String CARD_TYPE = "type_line";
    public static final String CARD_MANA = "mana_cost";
    public static final String CARD_CMC = "cmc";
    public static final String CARD_RARITY = "rarity";
    final String EUR_PRICE = "eur";
    final String EUR_PRICE_FOILED = "eur_foil";
    final String USD_PRICE = "usd";
    final String USD_PRICE_FOILED = "usd_foil";


    public List<Card> cardList;
    public List<CardPrice> priceList;


    public CardService(SetJSONService setJSONService, FileDownloader fileDownloader) {
        this.setJSONService = setJSONService;

        cardList = new ArrayList<>();

        try {
            InputStream jsonFile = Model.class.getClassLoader().getResourceAsStream("all-cards.json");
            cardList = parseJSONFullCardFromInputStream(jsonFile, false, false);
            if (jsonFile != null) jsonFile.close();

            File priceFile = new File(System.getenv("PRICE_FILE"));
            if (!priceFile.exists()) fileDownloader.download("/default-cards/default-cards-20230116100518.json");
            InputStream priceFileStream = new FileInputStream(priceFile);
            priceList = parseJSONPriceCardFromInputStream(priceFileStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Card> parseJSONFullCardFromInputStream(InputStream in, boolean onlyOneCard, boolean reversed) throws Exception {
        int numberOfRecords = 0;
        JsonFactory jsonfactory = new JsonFactory(); //init factory
        JsonParser jsonParser = jsonfactory.createJsonParser(in); //create JSON parser
        JsonToken jsonToken = jsonParser.nextToken();

        Card card = new Card();
        List<Card> listOfCard = new ArrayList<>();

        int objectDepth = 0;

        while (jsonToken != null) { //Iterate all elements of array
            String fieldname = jsonParser.getCurrentName();

            if (CARD_SCRYFALL_ID.equals(fieldname) && objectDepth == 1) {
                jsonToken = jsonParser.nextToken(); //read next token
                card.setScryfallID(jsonParser.getText());
            } else if (CARD_NAME.equals(fieldname) && objectDepth == 1) {
                jsonToken = jsonParser.nextToken();
                if (reversed) card.setEnglishName(jsonParser.getText());
                if (!reversed) card.setName(jsonParser.getText());
            } else if (CARD_LANG.equals(fieldname) && objectDepth == 1) {
                jsonToken = jsonParser.nextToken();
                card.setLang(jsonParser.getText());
            } else if (CARD_PRINTED_NAME.equals(fieldname) && objectDepth == 1) {
                jsonToken = jsonParser.nextToken();
                if (!reversed) card.setEnglishName(jsonParser.getText());
                if (reversed) card.setName(jsonParser.getText());
            } else if (CARD_EDITION.equals(fieldname) && objectDepth == 1) {
                jsonToken = jsonParser.nextToken();
                String txt = jsonParser.getText();
                card.setEdition(txt);
                card.setSet(getSet(card.getEdition()));
            } else if (CARD_EDITION_NUMBER.equals(fieldname) && objectDepth == 1) {
                jsonToken = jsonParser.nextToken();
                card.setEditionNumber(jsonParser.getText());
            } else if (CARD_ILLUSTRATION_LINK.equals(fieldname) && objectDepth == 2) {
                jsonToken = jsonParser.nextToken();
                card.setIllustration(jsonParser.getText());
            } else if (CARD_LINKS.equals(fieldname) && objectDepth == 2) {
                jsonToken = jsonParser.nextToken();
                card.setCMLink(jsonParser.getText());
            } else if (CARD_TYPE.equals(fieldname) && objectDepth == 1) {
                jsonToken = jsonParser.nextToken();
                card.setType(jsonParser.getText());
            } else if (CARD_CMC.equals(fieldname) && objectDepth == 1) {
                jsonToken = jsonParser.nextToken();
                card.setCmc((int) Float.parseFloat(jsonParser.getText()));
            } else if (CARD_RARITY.equals(fieldname) && objectDepth == 1) {
                jsonToken = jsonParser.nextToken();
                card.setRarity(jsonParser.getText());
            } else if (CARD_MANA.equals(fieldname) && objectDepth == 1) {
                jsonToken = jsonParser.nextToken();
                card.setMana(jsonParser.getText());
                if (card.getMana().contains("{R}") &&
                        !card.getMana().contains("{G}") &&
                        !card.getMana().contains("{U}") &&
                        !card.getMana().contains("{B}") &&
                        !card.getMana().contains("{W}")) {
                    card.setColor("red");
                } else if (!card.getMana().contains("{R}") &&
                        card.getMana().contains("{G}") &&
                        !card.getMana().contains("{U}") &&
                        !card.getMana().contains("{B}") &&
                        !card.getMana().contains("{W}")) {
                    card.setColor("green");
                } else if (!card.getMana().contains("{R}") &&
                        !card.getMana().contains("{G}") &&
                        card.getMana().contains("{U}") &&
                        !card.getMana().contains("{B}") &&
                        !card.getMana().contains("{W}")) {
                    card.setColor("blue");
                } else if (!card.getMana().contains("{R}") &&
                        !card.getMana().contains("{G}") &&
                        !card.getMana().contains("{U}") &&
                        card.getMana().contains("{B}") &&
                        !card.getMana().contains("{W}")) {
                    card.setColor("black");
                } else if (!card.getMana().contains("{R}") &&
                        !card.getMana().contains("{G}") &&
                        !card.getMana().contains("{U}") &&
                        !card.getMana().contains("{B}") &&
                        card.getMana().contains("{W}")) {
                    card.setColor("white");
                } else if (!card.getMana().contains("{R}") &&
                        !card.getMana().contains("{G}") &&
                        !card.getMana().contains("{U}") &&
                        !card.getMana().contains("{B}") &&
                        !card.getMana().contains("{W}")) {
                    card.setColor("colorless");
                } else {
                    card.setColor("multicolor");
                }
            }

            if (jsonToken == JsonToken.START_OBJECT) objectDepth++;

            if (jsonToken == JsonToken.END_OBJECT) {
                objectDepth--;
                if (objectDepth == 0) {
                    //do some processing, Indexing, saving in DB etc..
                    if (card.getLang().equals("en") || card.getLang().equals("fr") || card.getLang().equalsIgnoreCase("ja") || card.getLang().equalsIgnoreCase("ph")) {
                        if (card.getLang().equals("fr")) {
                            String en = card.getName();
                            if (card.getEnglishName() != null) card.setName(card.getEnglishName());
                            card.setEnglishName(en);
                            card.setCMLink("https://www.cardmarket.com/en/Magic/Products/Search?searchString=" + en);
                        } else {
                            card.setCMLink("https://www.cardmarket.com/en/Magic/Products/Search?searchString=" + card.getName());

                        }

                        listOfCard.add(card);
                        numberOfRecords++;
                    }

                    card = new Card();
                }
            }
            jsonToken = jsonParser.nextToken();

        }
        if (onlyOneCard) {
            listOfCard.add(card);
        }

        /*listOfCard.forEach(analysedCard -> {
            if (analysedCard.getLang().equalsIgnoreCase("fr")) {
                Card enCounterpart = listOfCard.stream().filter(obj -> obj.getEdition().equals(analysedCard.getEdition()) && obj.getEditionNumber().equals(analysedCard.getEditionNumber()) && obj.getLang().equalsIgnoreCase("en")).findFirst().orElse(null);
                if (enCounterpart != null) analysedCard.setEnScryfallId(enCounterpart.getScryfallID());
            }
        });*/

        log.info("Total Cards Records Found : " + numberOfRecords);
        return listOfCard;
    }

    private List<CardPrice> parseJSONPriceCardFromInputStream(InputStream in) throws Exception {
        int numberOfRecords = 0;
        JsonFactory jsonfactory = new JsonFactory(); //init factory
        JsonParser jsonParser = jsonfactory.createJsonParser(in); //create JSON parser
        JsonToken jsonToken = jsonParser.nextToken();

        CardPrice card = new CardPrice();
        List<CardPrice> listOfCard = new ArrayList<>();

        int objectDepth = 0;

        while (jsonToken != null) { //Iterate all elements of array
            String fieldname = jsonParser.getCurrentName();

            if (CARD_SCRYFALL_ID.equals(fieldname) && objectDepth == 1) {
                jsonToken = jsonParser.nextToken(); //read next token
                card.setScryfallId(jsonParser.getText());
            } else if (CARD_NAME.equals(fieldname) && objectDepth == 1) {
                jsonToken = jsonParser.nextToken();
                card.setName(jsonParser.getText());
            } else if (CARD_EDITION.equals(fieldname) && objectDepth == 1) {
                jsonToken = jsonParser.nextToken();
                card.setEdition(jsonParser.getText());
            } else if (CARD_EDITION_NUMBER.equals(fieldname) && objectDepth == 1) {
                jsonToken = jsonParser.nextToken();
                card.setEditionNumber(jsonParser.getText());
            } else if (EUR_PRICE.equals(fieldname) && objectDepth == 2) {
                jsonToken = jsonParser.nextToken(); //read next token
                String price = jsonParser.getText();
                if (!price.equals("null")) card.setPriceEur(Double.parseDouble(price));
            } else if (EUR_PRICE_FOILED.equals(fieldname) && objectDepth == 2) {
                jsonToken = jsonParser.nextToken(); //read next token
                String price = jsonParser.getText();
                if (!price.equals("null")) card.setPriceFoilEur(Double.parseDouble(price));
            } else if (USD_PRICE.equals(fieldname) && objectDepth == 2) {
                jsonToken = jsonParser.nextToken(); //read next token
                String price = jsonParser.getText();
                if (!price.equals("null")) card.setPriceUSD(Double.parseDouble(price));
            } else if (USD_PRICE_FOILED.equals(fieldname) && objectDepth == 2) {
                jsonToken = jsonParser.nextToken(); //read next token
                String price = jsonParser.getText();
                if (!price.equals("null")) card.setPriceFoilUSD(Double.parseDouble(price));
            }

            if (jsonToken == JsonToken.START_OBJECT) objectDepth++;

            if (jsonToken == JsonToken.END_OBJECT) {
                objectDepth--;
                if (objectDepth == 0) {
                    listOfCard.add(card);
                    card = new CardPrice();
                    numberOfRecords++;
                }
            }

            jsonToken = jsonParser.nextToken();
        }


        log.info("Total Cards Prices Records Found : " + numberOfRecords);
        return listOfCard;
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

    public Card findCardByEditionNumber(String edition, String editionNumber) {
        Card found = this.cardList.stream()
                .filter(card ->
                {
                    if (card.getScryfallID() == null) return false;
                    return card.getEdition().equalsIgnoreCase(edition) && card.getEditionNumber().equalsIgnoreCase(editionNumber) && card.getLang().equalsIgnoreCase("fr");
                })
                .findFirst().orElse(null);

        if (found != null) return found;
        try {
            URL url = new URL("https://api.scryfall.com/cards/" + edition.toLowerCase() + "/" + editionNumber + "/fr");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            return parseJSONFullCardFromInputStream(con.getInputStream(), true, true).stream().findFirst().orElse(null);
        } catch (Exception e) {
            try {
                URL url = new URL("https://api.scryfall.com/cards/" + edition.toLowerCase() + "/" + editionNumber);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                return parseJSONFullCardFromInputStream(con.getInputStream(), true, false).stream().findFirst().orElse(null);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return null;
    }

}
