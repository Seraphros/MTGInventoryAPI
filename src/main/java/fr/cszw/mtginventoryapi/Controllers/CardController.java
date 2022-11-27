package fr.cszw.mtginventoryapi.Controllers;

import fr.cszw.mtginventoryapi.Beans.Card;
import fr.cszw.mtginventoryapi.Repositories.CardRepository;
import fr.cszw.mtginventoryapi.Services.CardJSONService;
import fr.cszw.mtginventoryapi.Services.SetJSONService;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@RestController
public class CardController {

    @Autowired
    SetJSONService setJSONService;

    @Autowired
    CardJSONService cardJSONService;

    private final Logger logger = LoggerFactory.getLogger(CardController.class);



    @Autowired
    private CardRepository cardRepository;


    @GetMapping(path = "/card/search")
    public String index(String name) throws Exception {
        logger.info("REQUEST TO SEARCH CARD");

        URL url = new URL("https://api.scryfall.com/cards/search?q="+ name + "+and+lang=fr&unique=prints");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        return content.toString();
    }

    @GetMapping(path = "/card/searchLocal")
    public List<Card> indexLocal(String name) throws Exception {
        return cardJSONService.findCardByName(name);
    }

    @GetMapping(path = "/db")
    public List<Card> getAllCards() {
        return (List<Card>) cardRepository.findAll();
    }

    @GetMapping(path = "/customers")
    public String customers(KeycloakAuthenticationToken principal) {
        logger.info("REQUEST TO CUSTOMERS");
        SimpleKeycloakAccount details = (SimpleKeycloakAccount) principal.getDetails();
        KeycloakPrincipal principal1 = (KeycloakPrincipal) details.getPrincipal();

        return principal1.getKeycloakSecurityContext().getToken().getPreferredUsername();

    }
}
