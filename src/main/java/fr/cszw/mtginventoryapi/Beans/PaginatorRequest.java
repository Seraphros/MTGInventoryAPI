package fr.cszw.mtginventoryapi.Beans;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PaginatorRequest {

    private int page = 0;
    private String search = "";

    private String order = "name/ASC";
}
