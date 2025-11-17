package ir.bahman.academic_lms.exception;

public class ExamNotActiveException extends RuntimeException {
    public ExamNotActiveException(String message) {
        super(message);
    }
}
