package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class ClientConnection implements Runnable {
    private static Socket socket;
    private DataInputStream in;
    private static DataOutputStream out;

    private static String nick;
    private String login;
    private String pass;

    private ChatController chatController;

    public ClientConnection(String login, String pass, ChatController chatController) {
        this.login = login;
        this.pass = pass;
        this.chatController = chatController;
    }

    public void login() {
        try {
            out.writeUTF("/auth " + this.login + " " + this.pass);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logout() {
        nick = null;

        sendMessage("/end");
    }

    public static void sendMessage(String message) {
        if (message.isEmpty()) {
            return;
        }

        try {
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            this.login();

            // Цикл для принятия сообщений
            while (socket.isConnected()) {
                String msg = in.readUTF();

                if (nick != null) {
                    if (msg.startsWith("/")) {
                        if (msg.equalsIgnoreCase("/end")) {
                            this.logout();
                            this.chatController.logoutButtonAction();
                        } else if (msg.startsWith("/userlist ")) {
                            String[] users = msg.split(" ");

                            this.chatController.updateUserList(Arrays.copyOfRange(users, 1, users.length));
                        }
                    } else {
                        if (!msg.isEmpty()) {
                            this.chatController.addMessage(msg);
                        }
                    }
                } else {
                    if (msg.startsWith("/authok")) {
                        String[] elements = msg.split(" ");
                        nick = elements[1];
                        this.chatController.setNickLabel(nick);

                        LoginController.getInstance().showScene();
                    } else if (msg.startsWith("/authfail ")) {
                        String response = msg.substring(10);
                        LoginController.getInstance().showResponse(response);

                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
