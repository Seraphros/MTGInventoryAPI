package fr.cszw.mtginventoryapi.Repositories;


import fr.cszw.mtginventoryapi.Beans.Card;
import fr.cszw.mtginventoryapi.Beans.Place;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CardRepository extends CrudRepository<Card, Integer> {
    List<Card> findByOwner(String userId);
    Card findByOwnerAndScryfallIDAndFoilIs(String userId, String scryfallId, boolean isFoil);
    Card getCardByOwnerAndScryfallIDAndFoilIs(String userId, String scryfallId, boolean isFoil);
}