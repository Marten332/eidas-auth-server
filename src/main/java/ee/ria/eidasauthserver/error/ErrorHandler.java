package ee.ria.eidasauthserver.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;

@ControllerAdvice
@Slf4j
class ErrorHandler {

    @ExceptionHandler({BadRequestException.class})
    public ModelAndView handleBindException(BadRequestException ex, HttpServletResponse response) throws IOException {
        log.error("User exception: {}", ex.getMessage(), ex);
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return new ModelAndView();
    }

    @ExceptionHandler({Exception.class})
    public void handleAll(Exception ex) throws Exception {
        log.error("Server encountered an unexpected error: {}", ex.getMessage(), ex);
        throw ex;
    }
}
