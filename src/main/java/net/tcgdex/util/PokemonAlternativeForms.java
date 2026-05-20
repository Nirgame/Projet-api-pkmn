package net.tcgdex.util;

import net.tcgdex.model.PokemonAlternativeForm;
import net.tcgdex.model.RegionalForm;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PokemonAlternativeForms {

    private static final Map<Integer, List<PokemonAlternativeForm>> FORMS_BY_SPECIES = buildForms();
    private static final Map<Integer, String> BASE_ENGLISH_NAMES = buildBaseEnglishNames();
    private static final Map<Integer, String> BASE_FRENCH_NAMES = buildBaseFrenchNames();

    private PokemonAlternativeForms() {
    }

    public static List<PokemonAlternativeForm> getFormsForSpecies(int speciesId) {
        return FORMS_BY_SPECIES.getOrDefault(speciesId, List.of());
    }

    public static PokemonAlternativeForm getBaseReplacementForm(int speciesId) {
        return getFormsForSpecies(speciesId).stream()
                .filter(PokemonAlternativeForm::replacesBaseEntry)
                .findFirst()
                .orElse(null);
    }

    public static List<PokemonAlternativeForm> getAdditionalForms(int speciesId) {
        return getFormsForSpecies(speciesId).stream()
                .filter(form -> !form.replacesBaseEntry())
                .toList();
    }

    public static PokemonAlternativeForm fromEntryId(int entryId) {
        if (!PokemonAlternativeForm.isAlternativeEntryId(entryId)) {
            return null;
        }

        int speciesId = PokemonAlternativeForm.extractSpeciesId(entryId);
        int localCode = PokemonAlternativeForm.extractLocalCode(entryId);
        return getFormsForSpecies(speciesId).stream()
                .filter(form -> form.localCode() == localCode)
                .findFirst()
                .orElse(null);
    }

    public static String extractFormLabel(String englishName, String frenchName) {
        String haystack = buildHaystack(englishName, frenchName, null, null);
        for (List<PokemonAlternativeForm> forms : FORMS_BY_SPECIES.values()) {
            for (PokemonAlternativeForm form : forms) {
                if (matchesAnyAlias(haystack, form)) {
                    return form.formLabel();
                }
            }
        }
        return null;
    }

    public static boolean matchesForm(String englishName,
            String frenchName,
            String storedFormLabel,
            String derivedVariantLabel,
            PokemonAlternativeForm form) {
        return matchesAnyAlias(buildHaystack(englishName, frenchName, storedFormLabel, derivedVariantLabel), form);
    }

    public static boolean hasAnyMarker(String englishName,
            String frenchName,
            String storedFormLabel,
            String derivedVariantLabel,
            int speciesId) {
        String haystack = buildHaystack(englishName, frenchName, storedFormLabel, derivedVariantLabel);
        for (PokemonAlternativeForm form : getFormsForSpecies(speciesId)) {
            if (matchesAnyAlias(haystack, form)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasOtherMarker(String englishName,
            String frenchName,
            String storedFormLabel,
            String derivedVariantLabel,
            PokemonAlternativeForm expectedForm) {
        String haystack = buildHaystack(englishName, frenchName, storedFormLabel, derivedVariantLabel);
        for (PokemonAlternativeForm form : getFormsForSpecies(expectedForm.speciesId())) {
            if (form.equals(expectedForm)) {
                continue;
            }
            if (matchesAnyAlias(haystack, form)) {
                return true;
            }
        }
        return false;
    }

    public static String resolveBaseEnglishName(String englishName, String frenchName) {
        PokemonAlternativeForm form = findMatchingForm(englishName, frenchName, null, null);
        if (form == null) {
            return null;
        }
        return BASE_ENGLISH_NAMES.get(form.speciesId());
    }

    public static String resolveBaseFrenchName(String englishName, String frenchName) {
        PokemonAlternativeForm form = findMatchingForm(englishName, frenchName, null, null);
        if (form == null) {
            return null;
        }
        return BASE_FRENCH_NAMES.get(form.speciesId());
    }

    private static PokemonAlternativeForm findMatchingForm(String englishName,
            String frenchName,
            String storedFormLabel,
            String derivedVariantLabel) {
        String haystack = buildHaystack(englishName, frenchName, storedFormLabel, derivedVariantLabel);
        for (List<PokemonAlternativeForm> forms : FORMS_BY_SPECIES.values()) {
            for (PokemonAlternativeForm form : forms) {
                if (matchesAnyAlias(haystack, form)) {
                    return form;
                }
            }
        }
        return null;
    }

    private static boolean matchesAnyAlias(String haystack, PokemonAlternativeForm form) {
        return form.aliases().stream()
                .map(CardNameUtils::normalizeForSearch)
                .filter(alias -> !alias.isBlank())
                .anyMatch(haystack::contains);
    }

    private static String buildHaystack(String englishName,
            String frenchName,
            String storedFormLabel,
            String derivedVariantLabel) {
        return String.join(" ",
                CardNameUtils.normalizeForSearch(englishName),
                CardNameUtils.normalizeForSearch(frenchName),
                CardNameUtils.normalizeForSearch(storedFormLabel),
                CardNameUtils.normalizeForSearch(derivedVariantLabel));
    }

    private static Map<Integer, List<PokemonAlternativeForm>> buildForms() {
        Map<Integer, List<PokemonAlternativeForm>> forms = new LinkedHashMap<>();

        forms.put(351, List.of(
                form(351, 1, "Castform Rain", "Morpheo Pluie", "Pluie",
                        "castform rain", "rain castform", "rainy form castform", "morpheo pluie"),
                form(351, 2, "Castform Sun", "Morpheo Soleil", "Soleil",
                        "castform sun", "sun castform", "sunny form castform", "morpheo soleil"),
                form(351, 3, "Castform Snow", "Morpheo Neige", "Neige",
                        "castform snow", "castform ice", "snow castform", "snowy form castform", "morpheo neige")));

        forms.put(386, List.of(
                form(386, 1, "Deoxys Attack", "Deoxys Forme Attaque", "Attaque",
                        "deoxys attack", "deoxys attack forme", "attack forme deoxys", "deoxys forme attaque"),
                form(386, 2, "Deoxys Speed", "Deoxys Forme Vitesse", "Vitesse",
                        "deoxys speed", "deoxys speed forme", "speed forme deoxys", "deoxys forme vitesse"),
                form(386, 3, "Deoxys Defense", "Deoxys Forme Defense", "Defense",
                        "deoxys defense", "deoxys defense forme", "defense forme deoxys", "deoxys forme defense")));

        forms.put(413, List.of(
                defaultForm(413, 1, "Wormadam Leaf", "Cheniselle Cape Plante", "Cape Plante",
                        "wormadam leaf", "plant cloak wormadam", "cheniselle cape plante"),
                form(413, 2, "Wormadam Sand", "Cheniselle Cape Sable", "Cape Sable",
                        "wormadam sand", "sandy cloak wormadam", "cheniselle cape sable"),
                form(413, 3, "Wormadam Waste", "Cheniselle Cape Dechet", "Cape Dechet",
                        "wormadam waste", "trash cloak wormadam", "cheniselle cape dechet")));

        forms.put(479, List.of(
                form(479, 1, "Rotom Fan", "Motisma Helice", "Helice",
                        "rotom fan", "fan rotom", "motisma helice"),
                form(479, 2, "Rotom Oven", "Motisma Chaleur", "Chaleur",
                        "rotom oven", "rotom hoven", "heat rotom", "motisma chaleur"),
                form(479, 3, "Rotom Fridge", "Motisma Gel", "Gel",
                        "rotom fridge", "frost rotom", "motisma gel"),
                form(479, 4, "Rotom Mower", "Motisma Tonte", "Tonte",
                        "rotom mower", "rotom mawer", "mow rotom", "motisma tonte"),
                form(479, 5, "Rotom Wash", "Motisma Lavage", "Lavage",
                        "rotom wash", "wash rotom", "motisma lavage")));

        forms.put(487, List.of(
                form(487, 1, "Giratina Origin", "Giratina Forme Origine", "Origine",
                        "giratina origin", "origin forme giratina", "giratina forme origine")));

        forms.put(492, List.of(
                form(492, 1, "Shaymin Sky", "Shaymin Forme Ciel", "Ciel",
                        "shaymin sky", "sky forme shaymin", "shaymin forme ciel")));

        forms.put(555, List.of(
                form(555, 1, "Darmanitan Zen", "Darumacho Mode Transe", "Transe",
                        "darmanitan zen", "zen darmanitan", "zen mode darmanitan", "darumacho mode transe", "darumacho transe"),
                regionalForm(555, 2, RegionalForm.GALAR, "Galarian Darmanitan Zen", "Darumacho de Galar Mode Transe", "Transe de Galar",
                        "galarian darmanitan zen", "galarian zen darmanitan", "galarian darmanitan zen mode", "galarian zen mode darmanitan",
                        "darumacho de galar mode transe", "darumacho de galar transe")));

        forms.put(641, List.of(
                form(641, 1, "Tornadus Totem", "Boreas Forme Totemique", "Totemique",
                        "tornadus totem", "therian forme tornadus", "boreas forme totemique")));

        forms.put(642, List.of(
                form(642, 1, "Thundurus Totem", "Fulguris Forme Totemique", "Totemique",
                        "thundurus totem", "therian forme thundurus", "fulguris forme totemique")));

        forms.put(645, List.of(
                form(645, 1, "Landorus Totem", "Demeteros Forme Totemique", "Totemique",
                        "landorus totem", "therian forme landorus", "demeteros forme totemique")));

        forms.put(646, List.of(
                form(646, 1, "Kyurem White", "Kyurem Blanc", "Blanc",
                        "kyurem white", "white kyurem", "kyurem blanc"),
                form(646, 2, "Kyurem Black", "Kyurem Noir", "Noir",
                        "kyurem black", "black kyurem", "kyurem noir")));

        forms.put(648, List.of(
                form(648, 1, "Meloetta Pirouette", "Meloetta Pirouette", "Pirouette",
                        "meloetta pirouette", "pirouette forme meloetta", "meloetta forme danse")));

        forms.put(718, List.of(
                form(718, 1, "Zygarde 10%", "Zygarde 10%", "10%",
                        "zygarde 10", "zygarde 10%", "zygarde 10 percent"),
                form(718, 2, "Zygarde 100%", "Zygarde 100%", "100%",
                        "zygarde 100", "zygarde 100%", "zygarde complete")));

        forms.put(720, List.of(
                form(720, 1, "Hoopa Unbound", "Hoopa Dechaine", "Dechaine",
                        "hoopa unbound", "hoopa dechaine")));

        forms.put(741, List.of(
                defaultForm(741, 1, "Oricorio Pompom", "Plumeline Style Pom-Pom", "Pom-Pom",
                        "oricorio pompom", "oricorio pom-pom", "pom-pom style oricorio", "plumeline style pom-pom"),
                form(741, 2, "Oricorio Flamenco", "Plumeline Style Flamenco", "Flamenco",
                        "oricorio flamenco", "baile style oricorio", "plumeline style flamenco"),
                form(741, 3, "Oricorio Buyo", "Plumeline Style Buyo", "Buyo",
                        "oricorio buyo", "sensu style oricorio", "plumeline style buyo"),
                form(741, 4, "Oricorio Hula", "Plumeline Style Hula", "Hula",
                        "oricorio hula", "pau style oricorio", "plumeline style hula")));

        forms.put(745, List.of(
                defaultForm(745, 1, "Lycanroc Day", "Lougaroc Forme Diurne", "Diurne",
                        "lycanroc day", "midday form lycanroc", "lougaroc forme diurne"),
                form(745, 2, "Lycanroc Night", "Lougaroc Forme Nocturne", "Nocturne",
                        "lycanroc night", "midnight form lycanroc", "lougaroc forme nocturne"),
                form(745, 3, "Lycanroc Dawn", "Lougaroc Forme Crepusculaire", "Crepusculaire",
                        "lycanroc dawn", "dusk form lycanroc", "lougaroc forme crepusculaire")));

        forms.put(746, List.of(
                form(746, 1, "Wishiwashi Bench", "Froussardine Forme Banc", "Banc",
                        "wishiwashi bench", "wishiwashi school", "froussardine forme banc", "froussardine banc")));

        forms.put(800, List.of(
                form(800, 1, "Necrozma Dawn", "Necrozma Ailes de l'Aurore", "Aurore",
                        "necrozma dawn", "dawn wings necrozma", "necrozma ailes de l aurore"),
                form(800, 2, "Necrozma Dusk", "Necrozma Criniere du Couchant", "Couchant",
                        "necrozma dusk", "dusk mane necrozma", "necrozma criniere du couchant"),
                form(800, 3, "Ultra Necrozma", "Ultra-Necrozma", "Ultra",
                        "ultra necrozma", "ultra-necrozma")));

        forms.put(849, List.of(
                defaultForm(849, 1, "Toxtricity High", "Salarsen Forme Aigue", "Aigue",
                        "toxtricity high", "amped toxtricity", "amped form toxtricity", "salarsen forme aigue", "salarsen aigue"),
                form(849, 2, "Toxtricity Low", "Salarsen Forme Grave", "Grave",
                        "toxtricity low", "low key toxtricity", "low key form toxtricity", "salarsen forme grave", "salarsen grave")));

        forms.put(875, List.of(
                form(875, 1, "Eiscue Ice Cube", "Bekaglacon Tete de Glace", "Tete de Glace",
                        "eiscue ice cube", "ice face eiscue", "bekaglacon tete de glace")));

        forms.put(877, List.of(
                form(877, 1, "Morpeko Hungry", "Morpeko Affame", "Affame",
                        "morpeko hungry", "hangry mode morpeko", "morpeko affame")));

        forms.put(892, List.of(
                defaultForm(892, 1, "Urshifu Final Blow", "Shifours Style Poing Final", "Poing Final",
                        "urshifu final blow", "single strike urshifu", "shifours style poing final"),
                form(892, 2, "Urshifu Rapid Strike", "Shifours Style Mille Poings", "Mille Poings",
                        "urshifu rapid strike", "rapid strike urshifu", "shifours style mille poings")));

        forms.put(898, List.of(
                form(898, 1, "Calyrex Ice Rider", "Sylveroy, le Cavalier du Froid", "Cavalier du Froid",
                        "calyrex ice rider", "ice rider calyrex", "sylveroy cavalier du froid"),
                form(898, 2, "Calyrex Ghost Rider", "Sylveroy, le Cavalier d'Effroi", "Cavalier d'Effroi",
                        "calyrex ghost rider", "shadow rider calyrex", "sylveroy cavalier d effroi")));

        forms.put(905, List.of(
                form(905, 1, "Enamorus Totem", "Amovenus Forme Totemique", "Totemique",
                        "enamorus totem", "therian forme enamorus", "amovenus forme totemique")));

        forms.put(483, List.of(
                form(483, 1, "Origin Dialga", "Dialga Forme Origine", "Origine",
                        "origin dialga", "dialga origin", "dialga forme origine")));

        forms.put(484, List.of(
                form(484, 1, "Origin Palkia", "Palkia Forme Origine", "Origine",
                        "origin palkia", "palkia origin", "palkia forme origine")));

        forms.put(964, List.of(
                form(964, 1, "Palafin Hero", "Superdofin Forme Super", "Forme Super",
                        "palafin hero", "hero form palafin", "superdofin forme super")));

        forms.put(901, List.of(
                form(901, 1, "Bloodmoon Ursaluna", "Ursaking Lune Vermeille", "Lune Vermeille",
                        "bloodmoon ursaluna", "ursaluna bloodmoon", "ursaking lune vermeille")));

        forms.put(1017, List.of(
                form(1017, 1, "Ogerpon Water", "Ogerpon Masque du Puits", "Masque du Puits",
                        "ogerpon water", "wellspring mask ogerpon", "ogerpon masque du puits"),
                form(1017, 2, "Ogerpon Fire", "Ogerpon Masque du Fourneau", "Masque du Fourneau",
                        "ogerpon fire", "hearthflame mask ogerpon", "ogerpon masque du fourneau"),
                form(1017, 3, "Ogerpon Rock", "Ogerpon Masque de la Pierre", "Masque de la Pierre",
                        "ogerpon rock", "cornerstone mask ogerpon", "ogerpon masque de la pierre")));

        forms.put(128, List.of(
                regionalForm(128, 1, RegionalForm.PALDEA, "Paldean Tauros Fire", "Tauros de Paldea Race Flamboyante", "Feu",
                        "paldean tauros fire", "paldean tauros blaze breed", "tauros de paldea feu",
                        "tauros de paldea race flamboyante", "tauros de paldea type feu"),
                regionalForm(128, 2, RegionalForm.PALDEA, "Paldean Tauros Water", "Tauros de Paldea Race Aquatique", "Eau",
                        "paldean tauros water", "paldean tauros aqua breed", "tauros de paldea eau",
                        "tauros de paldea race aquatique", "tauros de paldea type eau")));

        return forms;
    }

    private static Map<Integer, String> buildBaseEnglishNames() {
        Map<Integer, String> names = new LinkedHashMap<>();
        names.put(128, "Tauros");
        names.put(351, "Castform");
        names.put(386, "Deoxys");
        names.put(413, "Wormadam");
        names.put(479, "Rotom");
        names.put(483, "Dialga");
        names.put(484, "Palkia");
        names.put(487, "Giratina");
        names.put(492, "Shaymin");
        names.put(555, "Darmanitan");
        names.put(641, "Tornadus");
        names.put(642, "Thundurus");
        names.put(645, "Landorus");
        names.put(646, "Kyurem");
        names.put(648, "Meloetta");
        names.put(718, "Zygarde");
        names.put(720, "Hoopa");
        names.put(741, "Oricorio");
        names.put(745, "Lycanroc");
        names.put(746, "Wishiwashi");
        names.put(800, "Necrozma");
        names.put(849, "Toxtricity");
        names.put(875, "Eiscue");
        names.put(877, "Morpeko");
        names.put(892, "Urshifu");
        names.put(898, "Calyrex");
        names.put(901, "Ursaluna");
        names.put(905, "Enamorus");
        names.put(964, "Palafin");
        names.put(1017, "Ogerpon");
        return names;
    }

    private static Map<Integer, String> buildBaseFrenchNames() {
        Map<Integer, String> names = new LinkedHashMap<>();
        names.put(128, "Tauros");
        names.put(351, "Morpheo");
        names.put(386, "Deoxys");
        names.put(413, "Cheniselle");
        names.put(479, "Motisma");
        names.put(483, "Dialga");
        names.put(484, "Palkia");
        names.put(487, "Giratina");
        names.put(492, "Shaymin");
        names.put(555, "Darumacho");
        names.put(641, "Boreas");
        names.put(642, "Fulguris");
        names.put(645, "Demeteros");
        names.put(646, "Kyurem");
        names.put(648, "Meloetta");
        names.put(718, "Zygarde");
        names.put(720, "Hoopa");
        names.put(741, "Plumeline");
        names.put(745, "Lougaroc");
        names.put(746, "Froussardine");
        names.put(800, "Necrozma");
        names.put(849, "Salarsen");
        names.put(875, "Bekaglacon");
        names.put(877, "Morpeko");
        names.put(892, "Shifours");
        names.put(898, "Sylveroy");
        names.put(901, "Ursaking");
        names.put(905, "Amovenus");
        names.put(964, "Superdofin");
        names.put(1017, "Ogerpon");
        return names;
    }

    private static PokemonAlternativeForm form(int speciesId,
            int localCode,
            String englishName,
            String frenchName,
            String formLabel,
            String... aliases) {
        return new PokemonAlternativeForm(
                speciesId,
                localCode,
                englishName,
                frenchName,
                formLabel,
                null,
                false,
                List.of(aliases));
    }

    private static PokemonAlternativeForm defaultForm(int speciesId,
            int localCode,
            String englishName,
            String frenchName,
            String formLabel,
            String... aliases) {
        return new PokemonAlternativeForm(
                speciesId,
                localCode,
                englishName,
                frenchName,
                formLabel,
                null,
                true,
                List.of(aliases));
    }

    private static PokemonAlternativeForm regionalForm(int speciesId,
            int localCode,
            RegionalForm regionalForm,
            String englishName,
            String frenchName,
            String formLabel,
            String... aliases) {
        return new PokemonAlternativeForm(
                speciesId,
                localCode,
                englishName,
                frenchName,
                formLabel,
                regionalForm,
                false,
                List.of(aliases));
    }
}
