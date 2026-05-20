package net.tcgdex.repository;

import net.tcgdex.entity.User;
import net.tcgdex.entity.UserCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCardRepository extends JpaRepository<UserCard, Long> {

    List<UserCard> findByUser(User user);

    Optional<UserCard> findByUserAndCardId(User user, String cardId);

    boolean existsByUserAndCardId(User user, String cardId);

    @Query("SELECT uc FROM UserCard uc WHERE uc.user = :user AND LOWER(uc.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<UserCard> findByUserAndNameContaining(@Param("user") User user, @Param("name") String name);

    @Query("SELECT DISTINCT uc.name FROM UserCard uc WHERE uc.user = :user ORDER BY uc.name")
    List<String> findDistinctPokemonNamesByUser(@Param("user") User user);

    long countByUser(User user);
}