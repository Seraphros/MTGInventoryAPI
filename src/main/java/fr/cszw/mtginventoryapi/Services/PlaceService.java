package fr.cszw.mtginventoryapi.Services;

import fr.cszw.mtginventoryapi.Beans.Place;
import fr.cszw.mtginventoryapi.Repositories.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@Scope("singleton")
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;


    public boolean createPlace(String name, String userId) {
        Place placeToInsert = new Place();
        placeToInsert.setName(name);
        placeToInsert.setUserID(userId);
        placeRepository.save(placeToInsert);
        return true;
    }

    public boolean deletePlace(Place place, String userId) {
        if (place.getUserID().equals(userId)) {
            placeRepository.delete(place);
        }
        return true;
    }

    public List<Place> getAllPlaces() {
        return (List<Place>) placeRepository.findAll();
    }

    public Place getPlaceById(int id) {
        return placeRepository.findById(id).orElse(null);
    }
    public List<Place> getAllPlacesOfUser(String userId) {
        List<Place> places = placeRepository.findByUserID(userId);
        places.sort(Comparator.comparing(Place::getName));
        return places;
    }
}
