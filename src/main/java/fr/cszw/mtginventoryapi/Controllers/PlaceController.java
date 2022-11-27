package fr.cszw.mtginventoryapi.Controllers;

import fr.cszw.mtginventoryapi.Beans.Card;
import fr.cszw.mtginventoryapi.Beans.Place;
import fr.cszw.mtginventoryapi.Repositories.CardRepository;
import fr.cszw.mtginventoryapi.Repositories.PlaceRepository;
import fr.cszw.mtginventoryapi.Services.CardJSONService;
import fr.cszw.mtginventoryapi.Services.PlaceService;
import fr.cszw.mtginventoryapi.Services.SetJSONService;
import org.jetbrains.annotations.NotNull;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;
import java.util.List;

@RestController
public class PlaceController {

    @Autowired
    private PlaceService placeService;


    @GetMapping(path = "/places")
    public List<Place> indexLocal(KeycloakAuthenticationToken principal) throws Exception {
        return placeService.getAllPlacesOfUser(principal.getName());
    }

    @PostMapping(path = "/places")
    @ResponseBody
    public boolean createPlace(KeycloakAuthenticationToken principal, @Valid @RequestBody @NotNull Place name) throws Exception {
        return placeService.createPlace(name.getName(), principal.getName());
    }
}
