package net.tcgdex.util;

import net.tcgdex.entity.UserCard;
import net.tcgdex.model.CardBrief;
import net.tcgdex.model.GenerationOption;
import net.tcgdex.model.PokemonAlternativeForm;
import net.tcgdex.model.PokemonSpeciesInfo;
import net.tcgdex.model.RegionalDisplayMode;
import net.tcgdex.model.Set;
import net.tcgdex.service.UiLanguageService;
import org.springframework.stereotype.Component;

@Component("displayText")
public class DisplayTextHelper {

    private final UiLanguageService uiLanguageService;

    public DisplayTextHelper(UiLanguageService uiLanguageService) {
        this.uiLanguageService = uiLanguageService;
    }

    public boolean isEnglish() {
        return "en".equalsIgnoreCase(uiLanguageService.getCurrentLanguage());
    }

    public String text(String french, String english) {
        return isEnglish()
                ? coalesce(english, french)
                : coalesce(french, english);
    }

    public String cardName(CardBrief card) {
        if (card == null) {
            return "";
        }
        return text(card.getFrenchName(), card.getEnglishName());
    }

    public String cardSecondary(CardBrief card) {
        if (card == null) {
            return null;
        }
        return secondary(text(card.getFrenchName(), card.getEnglishName()), text(card.getEnglishName(), card.getFrenchName()));
    }

    public String userCardName(UserCard card) {
        if (card == null) {
            return "";
        }
        return text(card.getFrenchName(), card.getName());
    }

    public String userCardSecondary(UserCard card) {
        if (card == null) {
            return null;
        }
        return secondary(text(card.getFrenchName(), card.getName()), text(card.getName(), card.getFrenchName()));
    }

    public String pokemonName(PokemonSpeciesInfo species) {
        if (species == null) {
            return "";
        }
        return text(resolveFrenchPokemonName(species), resolveEnglishPokemonName(species));
    }

    public String pokemonSecondary(PokemonSpeciesInfo species) {
        if (species == null) {
            return null;
        }
        return secondary(
                text(resolveFrenchPokemonName(species), resolveEnglishPokemonName(species)),
                text(resolveEnglishPokemonName(species), resolveFrenchPokemonName(species)));
    }

    public String setName(Set set) {
        if (set == null) {
            return "";
        }
        return text(set.getFrenchName(), set.getEnglishName());
    }

    public String setSecondary(Set set) {
        if (set == null) {
            return null;
        }
        return secondary(text(set.getFrenchName(), set.getEnglishName()), text(set.getEnglishName(), set.getFrenchName()));
    }

    public String cardSetName(CardBrief card) {
        if (card == null) {
            return null;
        }
        return blankToNull(coalesce(card.getDisplaySetName()));
    }

    public String userCardSetName(UserCard card) {
        if (card == null) {
            return null;
        }
        return blankToNull(coalesce(card.getDisplaySetName()));
    }

    public String generationLabel(GenerationOption option) {
        if (option == null) {
            return "";
        }
        return isEnglish() ? "Generation " + roman(option.id()) : option.label();
    }

    public String generationLabel(PokemonSpeciesInfo species) {
        if (species == null) {
            return "";
        }
        return isEnglish() ? "Generation " + roman(species.generationId()) : species.generationLabel();
    }

    public String regionalDisplayModeLabel(RegionalDisplayMode mode) {
        if (mode == null) {
            return "";
        }
        return switch (mode) {
            case INCLUDE -> ui("include");
            case EXCLUDE -> ui("exclude");
            case ONLY -> ui("only");
        };
    }

