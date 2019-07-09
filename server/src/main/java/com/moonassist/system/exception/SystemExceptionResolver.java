package com.moonassist.system.exception;

import com.google.common.collect.ImmutableMap;
import com.moonassist.exception.ConflictException;
import com.moonassist.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component
public class SystemExceptionResolver extends AbstractHandlerExceptionResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(SystemExceptionResolver.class);

  Map<Class, Integer> LOOKUP = ImmutableMap.of(
      IllegalArgumentException.class, HttpServletResponse.SC_BAD_REQUEST,
      IllegalStateException.class, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
      DataIntegrityViolationException.class, HttpServletResponse.SC_CONFLICT,
      ConflictException.class, HttpServletResponse.SC_CONFLICT,
      NotFoundException.class, HttpServletResponse.SC_NOT_FOUND
  );

  @Override
  protected ModelAndView doResolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {

    try {

      if (LOOKUP.containsKey(e.getClass())) {
        httpServletResponse.sendError(LOOKUP.get(e.getClass()));
      }
      else {
        httpServletResponse.sendError(500);
        throw new RuntimeException("Error handling expcetion", e);
      }

    } catch (IOException ioException) {
      LOGGER.error("Error: setting correct response, " + ioException.getMessage(), ioException);
    }

    return new ModelAndView();
  }

}
