package net.tcgdex.repository;

import net.tcgdex.entity.User;
import net.tcgdex.entity.UserTrackedSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTrackedSetRepository extends JpaRepository<UserTrackedSet, Long> {

    List<UserTrackedSet> findByUserOrderByCreatedAtAsc(User user);

    Optional<UserTrackedSet> findByUserAndSetId(User user, String setId);

    boolean existsByUserAndSetId(User user, String setId);

    void deleteByUserAndSetId(User user, String setId);

    long countByUser(User user);
}
