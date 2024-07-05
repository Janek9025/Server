package org.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable {

    private Socket socket;
    private Server server;
    private Scanner input;
    private PrintWriter output;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        try {
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(String message) {
        output.println(message);
    }

    @Override
    public void run() {
        String login = input.nextLine();
        server.addClient(login, this);
        server.printClients();
        server.broadcast("Server: " + login + " joined.");
        String message;
        do {
            message = input.nextLine();
            if (message.equalsIgnoreCase("/online")){
                output.println(server.getClients());
                continue;
            }
            if (message.startsWith("/w")){
                String username = message.split(" ")[1];
                if (server.isClientOnline(username)) {
                    server.send(username, login + ": " + message.substring(3 + username.length() + 1));
                } else {
                    server.send(login, "Server ERROR: '" + username + "' is not online.");
                }

                continue;
            }
            server.broadcast(login + ": " + message);
        } while (!message.equalsIgnoreCase("bye"));
        server.removeClient(login);
        server.broadcast("Server: " + login + " left.");
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
