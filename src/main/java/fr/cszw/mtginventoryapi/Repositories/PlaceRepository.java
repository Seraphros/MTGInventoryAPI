package fr.cszw.mtginventoryapi.Repositories;

import fr.cszw.mtginventoryapi.Beans.Place;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PlaceRepository extends CrudRepository<Place, Integer> {
    List<Place> findByUserID(String userId);
}