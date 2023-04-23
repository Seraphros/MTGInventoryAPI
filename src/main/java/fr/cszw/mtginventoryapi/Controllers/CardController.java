package fr.cszw.mtginventoryapi.Controllers;

import fr.cszw.mtginventoryapi.Beans.Card;
import fr.cszw.mtginventoryapi.Repositories.CardRepository;
import fr.cszw.mtginventoryapi.Services.CardService;
import fr.cszw.mtginventoryapi.Services.CollectionService;
import fr.cszw.mtginventoryapi.Services.FileDownloader;
import fr.cszw.mtginventoryapi.Services.SetJSONService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CardController {


    private final SetJSONService setJSONService;
    private final CardService cardService;
    private final FileDownloader fileDownloader;
    private final CollectionService collectionService;
    private final CardRepository cardRepository;

    @GetMapping(path = "/card/search")
    public String index(String name) throws Exception {
        log.info("REQUEST TO SEARCH CARD");

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

    @GetMapping(path = "/card/update")
    public void update() {
        this.collectionService.updateAllCards();
    }

    @GetMapping(path = "/card/searchLocal")
    public List<Card> indexLocal(String name) {
        return cardService.findCardByName(name);
    }

    @GetMapping(path = "/card/searchByEditionNumber")
    public Card findCardByEditionNumber(String edition, String editionNumber) {
        return cardService.findCardByEditionNumber(edition, editionNumber);
    }

    @GetMapping(path = "/db")
    public List<Card> getAllCards() {
        return (List<Card>) cardRepository.findAll();
    }

    @GetMapping(path = "/customers")
    public String customers(KeycloakAuthenticationToken principal) {
        log.info("REQUEST TO CUSTOMERS");
        SimpleKeycloakAccount details = (SimpleKeycloakAccount) principal.getDetails();
        KeycloakPrincipal principal1 = (KeycloakPrincipal) details.getPrincipal();

        return principal1.getKeycloakSecurityContext().getToken().getPreferredUsername();
    }

    @GetMapping(path = "/card/download", produces = "application/json")
    public void test() {
        fileDownloader.download("/default-cards/default-cards-20230101100506.json");
    }
}
