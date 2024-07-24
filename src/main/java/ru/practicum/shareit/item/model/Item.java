package ru.practicum.shareit.item.model;

import lombok.*;

/**
 * Created Shchetinin Alexander
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Item {

    private long id;
    private String name;
    private String description;
    private long owner;
    private boolean available;

}
