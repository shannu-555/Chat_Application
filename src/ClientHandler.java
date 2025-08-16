import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private User user;
    private List<ClientHandler> clients;
    private boolean running;
    
    public ClientHandler(Socket socket, List<ClientHandler> clients) {
        this.clientSocket = socket;
        this.clients = clients;
        this.running = true;
        
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error creating streams: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        try {
            // Wait for username from client
            Message connectMessage = (Message) input.readObject();
            if (connectMessage.getType() == Message.MessageType.CONNECT) {
                String username = connectMessage.getContent();
                
                // Check if username is already taken
                if (isUsernameTaken(username)) {
                    sendMessage(new Message("Server", username, "Username already taken. Please choose another.", Message.MessageType.PRIVATE));
                    closeConnection();
                    return;
                }
                
                // Create user and add to list
                user = new User(username);
                synchronized (clients) {
                    clients.add(this);
                }
                
                // Send confirmation to client
                sendMessage(new Message("Server", username, "Connected successfully!", Message.MessageType.CONNECT));
                
                // Broadcast user joined
                broadcastMessage(new Message("Server", "all", username + " has joined the chat!", Message.MessageType.BROADCAST), this);
                
                // Send updated user list to all clients
                updateUserList();
                
                System.out.println(username + " connected from " + clientSocket.getInetAddress());
                
                // Listen for messages
                while (running && !clientSocket.isClosed()) {
                    try {
                        Message message = (Message) input.readObject();
                        if (message != null) {
                            handleMessage(message);
                        }
                    } catch (EOFException e) {
                        System.out.println("Client " + username + " disconnected (EOF)");
                        break;
                    } catch (IOException e) {
                        if (running) {
                            System.err.println("Error reading message from " + username + ": " + e.getMessage());
                        }
                        break;
                    } catch (ClassNotFoundException e) {
                        System.err.println("Error deserializing message from " + username + ": " + e.getMessage());
                        break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in client handler: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }
    
    private void handleMessage(Message message) {
        if (message == null || message.getType() == null) {
            return;
        }
        
        switch (message.getType()) {
            case BROADCAST:
                broadcastMessage(message, this);
                break;
            case PRIVATE:
                sendPrivateMessage(message);
                break;
            case USERNAME_UPDATE:
                updateUsername(message.getContent());
                break;
            case DISCONNECT:
                System.out.println("Client " + user.getUsername() + " requested disconnect");
                closeConnection();
                break;
            default:
                System.err.println("Unknown message type: " + message.getType());
        }
    }
    
    private void broadcastMessage(Message message, ClientHandler sender) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender && client.isRunning()) {
                    try {
                        client.sendMessage(message);
                    } catch (Exception e) {
                        System.err.println("Error broadcasting to " + client.getUsername() + ": " + e.getMessage());
                    }
                }
            }
        }
    }
    
    private void sendPrivateMessage(Message message) {
        String recipient = message.getRecipient();
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.getUser() != null && client.getUser().getUsername().equals(recipient) && client.isRunning()) {
                    try {
                        client.sendMessage(message);
                        // Send confirmation to sender
                        sendMessage(new Message("Server", message.getSender(), 
                            "Private message sent to " + recipient, Message.MessageType.PRIVATE));
                        return;
                    } catch (Exception e) {
                        System.err.println("Error sending private message to " + recipient + ": " + e.getMessage());
                    }
                }
            }
        }
        // Recipient not found
        sendMessage(new Message("Server", message.getSender(), 
            "User " + recipient + " is not online or doesn't exist.", Message.MessageType.PRIVATE));
    }
    
    private void updateUsername(String newUsername) {
        if (isUsernameTaken(newUsername)) {
            sendMessage(new Message("Server", user.getUsername(), 
                "Username " + newUsername + " is already taken.", Message.MessageType.PRIVATE));
            return;
        }
        
        String oldUsername = user.getUsername();
        user.setUsername(newUsername);
        
        // Notify all clients about username change
        broadcastMessage(new Message("Server", "all", 
            oldUsername + " is now known as " + newUsername, Message.MessageType.BROADCAST), null);
        
        updateUserList();
    }
    
    private boolean isUsernameTaken(String username) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.getUser() != null && client.getUser().getUsername().equals(username)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void updateUserList() {
        StringBuilder userList = new StringBuilder();
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.getUser() != null) {
                    userList.append(client.getUser().getUsername()).append(",");
                }
            }
        }
        
        Message userListMessage = new Message("Server", "all", userList.toString(), Message.MessageType.USER_LIST);
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.isRunning()) {
                    try {
                        client.sendMessage(userListMessage);
                    } catch (Exception e) {
                        System.err.println("Error sending user list to " + client.getUsername() + ": " + e.getMessage());
                    }
                }
            }
        }
    }
    
    public void sendMessage(Message message) {
        if (!running || clientSocket.isClosed()) {
            return;
        }
        
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            System.err.println("Error sending message to " + (user != null ? user.getUsername() : "unknown") + ": " + e.getMessage());
            closeConnection();
        }
    }
    
    private void closeConnection() {
        running = false;
        
        if (user != null) {
            // Remove from clients list
            synchronized (clients) {
                clients.remove(this);
            }
            
            // Broadcast user left
            broadcastMessage(new Message("Server", "all", 
                user.getUsername() + " has left the chat!", Message.MessageType.BROADCAST), null);
            
            // Update user list
            updateUserList();
            
            System.out.println(user.getUsername() + " disconnected");
        }
        
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    
    public User getUser() {
        return user;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public String getUsername() {
        return user != null ? user.getUsername() : "unknown";
    }
}
