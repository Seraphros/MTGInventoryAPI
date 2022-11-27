package fr.cszw.mtginventoryapi.Controllers;

import fr.cszw.mtginventoryapi.Beans.Card;
import fr.cszw.mtginventoryapi.Services.CollectionService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
public class CollectionController {

    @Autowired
    CollectionService collectionService;

    @PostMapping(path = "/collection")
    public boolean insertCardsIntoCollection(KeycloakAuthenticationToken principal,@Valid @NotNull @RequestBody List<Card> cards) throws Exception {
        return collectionService.addCardsToCollection(cards, principal.getName());
    }

    @PostMapping(path = "/collection/evaluate")
    public List<Card> evaluateCardsPrice(KeycloakAuthenticationToken principal) {
        return collectionService.evaluateCardsPrice(principal.getName());
    }
}
