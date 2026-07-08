package net.tcgdex.controller;

import net.tcgdex.service.UiLanguageService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UiLanguageService uiLanguageService;

    public GlobalModelAttributes(UiLanguageService uiLanguageService) {
        this.uiLanguageService = uiLanguageService;
    }

    @ModelAttribute("uiLanguage")
    public String uiLanguage() {
        return uiLanguageService.getCurrentLanguage();
    }
}
