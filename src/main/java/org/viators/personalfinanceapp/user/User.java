package org.viators.personalfinanceapp.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.viators.personalfinanceapp.basket.Basket;
import org.viators.personalfinanceapp.category.Category;
import org.viators.personalfinanceapp.common.BaseEntity;
import org.viators.personalfinanceapp.exceptions.DuplicateResourceException;
import org.viators.personalfinanceapp.exceptions.ResourceNotFoundException;
import org.viators.personalfinanceapp.inflationreport.InflationReport;
import org.viators.personalfinanceapp.item.Item;
import org.viators.personalfinanceapp.userpreferences.UserPreferences;
import org.viators.personalfinanceapp.common.enums.StatusEnum;
import org.viators.personalfinanceapp.common.enums.UserRolesEnum;
import org.viators.personalfinanceapp.pricealert.PriceAlert;
import org.viators.personalfinanceapp.pricecomparison.PriceComparison;
import org.viators.personalfinanceapp.shoppinglist.ShoppingList;
import org.viators.personalfinanceapp.store.Store;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_lastname", columnList = "lastname")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "age")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    @Builder.Default
    private UserRolesEnum userRole = UserRolesEnum.USER;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserPreferences userPreferences;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Item> items = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private List<PriceAlert> priceAlerts = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ShoppingList> shoppingLists = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private List<InflationReport> inflationReports = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private List<PriceComparison> priceComparisons = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Basket> baskets = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Store> stores = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User that)) return false;
        return getUuid() != null && getUuid().equals(that.getUuid());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // Helper methods
    public void addCategory(Category category) {
        if (category != null) {
            this.categories.add(category);
            category.setUser(this);
        }
    }

    public void addItem(Item item) {
        if (item != null) {
            this.getItems().add(item);
            item.setUser(this);
        }
    }

    public void addUserPreferences(UserPreferences userPreferences) {
        if (userPreferences != null) {
            this.setUserPreferences(userPreferences);
            userPreferences.setUser(this);
        }
    }

    public void addStore(Store store) {
        if (store != null) {
            if (stores.contains(store)) {
                throw new DuplicateResourceException(
                        "User already contains this store");
            }
            this.stores.add(store);
            store.setUser(this);
        }
    }

    public void removeStore(Store store) {
        if (store != null) {
            if (!stores.contains(store)) {
                throw new ResourceNotFoundException(
                        "User does not have this store"
                );
            }
            this.stores.remove(store);
            store.setUser(null);
        }
    }

    public String getFullName() {
        return this.firstName.concat(" ").concat(this.lastName);
    }

    public boolean isAdmin() {
        return this.userRole.equals(UserRolesEnum.ADMIN);
    }

    public boolean isActive() {
        return this.getStatus().equals(StatusEnum.ACTIVE.getCode());
    }
}