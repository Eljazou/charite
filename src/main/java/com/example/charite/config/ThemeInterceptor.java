package com.example.charite.config;

import com.example.charite.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ThemeInterceptor implements HandlerInterceptor {

    private final UserService userService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView mav) {
        if (mav != null) {
            try {
                var user = userService.getCurrentUser();
                mav.addObject("userTheme", user.getTheme());
                mav.addObject("currentUser", user);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            var user = userService.getCurrentUser();
            Locale locale = user.getLanguage().equals("en") ? Locale.ENGLISH : Locale.FRENCH;
            request.getSession().setAttribute(
                    SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locale);
        } catch (Exception ignored) {}
        return true;
    }
}