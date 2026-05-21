package net.tcgdex.controller;

import net.tcgdex.entity.User;
import net.tcgdex.entity.UserCard;
import net.tcgdex.model.CardBrief;
import net.tcgdex.model.PokedexDetailView;
import net.tcgdex.model.PokedexPageResult;
import net.tcgdex.model.RegionalDisplayMode;
import net.tcgdex.model.RegionalForm;
import net.tcgdex.model.Set;
import net.tcgdex.model.Serie;
import net.tcgdex.service.CollectionService;
import net.tcgdex.service.PokedexService;
import net.tcgdex.service.TCGdexService;
import net.tcgdex.service.UserService;
import net.tcgdex.util.PokepediaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class WebController {

    @Autowired
    private TCGdexService tcgdexService;

    @Autowired
    private UserService userService;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private PokedexService pokedexService;

    @GetMapping("/")
    public String home(Model model) {
        try {
            List<Serie> series = tcgdexService.getSeries();
            model.addAttribute("series", series.subList(0, Math.min(10, series.size())));
        } catch (IOException e) {
            model.addAttribute("error", "Unable to load series");
        }
        return "home";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Nom d'utilisateur ou mot de passe invalide");
        }
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(username, password);
            redirectAttributes.addFlashAttribute("success",
                    "Inscription reussie. Vous pouvez maintenant vous connecter.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow();

        model.addAttribute("user", user);
        model.addAttribute("collectionSize", collectionService.getCollectionSize(user));

        return "dashboard";
    }

    @GetMapping("/browse")
    public String browse(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String setId,
            Authentication authentication,
            Model model) {
        Map<String, Integer> ownedCardCounts = Collections.emptyMap();
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("authenticated", true);
            model.addAttribute("username", authentication.getName());

            User user = userService.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                ownedCardCounts = new LinkedHashMap<>();
                for (UserCard userCard : collectionService.getUserCollection(user)) {
                    ownedCardCounts.merge(userCard.getCardId(), userCard.getQuantity(), Integer::sum);
                }
            }
        } else {
            model.addAttribute("authenticated", false);
        }

        try {
            List<CardBrief> cards;
            if (setId != null && !setId.trim().isEmpty()) {
                String normalizedSetId = setId.trim();
                cards = tcgdexService.getCardsBySet(normalizedSetId);
                if (cards.isEmpty()) {
                    try {
                        Set selectedSet = tcgdexService.getSet(normalizedSetId);
                        model.addAttribute("selectedSetInfo", selectedSet);
                        model.addAttribute("error",
                                selectedSet.isRegionalExclusive()
                                        ? "TCGdex ne fournit pas encore la liste des cartes pour ce set exclusif."
                                        : "TCGdex ne fournit pas encore la liste des cartes pour ce set.");
                    } catch (IOException ignored) {
                    }
                }
            } else {
                cards = tcgdexService.getCards();
            }

            if (search != null && !search.trim().isEmpty()) {
                cards = tcgdexService.filterCardsForSearch(cards, search);
            }

            cards = cards.stream()
                    .sorted(Comparator.comparing(card -> card.getImage() == null || card.getImage().isBlank()))
                    .toList();

            int safePage = Math.max(page, 1);
            int safeSize = Math.max(size, 1);
            int start = (safePage - 1) * safeSize;
            List<CardBrief> pageCards;

            if (start >= cards.size()) {
                pageCards = Collections.emptyList();
            } else {
                int end = Math.min(start + safeSize, cards.size());
                pageCards = cards.subList(start, end);
            }

            tcgdexService.enrichFormLabels(pageCards);

            int totalPages = Math.max(1, (int) Math.ceil((double) cards.size() / safeSize));
            model.addAttribute("cards", pageCards);
            model.addAttribute("currentPage", safePage);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("pageNumbers", buildCompactPageNumbers(safePage, totalPages));
            model.addAttribute("search", search);
            model.addAttribute("setId", setId);
            model.addAttribute("ownedCardCounts", ownedCardCounts);

        } catch (IOException e) {
            model.addAttribute("error", "Unable to load cards");
        }
        return "browse";
    }

    @GetMapping("/sets")
    public String sets(@RequestParam(required = false) String serieId,
            @RequestParam(required = false) List<String> languages,
            @RequestParam(defaultValue = "false") boolean mcdoOnly,
            @RequestParam(defaultValue = "false") boolean exclusiveOnly,
            @RequestParam(defaultValue = "false") boolean promoOnly,
            @RequestParam(defaultValue = "false") boolean pocketOnly,
            Model model) {
        try {
            List<Set> sets;
            if (serieId != null && !serieId.trim().isEmpty()) {
                String normalizedSerieId = serieId.trim();
                sets = tcgdexService.getSetsBySerie(normalizedSerieId);
                model.addAttribute("selectedSerieId", normalizedSerieId);

                try {
                    Serie serie = tcgdexService.getSerie(normalizedSerieId);
                    model.addAttribute("selectedSerieName", serie.getName());
                } catch (IOException ignored) {
                }
            } else {
                sets = tcgdexService.getSets();
            }

            if (languages != null && !languages.isEmpty()) {
                sets = sets.stream()
                        .filter(set -> set.isAvailableInAny(languages))
                        .toList();
            }

            if (mcdoOnly) {
                sets = sets.stream()
                        .filter(Set::isMcdoSet)
                        .toList();
            }

            if (exclusiveOnly) {
                sets = sets.stream()
                        .filter(Set::isRegionalExclusive)
                        .toList();
            }

            if (promoOnly) {
                sets = sets.stream()
                        .filter(Set::isPromoSet)
                        .toList();
            }

            if (pocketOnly) {
                sets = sets.stream()
                        .filter(Set::isPocketSet)
                        .toList();
            }

            model.addAttribute("sets", sets);
            model.addAttribute("availableLanguageOptions", List.of("en", "fr", "ja", "ko", "zh-cn", "zh-tw"));
            model.addAttribute("selectedLanguages", languages == null ? List.of() : languages);
            model.addAttribute("mcdoOnly", mcdoOnly);
            model.addAttribute("exclusiveOnly", exclusiveOnly);
            model.addAttribute("promoOnly", promoOnly);
            model.addAttribute("pocketOnly", pocketOnly);
        } catch (IOException e) {
            model.addAttribute("error", "Unable to load sets");
        }
        return "sets";
    }

    @GetMapping("/collection")
    public String collection(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow();

        model.addAttribute("user", user);
        model.addAttribute("collection", collectionService.getUserCollection(user));
        model.addAttribute("pokemonCards", collectionService.getDistinctPokemonCards(user));
        model.addAttribute("pokemonNames", collectionService.getDistinctPokemonNames(user));

        return "collection";
    }

    @GetMapping("/pokedex")
    public String pokedex(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "24") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer generation,
            @RequestParam(defaultValue = "false") boolean assignedOnly,
            @RequestParam(defaultValue = "false") boolean unassignedOnly,
            @RequestParam(defaultValue = "INCLUDE") String regionalMode,
            @RequestParam(required = false) String regionalForm,
            Authentication authentication,
            Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(authentication.getName()).orElseThrow();

        try {
            RegionalForm selectedRegionalForm = RegionalForm.fromFilterValue(regionalForm);
            RegionalDisplayMode selectedRegionalMode = RegionalDisplayMode.fromFilterValue(regionalMode);
            PokedexPageResult pageResult = pokedexService.getPokedexPage(
                    user,
                    search,
                    generation,
                    assignedOnly,
                    unassignedOnly,
                    selectedRegionalMode,
                    selectedRegionalForm,
                    page,
                    size);
            model.addAttribute("pokemons", pageResult.pokemons());
            model.addAttribute("generationOptions", pageResult.generationOptions());
            model.addAttribute("availableRegionalForms", pageResult.availableRegionalForms());
            model.addAttribute("currentPage", pageResult.currentPage());
            model.addAttribute("totalPages", pageResult.totalPages());
            model.addAttribute("totalResults", pageResult.totalResults());
            model.addAttribute("pageNumbers", buildCompactPageNumbers(pageResult.currentPage(), pageResult.totalPages()));
            model.addAttribute("search", search);
            model.addAttribute("generation", generation);
            model.addAttribute("assignedOnly", assignedOnly);
            model.addAttribute("unassignedOnly", unassignedOnly);
            model.addAttribute("regionalModes", RegionalDisplayMode.values());
            model.addAttribute("regionalMode", selectedRegionalMode.name());
            model.addAttribute("regionalForm", regionalForm);
        } catch (IOException exception) {
            model.addAttribute("error", "Impossible de charger le Pokedex pour le moment.");
        }

        return "pokedex";
    }

    @GetMapping("/pokedex/{pokemonId}")
    public String pokedexPokemon(@PathVariable int pokemonId,
            Authentication authentication,
            Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(authentication.getName()).orElseThrow();

        try {
            PokedexDetailView detailView = pokedexService.getPokemonDetail(user, pokemonId);
            model.addAttribute("detail", detailView);
        } catch (IOException exception) {
            model.addAttribute("error", "Impossible de charger cette fiche Pokemon.");
        }

        return "pokedex-detail";
    }

    @GetMapping("/pokemon")
    public String pokemon(@RequestParam String name,
            @RequestParam(required = false) String form,
            Authentication authentication,
            Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow();

        model.addAttribute("user", user);
        List<net.tcgdex.entity.UserCard> cards = collectionService.searchUserCards(user, name, form);
        model.addAttribute("pokemonName", name);
        model.addAttribute("cards", cards);

        if (!cards.isEmpty()) {
            net.tcgdex.entity.UserCard representativeCard = cards.get(0);
            model.addAttribute("pokemonDisplayName", representativeCard.getDisplayName());
            model.addAttribute("pokemonFrenchName", representativeCard.getSecondaryName());
            model.addAttribute("pokemonVariantLabel", representativeCard.getVariantLabel());
            model.addAttribute("pokemonPokepediaUrl", representativeCard.getPokepediaUrl());
        } else {
            model.addAttribute("pokemonDisplayName", name);
            model.addAttribute("pokemonPokepediaUrl", PokepediaUtils.buildPokemonUrl(name, name));
        }

        return "pokemon";
    }

    private List<Integer> buildCompactPageNumbers(int currentPage, int totalPages) {
        if (totalPages <= 9) {
            return java.util.stream.IntStream.rangeClosed(1, totalPages).boxed().toList();
        }

        List<Integer> pageNumbers = new ArrayList<>();
        pageNumbers.add(1);

        int start = Math.max(2, currentPage - 2);
        int end = Math.min(totalPages - 1, currentPage + 2);

        if (start > 2) {
            pageNumbers.add(null);
        }

        for (int page = start; page <= end; page++) {
            pageNumbers.add(page);
        }

        if (end < totalPages - 1) {
            pageNumbers.add(null);
        }

        pageNumbers.add(totalPages);
        return pageNumbers;
    }
}
