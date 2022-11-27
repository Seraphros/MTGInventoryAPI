package fr.cszw.mtginventoryapi.Services;

import fr.cszw.mtginventoryapi.Beans.Card;
import fr.cszw.mtginventoryapi.Beans.Place;
import fr.cszw.mtginventoryapi.Beans.Set;
import fr.cszw.mtginventoryapi.Repositories.PlaceRepository;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Scope("singleton")
public class PlaceService {

    private PlaceRepository placeRepository;

    public PlaceService(PlaceRepository repository) {
        this.placeRepository = repository;
    }

    public boolean createPlace(String name, String userId) {
        Place placeToInsert = new Place();
        placeToInsert.setName(name);
        placeToInsert.setUserID(userId);
        placeRepository.save(placeToInsert);
        return true;
    }

    public List<Place> getAllPlaces() {
        return (List<Place>) placeRepository.findAll();
    }

    public Place getPlaceById(int id) {
        return placeRepository.findById(id).orElse(null);
    }
    public List<Place> getAllPlacesOfUser(String userId) {
        return (List<Place>) placeRepository.findByUserID(userId);
    }
}
