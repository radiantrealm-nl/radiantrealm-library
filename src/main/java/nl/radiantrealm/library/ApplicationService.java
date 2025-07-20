package nl.radiantrealm.library;

public interface ApplicationService {
    void start() throws Exception;
    void stop() throws Exception;
    ApplicationStatus status() throws Exception;
}
