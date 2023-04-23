package fr.cszw.mtginventoryapi.Controllers;

import fr.cszw.mtginventoryapi.Beans.Place;
import fr.cszw.mtginventoryapi.Services.PlaceService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;


    @GetMapping(path = "/places")
    public List<Place> indexLocal(KeycloakAuthenticationToken principal) throws Exception {
        return placeService.getAllPlacesOfUser(principal.getName());
    }

    @PostMapping(path = "/places")
    @ResponseBody
    public boolean createPlace(KeycloakAuthenticationToken principal, @Valid @RequestBody @NotNull Place name) throws Exception {
        return placeService.createPlace(name.getName(), principal.getName());
    }

    @DeleteMapping(path = "/places")
    @ResponseBody
    public boolean deletePlace(KeycloakAuthenticationToken principal, @Valid @RequestBody @NotNull Place name) throws Exception {
        return placeService.deletePlace(name, principal.getName());
    }
}
