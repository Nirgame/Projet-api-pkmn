package net.tcgdex.service;

import net.tcgdex.entity.User;
import net.tcgdex.entity.UserCard;
import net.tcgdex.entity.UserTrackedSet;
import net.tcgdex.model.CardBrief;
import net.tcgdex.model.Set;
import net.tcgdex.model.TrackedSetDetailView;
import net.tcgdex.model.TrackedSetSummaryView;
import net.tcgdex.repository.UserTrackedSetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class SetTrackerService {
    private static final Pattern NATURAL_SORT_TOKEN_PATTERN = Pattern.compile("\\d+|\\D+");

    private final UserTrackedSetRepository trackedSetRepository;
    private final TCGdexService tcgdexService;
    private final CollectionService collectionService;

    public SetTrackerService(UserTrackedSetRepository trackedSetRepository,
            TCGdexService tcgdexService,
            CollectionService collectionService) {
        this.trackedSetRepository = trackedSetRepository;
        this.tcgdexService = tcgdexService;
        this.collectionService = collectionService;
    }

    public List<UserTrackedSet> getTrackedSetEntities(User user) {
        return trackedSetRepository.findByUserOrderByCreatedAtAsc(user);
    }

    public List<TrackedSetSummaryView> getTrackedSets(User user) throws IOException {
        Map<String, Integer> ownedCardCounts = collectionService.getOwnedCardCounts(user);
        return getTrackedSetEntities(user).stream()
                .map(trackedSet -> toSummaryView(trackedSet, ownedCardCounts))
                .sorted(Comparator.comparing(summary -> summary.set().getDisplayName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public void trackSet(User user, String setId) throws IOException {
        if (setId == null || setId.isBlank()) {
            throw new IllegalArgumentException("Selectionnez un set a suivre.");
        }
        if (trackedSetRepository.existsByUserAndSetId(user, setId)) {
            return;
        }

        Set set = tcgdexService.getSet(setId);
        trackedSetRepository.save(new UserTrackedSet(user, set.getId(), set.getDisplayName()));
    }

    public void untrackSet(User user, String setId) {
        trackedSetRepository.deleteByUserAndSetId(user, setId);
    }

    public TrackedSetDetailView getTrackedSetDetail(User user, String setId) throws IOException {
        UserTrackedSet trackedSet = trackedSetRepository.findByUserAndSetId(user, setId)
                .orElseThrow(() -> new IllegalArgumentException("Ce set n'est pas encore suivi."));

        Set set = tcgdexService.getSet(trackedSet.getSetId());
        List<CardBrief> cards = tcgdexService.getCardsBySet(trackedSet.getSetId()).stream()
                .sorted(Comparator.comparing(CardBrief::getLocalId, Comparator.nullsLast(this::compareNaturalLocalId))
                        .thenComparing(CardBrief::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        tcgdexService.enrichFormLabels(cards);
        tcgdexService.enrichRarities(cards);
        tcgdexService.enrichSetMetadata(cards);

        Map<String, Integer> ownedCardCounts = collectionService.getOwnedCardCounts(user);
        long ownedDistinctCards = cards.stream()
                .filter(card -> ownedCardCounts.containsKey(card.getId()))
                .count();
        long ownedCopies = cards.stream()
                .mapToLong(card -> ownedCardCounts.getOrDefault(card.getId(), 0))
                .sum();

        return new TrackedSetDetailView(set, cards, ownedCardCounts, ownedDistinctCards, ownedCopies);
    }

    private TrackedSetSummaryView toSummaryView(UserTrackedSet trackedSet, Map<String, Integer> ownedCardCounts) {
        try {
            Set set = tcgdexService.getSet(trackedSet.getSetId());
            List<CardBrief> cards = tcgdexService.getCardsBySet(trackedSet.getSetId());
            long ownedDistinctCards = cards.stream()
                    .filter(card -> ownedCardCounts.containsKey(card.getId()))
                    .count();
            long ownedCopies = cards.stream()
                    .mapToLong(card -> ownedCardCounts.getOrDefault(card.getId(), 0))
                    .sum();
            return new TrackedSetSummaryView(set, cards.size(), ownedDistinctCards, ownedCopies);
        } catch (IOException exception) {
            Set fallbackSet = new Set();
            fallbackSet.setId(trackedSet.getSetId());
            fallbackSet.setFrenchName(trackedSet.getSetName());
            return new TrackedSetSummaryView(fallbackSet, 0, 0, 0);
        }
    }

    private int compareNaturalLocalId(String left, String right) {
        Matcher leftMatcher = NATURAL_SORT_TOKEN_PATTERN.matcher(left);
        Matcher rightMatcher = NATURAL_SORT_TOKEN_PATTERN.matcher(right);

        while (leftMatcher.find() && rightMatcher.find()) {
            String leftToken = leftMatcher.group();
            String rightToken = rightMatcher.group();

            boolean leftNumeric = Character.isDigit(leftToken.charAt(0));
            boolean rightNumeric = Character.isDigit(rightToken.charAt(0));

            if (leftNumeric && rightNumeric) {
                String normalizedLeft = stripLeadingZeros(leftToken);
                String normalizedRight = stripLeadingZeros(rightToken);

                if (normalizedLeft.length() != normalizedRight.length()) {
                    return Integer.compare(normalizedLeft.length(), normalizedRight.length());
                }

                int numericComparison = normalizedLeft.compareTo(normalizedRight);
                if (numericComparison != 0) {
                    return numericComparison;
                }

                if (leftToken.length() != rightToken.length()) {
                    return Integer.compare(leftToken.length(), rightToken.length());
                }
                continue;
            }

            int textComparison = leftToken.toLowerCase(Locale.ROOT)
                    .compareTo(rightToken.toLowerCase(Locale.ROOT));
            if (textComparison != 0) {
                return textComparison;
            }
        }

        if (leftMatcher.find()) {
            return 1;
        }
        if (rightMatcher.find()) {
            return -1;
        }

        return left.compareToIgnoreCase(right);
    }

    private String stripLeadingZeros(String value) {
        String normalized = value.replaceFirst("^0+(?!$)", "");
        return normalized.isEmpty() ? "0" : normalized;
    }
}
