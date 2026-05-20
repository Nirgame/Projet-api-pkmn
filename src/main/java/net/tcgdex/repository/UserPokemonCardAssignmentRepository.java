package net.tcgdex.repository;

import net.tcgdex.entity.User;
import net.tcgdex.entity.UserPokemonCardAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPokemonCardAssignmentRepository extends JpaRepository<UserPokemonCardAssignment, Long> {

    List<UserPokemonCardAssignment> findByUser(User user);

    Optional<UserPokemonCardAssignment> findByUserAndPokemonId(User user, Integer pokemonId);

    void deleteByUserAndAssignedCardId(User user, String assignedCardId);
}
