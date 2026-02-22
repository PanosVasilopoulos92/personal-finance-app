package org.viators.personalfinanceapp.store;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.viators.personalfinanceapp.common.BaseEntity;
import org.viators.personalfinanceapp.user.User;
import org.viators.personalfinanceapp.common.enums.StoreTypeEnum;

@Entity
@Table(
        name = "stores",
        indexes = @Index(
                name = "idx_store_name", columnList = "store_name"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Store extends BaseEntity {

    @Column(name = "store_name", nullable = false)
    private String name;

    @Column(name = "store_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private StoreTypeEnum storeType;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "region")
    private String region;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "website")
    private String website;

    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false)
    private User user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Store that)) return false;
        return getUuid() != null && getUuid().equals(that.getUuid());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
