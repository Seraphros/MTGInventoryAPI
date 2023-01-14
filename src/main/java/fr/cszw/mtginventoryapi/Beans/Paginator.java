package fr.cszw.mtginventoryapi.Beans;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Paginator {

    private static int ITEM_NUMBER = 100;
    private int totalCards;


    public int getItemNumber() {
        return ITEM_NUMBER;
    }
}
