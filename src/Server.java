import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int DEFAULT_PORT = 8080;
    private static final int MAX_CLIENTS = 100;
    
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private ExecutorService threadPool;
    private boolean running;
    
    public Server(int port) {
        clients = new CopyOnWriteArrayList<>();
        threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
        running = false;
        
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Chat Server started on port " + port);
            System.out.println("Waiting for clients to connect...");
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            System.exit(1);
        }
    }
    
    public void start() {
        running = true;
        
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket, clients);
                threadPool.execute(clientHandler);
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }
    
    public void stop() {
        running = false;
        
        // Close all client connections
        for (ClientHandler client : clients) {
            client.sendMessage(new Message("Server", "all", "Server is shutting down.", Message.MessageType.BROADCAST));
        }
        
        // Shutdown thread pool
        threadPool.shutdown();
        
        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        
        System.out.println("Server stopped.");
    }
    
    public List<ClientHandler> getClients() {
        return clients;
    }
    
    public int getClientCount() {
        return clients.size();
    }
    
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port " + DEFAULT_PORT);
            }
        }
        
        Server server = new Server(port);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down server...");
            server.stop();
        }));
        
        // Start server
        server.start();
    }
}
