package org.viators.personalfinanceapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.viators.personalfinanceapp.exceptions.DuplicateResourceException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shopping_lists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingList extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "is_favorite")
    private Boolean isFavorite;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ShoppingListItem> shoppingListItems = new ArrayList<>();

    public void addShoppingListItem(ShoppingListItem item) {
        if (item != null) {
            if (shoppingListItems.contains(item)) {
                throw new DuplicateResourceException("Shopping list item already exists in shopping list");
            }
            shoppingListItems.add(item);
            item.setShoppingList(this);
        }
    }

    public void removeShoppingListItem(ShoppingListItem item) {
        if (item != null) {
            shoppingListItems.remove(item);
            item.setShoppingList(null);
        }
    }
}
