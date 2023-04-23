package fr.cszw.mtginventoryapi.Services;

import fr.cszw.mtginventoryapi.Beans.Set;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SetJSONService {
    String[] ips;

    public static final String SET_ID = "id";
    public static final String SET_NAME = "name";
    public static final String SET_CODE = "code";
    public static final String SET_ICON = "icon_svg_uri";
    public static final String SET_DATE = "released_at";


    public List<Set> setList;

    public SetJSONService() {
        setList = new ArrayList<>();

        try {
            URL url = new URL("https://api.scryfall.com/sets");
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

                if (SET_ID.equals(fieldname)) {
                    jsonToken = jsonParser.nextToken(); //read next token
                    set.setId(jsonParser.getText());
                } else if (SET_NAME.equals(fieldname)) {
                    jsonToken = jsonParser.nextToken();
                    set.setSet_name(jsonParser.getText());
                } else if (SET_CODE.equals(fieldname)) {
                    jsonToken = jsonParser.nextToken();
                    set.setSet(jsonParser.getText());
                } else if (SET_ICON.equals(fieldname)) {
                    jsonToken = jsonParser.nextToken();
                    set.setLogo(jsonParser.getText());
                } else if (SET_DATE.equals(fieldname)) {
                    jsonToken = jsonParser.nextToken();
                    set.setReleasedDate(jsonParser.getText());
                }

                if (jsonToken == JsonToken.START_OBJECT) objectDepth++;

                if (jsonToken == JsonToken.END_OBJECT) {
                    objectDepth--;
                    if (objectDepth == 1) {
                        //do some processing, Indexing, saving in DB etc..
                        setList.add(set);
                        numberOfRecords++;
                    }

                    set = new Set();
                }

                jsonToken = jsonParser.nextToken();
            }

            log.info("Total Set Records Found : " + numberOfRecords);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Set getSetFromList(String setCode) {
        return this.setList.stream().filter(set -> set.getSet().equals(setCode)).findFirst().orElse(null);
    }
}
