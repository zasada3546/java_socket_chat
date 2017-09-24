package server;

public interface IAuthService {
    void start();
    void stop();
    String getNickByLoginPass(String login, String pass);
}
