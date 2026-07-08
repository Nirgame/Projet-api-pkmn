package net.tcgdex.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class UiLanguageService {
    public static final String SESSION_KEY = "uiLanguage";
    private static final String DEFAULT_LANGUAGE = "fr";

    public String getCurrentLanguage() {
        ServletRequestAttributes attributes = getRequestAttributes();
        if (attributes == null) {
            return DEFAULT_LANGUAGE;
        }

        HttpSession session = attributes.getRequest().getSession(false);
        if (session == null) {
            return DEFAULT_LANGUAGE;
        }

        Object value = session.getAttribute(SESSION_KEY);
        return normalizeLanguage(value instanceof String language ? language : null);
    }

    public void setCurrentLanguage(String language) {
        ServletRequestAttributes attributes = getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        request.getSession(true).setAttribute(SESSION_KEY, normalizeLanguage(language));
    }

    public String normalizeLanguage(String language) {
        if ("en".equalsIgnoreCase(language)) {
            return "en";
        }
        return DEFAULT_LANGUAGE;
    }

    private ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes;
        }
        return null;
    }
}
