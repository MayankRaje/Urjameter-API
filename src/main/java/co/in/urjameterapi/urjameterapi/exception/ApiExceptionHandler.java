package co.in.urjameterapi.urjameterapi.exception;

import org.springframework.http.ProblemDetail;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(PortalException.class)
    public ProblemDetail portal(PortalException e) {
        ProblemDetail result = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(e.status()), e.getMessage());
        result.setTitle(e.status() == 504 ? "Portal timeout" : e.status() == 503
                ? "Portal integration unavailable" : "Portal response error");
        return result;
    }
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail invalid(Exception e) {
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), "Invalid request. Use a positive page and a valid meter ID.");
    }
}

