package fr.cszw.mtginventoryapi.Controllers;

import fr.cszw.mtginventoryapi.Beans.Card;
import fr.cszw.mtginventoryapi.Beans.Paginator;
import fr.cszw.mtginventoryapi.Beans.PaginatorRequest;
import fr.cszw.mtginventoryapi.Services.CollectionService;
import lombok.RequiredArgsConstructor;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("collection")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @PostMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Card> insertCardsIntoCollection(KeycloakAuthenticationToken principal,@Valid @NotNull @RequestBody List<Card> cards) {
        return collectionService.addCardsToCollection(cards, principal.getName());
    }

    @DeleteMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteCardsOfCollection(KeycloakAuthenticationToken principal, @Valid @NotNull @RequestBody List<Card> cards) {
        collectionService.removeCardsOfCollection(cards, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/evaluate", produces = "application/json")
    public List<Card> evaluateCardsPrice(KeycloakAuthenticationToken principal) {
        return collectionService.evaluateCardsPrice(principal.getName());
    }

    @GetMapping(path = "/evaluateByCard", produces = "application/json")
    public Card evaluateCardsPrice(KeycloakAuthenticationToken principal, @RequestParam @NotNull int id) {
        return collectionService.evaluateCardPrice(id, principal.getName());
    }

    @GetMapping(path = "/total", produces = "application/text")
    public String getTotalCards(KeycloakAuthenticationToken principal) {
        return collectionService.getAllNumberCardsOfUser(principal.getName()).toString();
    }

    @GetMapping(path = "/", produces = "application/json")
    public List<Card> getTotalCollection(KeycloakAuthenticationToken principal) {
        return collectionService.getAllCardOfUser(principal.getName());
    }

    @PostMapping(path = "/paginated", produces = "application/json")
    public List<Card> getTotalCollection(KeycloakAuthenticationToken principal,  @Valid @NotNull @RequestBody PaginatorRequest search) {
        return collectionService.getAllCardOfUserPaginated(principal.getName(), search.getPage(), search.getSearch(), search.getOrder());
    }

    @PostMapping(path = "/paginator", produces = "application/json")
    public Paginator getPaginator(KeycloakAuthenticationToken principal, @Valid @NotNull @RequestBody PaginatorRequest search) {
        return collectionService.getCollectionPaginator(principal.getName(), search.getSearch());
    }

    @GetMapping(path = "/place")
    public List<Card> getPlaceCards(KeycloakAuthenticationToken principal, @RequestParam @NotNull int id) {
        return collectionService.getCardsOfPlace(principal.getName(), id);
    }

    @GetMapping(path = "/placesOrdered")
    public List<Card> getOrderedPlaceCards(KeycloakAuthenticationToken principal, @RequestParam @NotNull String placesId) {
        return collectionService.getOrderedCardsOfPlaces(principal.getName(), placesId);
    }

}
