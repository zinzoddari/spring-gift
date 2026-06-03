package gift.infra.exception;

import jakarta.persistence.OptimisticLockException;
import java.util.NoSuchElementException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(final IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElement(final NoSuchElementException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Void> handleUnauthorized(final UnauthorizedException e) {
        return ResponseEntity.status(401).build();
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Void> handleForbidden(final SecurityException e) {
        return ResponseEntity.status(403).build();
    }

    @ExceptionHandler({OptimisticLockException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<String> handleOptimisticLock(final RuntimeException e) {
        return ResponseEntity.status(409).body("잠시 후 다시 시도해 주세요.");
    }
}
