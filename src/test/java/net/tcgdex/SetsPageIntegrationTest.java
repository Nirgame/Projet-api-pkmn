package net.tcgdex;

import net.tcgdex.model.Serie;
import net.tcgdex.model.Set;
import net.tcgdex.service.TCGdexService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SetsPageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TCGdexService tcgdexService;

    @Test
    void setsPageShouldFilterBySerieId() throws Exception {
        Serie baseSerie = new Serie("base", "Base", null);
        Serie neoSerie = new Serie("neo", "Neo", null);

        Set baseSet = new Set();
        baseSet.setId("base1");
        baseSet.setName("Base Set");
        baseSet.setEnglishName("Base Set");
        baseSet.setFrenchName("Set de Base");
        baseSet.setLogo("https://assets.tcgdex.net/en/base/base1/logo");
        baseSet.setSymbol("https://assets.tcgdex.net/univ/base/base1/symbol");
        baseSet.setTotal("102");
        baseSet.setSerie(baseSerie);
        baseSet.setAvailableLanguages(List.of("en", "fr"));

        Set neoSet = new Set();
        neoSet.setId("neo1");
        neoSet.setName("Neo Genesis");
        neoSet.setEnglishName("Neo Genesis");
        neoSet.setFrenchName("Neo Genesis");
        neoSet.setTotal("111");
        neoSet.setSerie(neoSerie);
        neoSet.setAvailableLanguages(List.of("en"));

        Serie selectedSerie = new Serie("base", "Base", "https://assets.tcgdex.net/en/base/base1/logo");
        selectedSerie.setSets(List.of(baseSet));

        when(tcgdexService.getSetsBySerie("base")).thenReturn(List.of(baseSet));
        when(tcgdexService.getSerie("base")).thenReturn(selectedSerie);

        mockMvc.perform(get("/sets").param("serieId", "base"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Filtre actif")))
                .andExpect(content().string(containsString("Base Set")))
                .andExpect(content().string(containsString("Set de Base")))
                .andExpect(content().string(containsString("https://assets.tcgdex.net/en/base/base1/logo.webp")))
                .andExpect(content().string(containsString("EN + FR")))
                .andExpect(content().string(containsString("Voir tous les sets")));
    }

    @Test
    void setsPageShouldSupportLanguageAndExclusiveFilters() throws Exception {
        Set englishSet = new Set();
        englishSet.setId("base1");
        englishSet.setName("Base Set");
        englishSet.setEnglishName("Base Set");
        englishSet.setFrenchName("Set de Base");
        englishSet.setAvailableLanguages(List.of("en", "fr"));

        Set japaneseExclusiveSet = new Set();
        japaneseExclusiveSet.setId("PMCG1");
        japaneseExclusiveSet.setName("拡張パック");
        japaneseExclusiveSet.setAvailableLanguages(List.of("ja"));

        when(tcgdexService.getSets()).thenReturn(List.of(englishSet, japaneseExclusiveSet));

        mockMvc.perform(get("/sets")
                        .param("exclusiveOnly", "true")
                        .param("languages", "ja"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pokemon TCG Pocket")))
                .andExpect(content().string(containsString("Exclus hors EN/FR")))
                .andExpect(content().string(containsString("拡張パック")))
                .andExpect(content().string(containsString("Pas de nom EN/FR dans TCGdex")))
                .andExpect(content().string(containsString("Exclu Japon")));
    }

    @Test
    void setsPageShouldSearchAcrossLocalizedAliases() throws Exception {
        Set koreanSet = new Set();
        koreanSet.setId("sv-k1");
        koreanSet.setName("Scarlet ex");
        koreanSet.setEnglishName("Scarlet ex");
        koreanSet.setAvailableLanguages(List.of("en", "ko"));
        koreanSet.setLocalizedNames(Map.of(
                "en", "Scarlet ex",
                "ko", "스칼렛 ex"));

        when(tcgdexService.getSets()).thenReturn(List.of(koreanSet));

        mockMvc.perform(get("/sets").param("search", "스칼렛"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Scarlet ex")))
                .andExpect(content().string(containsString("스칼렛 ex")));
    }
}