    public String ui(String key) {
        return switch (key) {
            case "app_title" -> text("Pokemon TCG Collection", "Pokemon TCG Collection");
            case "home" -> text("Accueil", "Home");
            case "dashboard" -> text("Dashboard", "Dashboard");
            case "my_dashboard" -> text("Mon Dashboard", "My Dashboard");
            case "browse" -> text("Parcourir", "Browse");
            case "sets" -> text("Ensembles", "Sets");
            case "set_tracker" -> text("Suivi des sets", "Set Tracker");
            case "browse_cards" -> text("Parcourir les cartes", "Browse Cards");
            case "browse_cards_description" -> text("Decouvrir de nouvelles cartes", "Discover new cards");
            case "browse_cards_action" -> text("Voir les cartes", "View Cards");
            case "browse_cards_search_placeholder" -> text(
                    "Rechercher une carte en francais ou en anglais...",
                    "Search a card in French or English...");
            case "browse_sets" -> text("Voir les sets", "View Sets");
            case "browse_no_cards" -> text("Aucune carte trouvee pour ce filtre.", "No card matches this filter.");
            case "search" -> text("Recherche", "Search");
            case "quick_search" -> text("Recherche rapide", "Quick Search");
            case "dynamic_search" -> text("Recherche dynamique", "Live Search");
            case "filter" -> text("Filtrer", "Filter");
            case "filters" -> text("Filtres", "Filters");
            case "apply" -> text("Appliquer", "Apply");
            case "reset" -> text("Reinitialiser", "Reset");
            case "set" -> text("Set", "Set");
            case "all_sets" -> text("Tous les sets", "All sets");
            case "rarity" -> text("Raret\u00E9", "Rarity");
            case "all_rarities" -> text("Toutes les raret\u00E9s", "All rarities");
            case "form" -> text("Forme", "Form");
            case "all_forms" -> text("Toutes les formes", "All forms");
            case "pokemon" -> text("Pok\u00E9mon", "Pokemon");
            case "all_pokemon" -> text("Tous les Pok\u00E9mon", "All Pokemon");
            case "pokedex" -> text("Pokedex", "Pokedex");
            case "pokedex_description" -> text(
                    "Retrouvez la liste complete des Pok\u00E9mon et choisissez la carte de votre collection a afficher pour chacun.",
                    "Browse the full Pokemon list and choose which card from your collection is displayed for each one.");
            case "back_to_pokedex" -> text("Retour au pokedex", "Back to Pokedex");
            case "back_pokedex" -> text("Retour Pokedex", "Back to Pokedex");
            case "quick_pokedex_nav" -> text("Navigation rapide sur le Pokedex", "Quick Pokedex Navigation");
            case "view_details" -> text("Ouvrir la fiche", "Open details");
            case "view_pokepedia" -> text("Voir sur Pokepedia", "View on Pokepedia");
            case "assigned_only" -> text("Assignes seulement", "Assigned only");
            case "unassigned_only" -> text("Non assignes seulement", "Unassigned only");
            case "unassigned_count_label" -> text("non assignes", "unassigned");
            case "missing_cards_only" -> text("Pas de cartes disponibles", "No cards available");
            case "no_match_filter" -> text("Aucun Pok\u00E9mon ne correspond a ce filtre.", "No Pokemon matches this filter.");
            case "comment" -> text("Commentaire", "Comment");
            case "add" -> text("Ajouter", "Add");
            case "remove" -> text("Retirer", "Remove");
            case "delete" -> text("Supprimer", "Delete");
            case "assign" -> text("Assigner", "Assign");
            case "save_comment" -> text("Enregistrer le commentaire", "Save comment");
            case "add_comment_placeholder" -> text("Ajouter un commentaire sur cette affectation...", "Add a comment for this assignment...");
            case "assigned_card" -> text("Carte assignee", "Assigned card");
            case "displayed_card_pokedex" -> text("Carte affichee dans le Pokedex", "Card displayed in the Pokedex");
            case "no_assigned_card" -> text("Aucune carte assignee", "No assigned card");
            case "no_assigned_card_long" -> text("Aucune carte n'est encore assignee a ce Pokemon.", "No card has been assigned to this Pokemon yet.");
            case "no_matching_card" -> text("Aucune carte correspondante", "No matching card");
            case "marked_no_card" -> text("Ce Pokemon est marque comme sans carte disponible.", "This Pokemon is marked as having no available card.");
            case "owned_cards_collection" -> text("Cartes deja dans ma collection", "Cards already in my collection");
            case "available_cards_pokemon" -> text("Cartes TCG disponibles pour ce Pokemon", "TCG cards available for this Pokemon");
            case "all_cards" -> text("Toutes les cartes", "All cards");
            case "no_cards_yet_collection" -> text(
                    "Vous n'avez pas encore de carte de ce Pokemon dans votre collection.",
                    "You do not have any cards for this Pokemon in your collection yet.");
            case "no_cards_found_pokemon" -> text("Aucune carte n'a ete remontee pour ce Pokemon.", "No cards were found for this Pokemon.");
            case "card_singular" -> text("carte", "card");
            case "cards_plural" -> text("cartes", "cards");
            case "displayed_count" -> text("carte(s) affichee(s)", "card(s) shown");
            case "options" -> text("option(s)", "option(s)");
            case "previous" -> text("Precedent", "Previous");
            case "next" -> text("Suivant", "Next");
            case "page" -> text("Page", "Page");
            case "error" -> text("Erreur", "Error");
            case "error_prefix" -> text("Erreur : ", "Error: ");
            case "error_add_collection" -> text("Erreur lors de l'ajout a la collection", "Error while adding to collection");
            case "error_remove_collection" -> text("Erreur lors de la suppression de la collection", "Error while removing from collection");
            case "error_remove" -> text("Erreur lors de la suppression", "Error while removing");
            case "error_assign" -> text("Erreur lors de l'assignation", "Error while assigning");
            case "error_clear_assignment" -> text("Erreur lors de la suppression de l'assignation", "Error while clearing assignment");
            case "error_missing_status" -> text("Erreur lors de la mise a jour du statut de carte", "Error while updating card status");
            case "error_save_comment" -> text("Erreur lors de l'enregistrement du commentaire", "Error while saving comment");
            case "include" -> text("Inclure", "Include");
            case "exclude" -> text("Exclure", "Exclude");
            case "only" -> text("Seulement", "Only");
            case "alternative_forms" -> text("Formes alternatives", "Alternate forms");
            case "megas_gigamax" -> text("Megas et Gigamax", "Megas and Gigantamax");
            case "generation" -> text("Generation", "Generation");
            case "all_generations" -> text("Toutes les generations", "All generations");
            case "collection" -> text("Ma Collection", "My Collection");
            case "empty_collection" -> text("Votre collection est vide", "Your collection is empty");
            case "add_cards" -> text("Ajouter des cartes", "Add cards");
            case "all_my_cards" -> text("Toutes mes cartes", "All my cards");
            case "collection_filters" -> text("Filtres de collection", "Collection filters");
            case "collection_of" -> text("Collection de ", "Collection of ");
            case "different_pokemon" -> text("Pokemons differents", "Different Pokemon");
            case "completed_sets" -> text("Sets complets", "Completed sets");
            case "total_cards" -> text("Cartes totales", "Total cards");
            case "search_help_collection" -> text(
                    "Recherche rapide et filtres compacts pour retrouver une carte sans prendre toute la page.",
                    "Compact search and filters to find a card quickly without taking over the page.");
            case "login" -> text("Connexion", "Login");
            case "login_go" -> text("Aller a la connexion", "Go to login");
            case "register" -> text("Inscription", "Register");
            case "create_account" -> text("Creer un compte", "Create an account");
            case "go_dashboard" -> text("Ouvrir le dashboard", "Open dashboard");
            case "open_collection" -> text("Ouvrir ma collection", "Open my collection");
            case "open_pokedex" -> text("Ouvrir le pokedex", "Open Pokedex");
            case "available_series" -> text("Series disponibles", "Available series");
            case "choose_action" -> text(
                    "Choisissez rapidement ce que vous voulez faire dans l'application.",
                    "Quickly choose what you want to do in the application.");
            case "quick_access_personal" -> text("Accedez rapidement a votre espace personnel.", "Quickly access your personal space.");
            case "already_connected_area" -> text("Retrouvez directement votre espace deja connecte.", "Jump straight back into your signed-in area.");
            case "sets_list" -> text("Liste des sets", "Set list");
            case "all_existing_sets" -> text("Consultez tous les ensembles existants.", "Browse every existing set.");
            case "full_card_list" -> text("Liste complete des cartes", "Full card list");
            case "browse_available_cards" -> text("Parcourez l'ensemble des cartes disponibles.", "Browse all available cards.");
            case "my_collection_desc" -> text("Retrouvez vos cartes ajoutees et votre progression.", "See your added cards and progress.");
            case "pokedex_desc" -> text("Associez une carte possedee a chaque Pokemon de la liste complete.", "Assign one owned card to each Pokemon from the full list.");
            case "welcome" -> text("Bienvenue, ", "Welcome, ");
            case "logout" -> text("Deconnexion", "Logout");
            case "scroll_top" -> text("Retour en haut", "Back to top");
            case "zoom_card" -> text("Zoom carte", "Card zoom");
            case "close" -> text("Fermer", "Close");
            case "previous_card" -> text("Carte precedente", "Previous card");
            case "next_card" -> text("Carte suivante", "Next card");
            case "card_label" -> text("Carte", "Card");
            case "local_number" -> text("Numero local", "Local number");
            case "price_cardmarket" -> text("Prix Cardmarket", "Cardmarket price");
            case "price_tcgplayer" -> text("Prix TCGplayer", "TCGplayer price");
            case "quantity" -> text("Quantite", "Quantity");
            case "updating" -> text("Mise a jour en cours...", "Updating...");
            case "action_saved" -> text("Action enregistree.", "Action saved.");
            case "unexpected_error" -> text("Une erreur est survenue.", "An error occurred.");
            case "add_to_collection" -> text("Ajouter a la collection", "Add to collection");
            case "remove_from_collection" -> text("Retirer de la collection", "Remove from collection");
            case "assign_in_pokedex" -> text("Assigner dans le Pokedex", "Assign in the Pokedex");
            case "clear_assignment" -> text("Retirer l'assignation", "Clear assignment");
            case "owned_copies_status" -> text("Vous possedez deja __COUNT__ exemplaire(s) de cette carte.", "You already own __COUNT__ copy/copies of this card.");
            case "owned_status" -> text("Possedee", "Owned");
            case "missing_status" -> text("Manquante", "Missing");
            case "my_collection_button" -> text("Voir ma collection", "View my collection");
            case "set_explore" -> text("Explorer tous les sets", "Browse all sets");
            case "quick_actions" -> text("Actions rapides", "Quick actions");
            case "manage_collection" -> text("Gerer ma collection", "Manage my collection");
            case "manage_collection_desc" -> text("Organisez vos cartes par Pokemon et consultez vos statistiques.", "Organize your cards by Pokemon and review your stats.");
            case "pokemon_cards_title" -> text("Cartes ", "Cards ");
            case "all_cards_for" -> text("Toutes les cartes ", "All cards for ");
            case "in_your_collection" -> text("dans votre collection", "in your collection");
            case "no_card_found" -> text("Aucune carte trouvee", "No card found");
            case "browse_to_add_cards" -> text("Chercher des cartes", "Find cards");
            case "set_filters_help" -> text(
                    "Recherche sur les ids et noms EN/FR/JP/KR/ZH, puis combine les langues de disponibilite et les familles de sets.",
                    "Search ids and EN/FR/JP/KR/ZH names, then combine availability languages and set families.");
            case "active_filter" -> text("Filtre actif :", "Active filter:");
            case "view_all_sets" -> text("Voir tous les sets", "View all sets");
            case "no_set_for_series" -> text("Aucun set trouve pour cette serie.", "No set found for this series.");
            case "no_set_for_filters" -> text("Aucun set ne correspond aux filtres actuels.", "No set matches the current filters.");
            case "availability" -> text("Disponibilite", "Availability");
            case "languages_count" -> text("langue(s)", "language(s)");
            case "no_visual_asset" -> text("Aucun visuel TCGdex", "No TCGdex visual");
            case "translation_missing" -> text("Pas de traduction EN/FR dans TCGdex", "No EN/FR translation in TCGdex");
            case "mcdo_only" -> text("MCDO", "MCDO");
            case "exclusive_only" -> text("Exclus hors EN/FR", "Exclusive outside EN/FR");
            case "promo_only" -> text("Promos", "Promos");
            case "pocket_only" -> text("Pokemon TCG Pocket", "Pokemon TCG Pocket");
            case "tracked_sets" -> text("Sets suivis", "Tracked sets");
            case "validated_cards" -> text("Cartes validees", "Validated cards");
            case "add_set_to_tracker" -> text("Ajouter un set au suivi", "Add a set to tracker");
            case "select_set" -> text("Selectionnez un set...", "Select a set...");
            case "add_to_tracker" -> text("Ajouter au suivi", "Add to tracker");
            case "no_tracked_sets" -> text(
                    "Aucun set suivi pour le moment. Ajoutez-en un pour commencer votre checklist.",
                    "No tracked sets yet. Add one to start your checklist.");
            case "complete" -> text("Complet", "Complete");
            case "in_progress" -> text("En cours", "In progress");
            case "owned_plural" -> text("Possedees", "Owned");
            case "missing_plural" -> text("Manquantes", "Missing");
            case "open_checklist" -> text("Ouvrir la checklist", "Open checklist");
            case "remove_from_tracker" -> text("Retirer du suivi", "Remove from tracker");
            case "back_to_set_tracker" -> text("Retour au suivi des sets", "Back to set tracker");
            case "display_mode" -> text("Affichage", "Display");
            case "owned_only" -> text("Possedees seulement", "Owned only");
            case "missing_only" -> text("Manquantes seulement", "Missing only");
            default -> key;
        };
    }

