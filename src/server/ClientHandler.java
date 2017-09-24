package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private Server server;
    private DataOutputStream out;
    private DataInputStream in;
    private String name;

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.server = server;
            this.name = "";
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            try {
                // Основной цикл для повторных авторизаций
                while (true) {
                    // Авторизация клиента по логину / паролю
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/auth ")) {
                            String[] elements = str.split(" ");

                            if (elements.length == 3) {
                                String nick = server.getAuthService().getNickByLoginPass(elements[1], elements[2]);
                                if (nick != null) {
                                    if (!server.isNickBusy(nick)) {
                                        sendMessage("/authok " + nick);

                                        this.name = nick;

                                        setAuthorized(true);

                                        break;
                                    } else {
                                        sendMessage("/authfail Учётная запись уже используется");
                                    }
                                } else {
                                    sendMessage("/authfail Неверный логин / пароль");
                                }
                            } else {
                                sendMessage("/authfail Неверное кол-во параметров");
                            }
                        } else {
                            sendMessage("/authfail Для начала нужна авторизация");
                        }
                    }

                    while (true) {
                        String str = in.readUTF();

                        if (str.equalsIgnoreCase("/end")) {
                            server.broadcast(str, name);

                            break;
                        }

                        System.out.println("Client " + name + ": " + str);

                        if (str.startsWith("/w ")) {
                            String[] elements = str.split(" ");

                            server.broadcast(name + " -> " + elements[1] + " (DM): " + elements[2], name, elements[1]);
                        } else {
                            server.broadcast(name + " : " + str);
                        }
                    }

                    setAuthorized(false);
                }
            } catch (IOException e) {
                // e.printStackTrace();
            } finally {
                setAuthorized(false);

                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    private void setAuthorized(boolean isAuthorized) {
        if (isAuthorized) {
            server.subscribe(this);

            if (!name.isEmpty()) {
                server.broadcast("Пользователь " + name + " зашёл в чат");
                server.broadcastUserList();
            }
        } else {
            server.unsubscribe(this);

            if (!name.isEmpty()) {
                server.broadcast("Пользователь " + name + " вышел из чата");
                server.broadcastUserList();
            }
        }
    }
}
