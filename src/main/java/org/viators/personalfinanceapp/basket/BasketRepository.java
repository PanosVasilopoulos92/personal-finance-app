package org.viators.personalfinanceapp.basket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BasketRepository extends JpaRepository<Basket, Long> {

    List<Basket> findByUser(Long userId);

    List<Basket> findByUserAndName(Long userId, String name);

    @Query("""
            select b from Basket b
            left join fetch b.basketItems
            where b.id = :basketId
            """)
    Optional<Basket> findBasketWithItems(@Param("basketId") Long basketId);
}