    private String secondary(String primary, String alternate) {
        String normalizedPrimary = coalesce(primary);
        String normalizedAlternate = coalesce(alternate);
        if (normalizedAlternate.isBlank() || normalizedAlternate.equalsIgnoreCase(normalizedPrimary)) {
            return null;
        }
        return normalizedAlternate;
    }

    private String coalesce(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String resolveFrenchPokemonName(PokemonSpeciesInfo species) {
        if (species.isRegionalForm() && species.getBaseFrenchName() != null && !species.getBaseFrenchName().isBlank()) {
            return species.getBaseFrenchName() + " " + species.regionalForm().frenchSuffix();
        }
        return species.frenchName();
    }

    private String resolveEnglishPokemonName(PokemonSpeciesInfo species) {
        if (species.isRegionalForm()) {
            return capitalize(species.regionalForm().englishPrefix()) + " " + species.getBaseEnglishName();
        }
        if (species.isAlternativeForm() && species.alternativeForm().replacesBaseEntry() && !species.alternativeForm().isMegaOrGigantamaxForm()) {
            PokemonAlternativeForm form = species.alternativeForm();
            return form.englishName();
        }
        return species.getSearchableEnglishName();
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private String roman(int value) {
        return switch (value) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            default -> String.valueOf(value);
        };
    }
}
