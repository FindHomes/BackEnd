package com.findhomes.findhomesbe.argument_resolver;

import com.findhomes.findhomesbe.exception.exception.PreconditionRequiredException;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class SessionValueArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(SessionValue.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        SessionValue sessionValueAnnotation = parameter.getParameterAnnotation(SessionValue.class);
        if (sessionValueAnnotation != null) {
            HttpSession session = webRequest.getNativeRequest(jakarta.servlet.http.HttpServletRequest.class).getSession();
            return session.getAttribute(sessionValueAnnotation.value());
        } else {
            throw new PreconditionRequiredException("필수 조건이 입력되지 않았습니다.");
        }
    }
}
