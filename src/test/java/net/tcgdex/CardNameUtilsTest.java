package net.tcgdex;

import net.tcgdex.entity.UserCard;
import net.tcgdex.model.Card;
import net.tcgdex.model.CardBrief;
import net.tcgdex.model.PokemonAlternativeForm;
import net.tcgdex.model.PokemonSpeciesInfo;
import net.tcgdex.model.RegionalForm;
import net.tcgdex.util.CardNameUtils;
import net.tcgdex.util.PokemonAlternativeForms;
import net.tcgdex.util.PokemonNameUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardNameUtilsTest {

    @Test
    void searchShouldMatchEnglishAndFrenchNames() {
        CardBrief card = new CardBrief();
        card.setName("Alolan Sandshrew");
        card.setFrenchName("Sabelette d'Alola");
        card.setId("basep-1");

        assertThat(CardNameUtils.matchesSearch(card, "sandshrew")).isTrue();
        assertThat(CardNameUtils.matchesSearch(card, "sabelette")).isTrue();
        assertThat(CardNameUtils.matchesSearch(card, "sabelette alola")).isTrue();
        assertThat(CardNameUtils.matchesSearch(card, "alolan sandshrew")).isTrue();
    }

    @Test
    void exactMatchShouldKeepBasePokemonAndVariantSeparated() {
        UserCard baseCard = new UserCard();
        baseCard.setName("Sandshrew");
        baseCard.setFrenchName("Sabelette");

        UserCard variantCard = new UserCard();
        variantCard.setName("Alolan Sandshrew");
        variantCard.setFrenchName("Sabelette d'Alola");

        assertThat(CardNameUtils.matchesExactName(baseCard, "Sabelette")).isTrue();
        assertThat(CardNameUtils.matchesExactName(baseCard, "Sabelette d'Alola")).isFalse();
        assertThat(CardNameUtils.matchesExactName(variantCard, "Sabelette")).isFalse();
        assertThat(CardNameUtils.matchesExactName(variantCard, "Sabelette d'Alola")).isTrue();
    }

    @Test
    void variantLabelShouldDetectRegionalFormsFromEnglishOrFrench() {
        assertThat(CardNameUtils.extractVariantLabel("Alolan Sandshrew", "Sabelette d'Alola"))
                .isEqualTo("Alola");
        assertThat(CardNameUtils.extractVariantLabel("Galarian Meowth", "Miaouss de Galar"))
                .isEqualTo("Galar");
    }

    @Test
    void shouldInferNamedFormsOnlyWhenApiExposesThem() {
        Card rotom = new Card();
        rotom.setEnglishName("Fan Rotom");
        rotom.setFrenchName("Motisma Helice");

        Card castform = new Card();
        castform.setEnglishName("Castform Rainy Form");
        castform.setFrenchName("Morpheo Pluie");

        assertThat(CardNameUtils.inferFormLabel(rotom)).isEqualTo("Helice");
        assertThat(CardNameUtils.inferFormLabel(castform)).isEqualTo("Pluie");
    }

    @Test
    void regionalSpeciesMatchingShouldNotMixBaseAndRegionalCards() {
        PokemonSpeciesInfo baseSpecies = new PokemonSpeciesInfo(19, 19, "rattata", "Rattata", "Rattata", 1, "Generation I", null);
        PokemonSpeciesInfo alolanSpecies = new PokemonSpeciesInfo(1_010_019, 19, "rattata", "Alolan Rattata", "Rattata d'Alola", 1, "Generation I", RegionalForm.ALOLA);

        CardBrief baseCard = new CardBrief();
        baseCard.setEnglishName("Rattata");
        baseCard.setFrenchName("Rattata");

        CardBrief alolanCard = new CardBrief();
        alolanCard.setEnglishName("Alolan Rattata");
        alolanCard.setFrenchName("Rattata d'Alola");

        assertThat(PokemonNameUtils.matchesSpecies(baseCard, baseSpecies)).isTrue();
        assertThat(PokemonNameUtils.matchesSpecies(baseCard, alolanSpecies)).isFalse();
        assertThat(PokemonNameUtils.matchesSpecies(alolanCard, baseSpecies)).isFalse();
        assertThat(PokemonNameUtils.matchesSpecies(alolanCard, alolanSpecies)).isTrue();
    }

    @Test
    void baseSpeciesShouldAlsoMatchMegaAndGigantamaxCards() {
        PokemonSpeciesInfo charizard = new PokemonSpeciesInfo(6, 6, "charizard", "Charizard", "Dracaufeu", 1, "Generation I", null);
        PokemonSpeciesInfo dragonite = new PokemonSpeciesInfo(149, 149, "dragonite", "Dragonite", "Dracolosse", 1, "Generation I", null);

        CardBrief megaCharizard = new CardBrief();
        megaCharizard.setEnglishName("Mega Charizard X");
        megaCharizard.setFrenchName("Mega-Dracaufeu X");

        CardBrief megaDragonite = new CardBrief();
        megaDragonite.setEnglishName("Mega Dragonite ex");
        megaDragonite.setFrenchName("Méga-Dracolosse-ex");

        CardBrief shortMegaDragonite = new CardBrief();
        shortMegaDragonite.setEnglishName("M-Dragonite EX");
        shortMegaDragonite.setFrenchName("M-Dracolosse-EX");

        CardBrief gigantamaxCharizard = new CardBrief();
        gigantamaxCharizard.setEnglishName("Gigantamax Charizard");
        gigantamaxCharizard.setFrenchName("Dracaufeu Gigamax");

        assertThat(PokemonNameUtils.matchesSpecies(megaCharizard, charizard)).isTrue();
        assertThat(PokemonNameUtils.matchesSpecies(megaDragonite, dragonite)).isTrue();
        assertThat(PokemonNameUtils.matchesSpecies(shortMegaDragonite, dragonite)).isTrue();
        assertThat(PokemonNameUtils.matchesSpecies(gigantamaxCharizard, charizard)).isTrue();
    }

    @Test
    void alternativeFormMatchingShouldSupportDefaultAndExplicitForms() {
        PokemonAlternativeForm lycanrocDay = new PokemonAlternativeForm(
                745, 1, "Lycanroc Day", "Lougaroc Forme Diurne", "Diurne", true, java.util.List.of("lycanroc day"));
        PokemonSpeciesInfo lycanrocDaySpecies = new PokemonSpeciesInfo(
                745, 745, "lycanroc", "Lycanroc Day", "Lougaroc Forme Diurne", 7, "Generation VII", null, lycanrocDay, "Lycanroc", "Lougaroc");

        PokemonAlternativeForm rotomFan = new PokemonAlternativeForm(
                479, 1, "Rotom Fan", "Motisma Helice", "Helice", false, java.util.List.of("fan rotom", "motisma helice"));
        PokemonSpeciesInfo rotomFanSpecies = new PokemonSpeciesInfo(
                rotomFan.toEntryId(), 479, "rotom", "Rotom Fan", "Motisma Helice", 4, "Generation IV", null, rotomFan, "Rotom", "Motisma");

        CardBrief plainLycanroc = new CardBrief();
        plainLycanroc.setEnglishName("Lycanroc");
        plainLycanroc.setFrenchName("Lougaroc");

        CardBrief nightLycanroc = new CardBrief();
        nightLycanroc.setEnglishName("Midnight Form Lycanroc");
        nightLycanroc.setFrenchName("Lougaroc Forme Nocturne");

        CardBrief fanRotom = new CardBrief();
        fanRotom.setEnglishName("Fan Rotom");
        fanRotom.setFrenchName("Motisma Helice");
        fanRotom.setFormLabel("Helice");

        assertThat(PokemonNameUtils.matchesSpecies(plainLycanroc, lycanrocDaySpecies)).isTrue();
        assertThat(PokemonNameUtils.matchesSpecies(nightLycanroc, lycanrocDaySpecies)).isFalse();
        assertThat(PokemonNameUtils.matchesSpecies(fanRotom, rotomFanSpecies)).isTrue();
    }

    @Test
    void galarianDarmanitanZenShouldBeRegisteredAndMatchedSeparately() {
        PokemonAlternativeForm galarianZen = PokemonAlternativeForms.fromEntryId(2_055_502);
        assertThat(galarianZen).isNotNull();
        assertThat(galarianZen.regionalForm()).isEqualTo(RegionalForm.GALAR);

        PokemonSpeciesInfo galarianZenSpecies = new PokemonSpeciesInfo(
                galarianZen.toEntryId(),
                555,
                "darmanitan",
                "Galarian Darmanitan Zen",
                "Darumacho de Galar Mode Transe",
                5,
                "Generation V",
                RegionalForm.GALAR,
                galarianZen,
                "Darmanitan",
                "Darumacho");

        CardBrief galarianZenCard = new CardBrief();
        galarianZenCard.setEnglishName("Galarian Darmanitan Zen");
        galarianZenCard.setFrenchName("Darumacho de Galar Mode Transe");

        CardBrief regularZenCard = new CardBrief();
        regularZenCard.setEnglishName("Darmanitan Zen");
        regularZenCard.setFrenchName("Darumacho Mode Transe");

        assertThat(PokemonNameUtils.matchesSpecies(galarianZenCard, galarianZenSpecies)).isTrue();
        assertThat(PokemonNameUtils.matchesSpecies(regularZenCard, galarianZenSpecies)).isFalse();
    }

    @Test
    void shortPokemonNamesShouldNotMatchInsideLongerNames() {
        PokemonSpeciesInfo aboSpecies = new PokemonSpeciesInfo(23, 23, "ekans", "Ekans", "Abo", 1, "Generation I", null);
        PokemonSpeciesInfo rattataSpecies = new PokemonSpeciesInfo(19, 19, "rattata", "Rattata", "Rattata", 1, "Generation I", null);

        CardBrief crabominable = new CardBrief();
        crabominable.setEnglishName("Crabominable");
        crabominable.setFrenchName("Crabominable");

        CardBrief pumpkaboo = new CardBrief();
        pumpkaboo.setEnglishName("Pumpkaboo");
        pumpkaboo.setFrenchName("Pitrouille");

        CardBrief rattatac = new CardBrief();
        rattatac.setEnglishName("Raticate");
        rattatac.setFrenchName("Rattatac");

        assertThat(PokemonNameUtils.matchesSpecies(crabominable, aboSpecies)).isFalse();
        assertThat(PokemonNameUtils.matchesSpecies(pumpkaboo, aboSpecies)).isFalse();
        assertThat(PokemonNameUtils.matchesSpecies(rattatac, rattataSpecies)).isFalse();
    }

    @Test
    void customFormsShouldMatchCommonCardNames() {
        PokemonAlternativeForm toxtricityLow = new PokemonAlternativeForm(
                849, 2, "Toxtricity Low", "Salarsen Forme Grave", "Grave", false,
                java.util.List.of("toxtricity low", "low key toxtricity", "salarsen forme grave"));
        PokemonSpeciesInfo toxtricityLowSpecies = new PokemonSpeciesInfo(
                toxtricityLow.toEntryId(), 849, "toxtricity", "Toxtricity Low", "Salarsen Forme Grave",
                8, "Generation VIII", null, toxtricityLow, "Toxtricity", "Salarsen");

        PokemonAlternativeForm darmanitanZen = new PokemonAlternativeForm(
                555, 1, "Darmanitan Zen", "Darumacho Mode Transe", "Transe", false,
                java.util.List.of("darmanitan zen", "zen mode darmanitan", "darumacho mode transe"));
        PokemonSpeciesInfo darmanitanZenSpecies = new PokemonSpeciesInfo(
                darmanitanZen.toEntryId(), 555, "darmanitan", "Darmanitan Zen", "Darumacho Mode Transe",
                5, "Generation V", null, darmanitanZen, "Darmanitan", "Darumacho");

        CardBrief lowKeyCard = new CardBrief();
        lowKeyCard.setEnglishName("Low Key Toxtricity");
        lowKeyCard.setFrenchName("Salarsen Forme Grave");

        CardBrief zenCard = new CardBrief();
        zenCard.setEnglishName("Zen Mode Darmanitan");
        zenCard.setFrenchName("Darumacho Mode Transe");

        assertThat(PokemonNameUtils.matchesSpecies(lowKeyCard, toxtricityLowSpecies)).isTrue();
        assertThat(PokemonNameUtils.matchesSpecies(zenCard, darmanitanZenSpecies)).isTrue();
    }

    @Test
    void intrinsicallyRegionalEvolutionsShouldKeepTheirRegionalCards() {
        PokemonSpeciesInfo obstagoon = new PokemonSpeciesInfo(862, 862, "obstagoon", "Obstagoon", "Ixon", 8, "Generation VIII", null);
        PokemonSpeciesInfo clodsire = new PokemonSpeciesInfo(980, 980, "clodsire", "Clodsire", "Terraiste", 9, "Generation IX", null);

        CardBrief galarianObstagoon = new CardBrief();
        galarianObstagoon.setEnglishName("Galarian Obstagoon");
        galarianObstagoon.setFrenchName("Ixon de Galar");

        CardBrief paldeanClodsire = new CardBrief();
        paldeanClodsire.setEnglishName("Paldean Clodsire");
        paldeanClodsire.setFrenchName("Terraiste de Paldea");

        assertThat(PokemonNameUtils.matchesSpecies(galarianObstagoon, obstagoon)).isTrue();
        assertThat(PokemonNameUtils.matchesSpecies(paldeanClodsire, clodsire)).isTrue();
    }

    @Test
    void customFormsShouldFallbackToBaseSpeciesCardsWhenFormIsNotExplicit() {
        PokemonAlternativeForm landorusTotem = new PokemonAlternativeForm(
                645, 1, "Landorus Totem", "Demeteros Forme Totemique", "Totemique", false,
                java.util.List.of("landorus totem", "therian forme landorus", "demeteros forme totemique"));
        PokemonSpeciesInfo landorusTotemSpecies = new PokemonSpeciesInfo(
                landorusTotem.toEntryId(), 645, "landorus", "Landorus Totem", "Demeteros Forme Totemique",
                5, "Generation V", null, landorusTotem, "Landorus", "Demeteros");

        CardBrief plainLandorus = new CardBrief();
        plainLandorus.setEnglishName("Landorus");
        plainLandorus.setFrenchName("Demeteros");

        assertThat(PokemonNameUtils.matchesSpecies(plainLandorus, landorusTotemSpecies)).isFalse();
        assertThat(PokemonNameUtils.matchesBaseSpecies(plainLandorus, landorusTotemSpecies)).isTrue();
    }

    @Test
    void paldeanTaurosFireAndWaterShouldBeRegisteredAsAlternativeForms() {
        PokemonAlternativeForm paldeanTaurosFire = PokemonAlternativeForms.fromEntryId(2_012_801);
        PokemonAlternativeForm paldeanTaurosWater = PokemonAlternativeForms.fromEntryId(2_012_802);

        assertThat(paldeanTaurosFire).isNotNull();
        assertThat(paldeanTaurosFire.regionalForm()).isEqualTo(RegionalForm.PALDEA);
        assertThat(paldeanTaurosFire.frenchName()).contains("Flamboyante");

        assertThat(paldeanTaurosWater).isNotNull();
        assertThat(paldeanTaurosWater.regionalForm()).isEqualTo(RegionalForm.PALDEA);
        assertThat(paldeanTaurosWater.frenchName()).contains("Aquatique");
    }

    @Test
    void recentlyAddedMegaFormsShouldBeRegistered() {
        PokemonAlternativeForm megaDragonite = PokemonAlternativeForms.fromEntryId(2_014_901);
        PokemonAlternativeForm megaGreninja = PokemonAlternativeForms.fromEntryId(2_065_801);
        PokemonAlternativeForm megaZygarde = PokemonAlternativeForms.fromEntryId(2_071_811);
        PokemonAlternativeForm megaRaichuX = PokemonAlternativeForms.fromEntryId(2_002_601);
        PokemonAlternativeForm megaAbsolZ = PokemonAlternativeForms.fromEntryId(2_035_902);
        PokemonAlternativeForm megaLucarioZ = PokemonAlternativeForms.fromEntryId(2_044_802);
        PokemonAlternativeForm megaSarmurai = PokemonAlternativeForms.fromEntryId(2_076_801);
        PokemonAlternativeForm megaFalinks = PokemonAlternativeForms.fromEntryId(2_087_001);

        assertThat(megaDragonite).isNotNull();
        assertThat(megaDragonite.megaForm()).isTrue();
        assertThat(megaDragonite.frenchName()).contains("Dracolosse");

        assertThat(megaGreninja).isNotNull();
        assertThat(megaGreninja.megaForm()).isTrue();
        assertThat(megaGreninja.englishName()).isEqualTo("Mega Greninja");

        assertThat(megaZygarde).isNotNull();
        assertThat(megaZygarde.megaForm()).isTrue();
        assertThat(megaZygarde.frenchName()).contains("Zygarde");

        assertThat(megaRaichuX).isNotNull();
        assertThat(megaRaichuX.formLabel()).isEqualTo("Mega X");

        assertThat(megaAbsolZ).isNotNull();
        assertThat(megaAbsolZ.englishName()).isEqualTo("Mega Absol Z");

        assertThat(megaLucarioZ).isNotNull();
        assertThat(megaLucarioZ.formLabel()).isEqualTo("Mega Z");

        assertThat(megaSarmurai).isNotNull();
        assertThat(megaSarmurai.englishName()).isEqualTo("Mega Golisopod");
        assertThat(megaSarmurai.frenchName()).contains("Sarmura");

        assertThat(megaFalinks).isNotNull();
        assertThat(megaFalinks.frenchName()).contains("Hexadron");
    }
}
