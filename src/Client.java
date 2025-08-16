import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class Client extends JFrame {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;
    
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String username;
    private boolean connected = false;
    
    // GUI Components
    private JEditorPane chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private JLabel statusLabel;
    private JButton connectButton;
    private JButton disconnectButton;
    private JPanel sidebarPanel;
    private JLabel onlineUsersLabel;
    private JButton emojiButton;
    
    // Thread for listening to server messages
    private Thread listenerThread;
    
    // Emoji map
    private static final Map<String, String> EMOJI_MAP = new HashMap<>();
    static {
        EMOJI_MAP.put(":)", "😊");
        EMOJI_MAP.put(":(", "😢");
        EMOJI_MAP.put(":D", "😃");
        EMOJI_MAP.put(";)", "😉");
        EMOJI_MAP.put(":P", "😛");
        EMOJI_MAP.put(":O", "😮");
        EMOJI_MAP.put("<3", "❤️");
        EMOJI_MAP.put(":heart:", "❤️");
        EMOJI_MAP.put(":thumbsup:", "👍");
        EMOJI_MAP.put(":thumbsdown:", "👎");
        EMOJI_MAP.put(":wave:", "👋");
        EMOJI_MAP.put(":fire:", "🔥");
        EMOJI_MAP.put(":rocket:", "🚀");
        EMOJI_MAP.put(":star:", "⭐");
        EMOJI_MAP.put(":check:", "✅");
        EMOJI_MAP.put(":x:", "❌");
    }
    
    public Client() {
        setupGUI();
        setupEventHandlers();
    }
    
    private void setupGUI() {
        setTitle("Modern Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }
        
        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(248, 249, 250));
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        
        // Content panel (chat area and sidebar)
        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setBackground(Color.WHITE);
        
        // Chat area (center)
        chatArea = new JEditorPane();
        chatArea.setEditable(false);
        chatArea.setContentType("text/html");
        chatArea.setBackground(new Color(248, 249, 250));
        
        // Set up HTML document with custom CSS
        HTMLEditorKit kit = new HTMLEditorKit();
        HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
        doc.setBase(getClass().getResource("/"));
        
        // Add CSS styles
        String css = """
            <style>
                body { 
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                    margin: 0; 
                    padding: 20px; 
                    background-color: #f8f9fa;
                    line-height: 1.4;
                }
                .message-container { 
                    margin-bottom: 15px; 
                    display: flex; 
                    flex-direction: column;
                }
                .message-sent { 
                    align-items: flex-end; 
                }
                .message-received { 
                    align-items: flex-start; 
                }
                .message-bubble { 
                    max-width: 70%; 
                    padding: 12px 16px; 
                    border-radius: 18px; 
                    margin: 2px 0; 
                    word-wrap: break-word;
                    box-shadow: 0 1px 2px rgba(0,0,0,0.1);
                }
                .message-sent .message-bubble { 
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                    color: white; 
                    border-bottom-right-radius: 4px;
                }
                .message-received .message-bubble { 
                    background: white; 
                    color: #333; 
                    border: 1px solid #e9ecef;
                    border-bottom-left-radius: 4px;
                }
                .message-info { 
                    font-size: 11px; 
                    color: #6c757d; 
                    margin: 2px 8px; 
                    font-weight: 500;
                }
                .message-sent .message-info { 
                    text-align: right; 
                }
                .message-received .message-info { 
                    text-align: left; 
                }
                .system-message { 
                    text-align: center; 
                    color: #6c757d; 
                    font-style: italic; 
                    margin: 10px 0;
                    padding: 8px;
                    background: rgba(108, 117, 125, 0.1);
                    border-radius: 8px;
                }
                .private-message { 
                    background: rgba(255, 193, 7, 0.1) !important; 
                    border-left: 3px solid #ffc107 !important;
                }
            </style>
            """;
        
        doc.putProperty("stylesheet", css);
        chatArea.setDocument(doc);
        
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setBorder(null);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Sidebar panel
        sidebarPanel = createSidebarPanel();
        
        // Message input area (bottom)
        JPanel inputPanel = createInputPanel();
        
        // Add components to content panel
        contentPanel.add(chatScrollPane, BorderLayout.CENTER);
        contentPanel.add(sidebarPanel, BorderLayout.EAST);
        
        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        
        // Initialize chat area with welcome message
        appendSystemMessage("Welcome to Modern Chat! Click 'Connect' to join the conversation.");
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(new Color(52, 58, 64));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        // Title
        JLabel titleLabel = new JLabel("💬 Modern Chat");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlPanel.setOpaque(false);
        
        connectButton = createStyledButton("Connect", new Color(40, 167, 69));
        disconnectButton = createStyledButton("Disconnect", new Color(220, 53, 69));
        disconnectButton.setEnabled(false);
        
        statusLabel = new JLabel("Disconnected");
        statusLabel.setForeground(new Color(255, 193, 7));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        controlPanel.add(statusLabel);
        controlPanel.add(connectButton);
        controlPanel.add(disconnectButton);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(controlPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel(new BorderLayout(0, 0));
        sidebarPanel.setPreferredSize(new Dimension(250, 0));
        sidebarPanel.setBackground(Color.WHITE);
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(233, 236, 239)));
        
        // Online users header
        onlineUsersLabel = new JLabel("👥 Online Users (0)");
        onlineUsersLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        onlineUsersLabel.setBorder(new EmptyBorder(15, 15, 10, 15));
        onlineUsersLabel.setForeground(new Color(52, 58, 64));
        
        // User list
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setBackground(new Color(248, 249, 250));
        userList.setBorder(null);
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // Custom cell renderer for user list
        userList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                if (value != null) {
                    setText("🟢 " + value.toString());
                    setBorder(new EmptyBorder(8, 15, 8, 15));
                    
                    if (isSelected) {
                        setBackground(new Color(0, 123, 255, 25));
                        setForeground(new Color(0, 123, 255));
                    } else {
                        setBackground(new Color(248, 249, 250));
                        setForeground(new Color(52, 58, 64));
                    }
                }
                return this;
            }
        });
        
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(null);
        
        sidebarPanel.add(onlineUsersLabel, BorderLayout.NORTH);
        sidebarPanel.add(userScrollPane, BorderLayout.CENTER);
        
        return sidebarPanel;
    }
    
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        // Emoji button
        emojiButton = new JButton("😊");
        emojiButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        emojiButton.setPreferredSize(new Dimension(40, 40));
        emojiButton.setBackground(new Color(248, 249, 250));
        emojiButton.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)));
        emojiButton.setFocusPainted(false);
        
        // Message field
        messageField = new JTextField();
        messageField.setEnabled(false);
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(233, 236, 239)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Send button
        sendButton = createStyledButton("Send", new Color(0, 123, 255));
        sendButton.setEnabled(false);
        sendButton.setPreferredSize(new Dimension(80, 40));
        
        // Input container
        JPanel inputContainer = new JPanel(new BorderLayout(10, 0));
        inputContainer.setOpaque(false);
        inputContainer.add(emojiButton, BorderLayout.WEST);
        inputContainer.add(messageField, BorderLayout.CENTER);
        inputContainer.add(sendButton, BorderLayout.EAST);
        
        inputPanel.add(inputContainer, BorderLayout.CENTER);
        
        return inputPanel;
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(backgroundColor.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }
    
    private void setupEventHandlers() {
        // Connect button
        connectButton.addActionListener(e -> connectToServer());
        
        // Disconnect button
        disconnectButton.addActionListener(e -> disconnectFromServer());
        
        // Send button
        sendButton.addActionListener(e -> sendMessage());
        
        // Message field enter key
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
        
        // Emoji button
        emojiButton.addActionListener(e -> showEmojiPanel());
        
        // User list double-click for private message
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedUser = userList.getSelectedValue();
                    if (selectedUser != null && !selectedUser.equals(username)) {
                        messageField.setText("@" + selectedUser + " ");
                        messageField.requestFocus();
                    }
                }
            }
        });
        
        // Window close event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnectFromServer();
            }
        });
    }
    
    private void showEmojiPanel() {
        String[] emojis = {"😊", "😂", "❤️", "👍", "👎", "🎉", "🔥", "🚀", "⭐", "✅", "❌", "👋", "😢", "😃", "😉", "😛", "😮"};
        
        JPopupMenu emojiMenu = new JPopupMenu();
        JPanel emojiPanel = new JPanel(new GridLayout(0, 4, 5, 5));
        emojiPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        for (String emoji : emojis) {
            JButton emojiBtn = new JButton(emoji);
            emojiBtn.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            emojiBtn.setPreferredSize(new Dimension(40, 40));
            emojiBtn.setFocusPainted(false);
            emojiBtn.addActionListener(e -> {
                messageField.setText(messageField.getText() + emoji);
                messageField.requestFocus();
                emojiMenu.setVisible(false);
            });
            emojiPanel.add(emojiBtn);
        }
        
        emojiMenu.add(emojiPanel);
        emojiMenu.show(emojiButton, 0, -emojiMenu.getPreferredSize().height);
    }
    
    private void connectToServer() {
        String host = JOptionPane.showInputDialog(this, "Enter server host:", DEFAULT_HOST);
        if (host == null) return;
        
        String portStr = JOptionPane.showInputDialog(this, "Enter server port:", String.valueOf(DEFAULT_PORT));
        if (portStr == null) return;
        
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid port number", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        username = JOptionPane.showInputDialog(this, "Enter your username:");
        if (username == null || username.trim().isEmpty()) return;
        
        try {
            socket = new Socket(host, port);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            
            // Send connection message with username
            Message connectMessage = new Message(username, "server", username, Message.MessageType.CONNECT);
            output.writeObject(connectMessage);
            
            // Start listener thread
            startListenerThread();
            
            // Update GUI
            connected = true;
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            messageField.setEnabled(true);
            sendButton.setEnabled(true);
            statusLabel.setText("Connected to " + host + ":" + port);
            statusLabel.setForeground(new Color(40, 167, 69));
            
            appendSystemMessage("Connecting to server...");
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to connect: " + e.getMessage(), 
                "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void disconnectFromServer() {
        if (connected) {
            try {
                if (output != null) {
                    Message disconnectMessage = new Message(username, "server", "Disconnecting", Message.MessageType.DISCONNECT);
                    output.writeObject(disconnectMessage);
                }
            } catch (IOException e) {
                System.err.println("Error sending disconnect message: " + e.getMessage());
            }
            
            closeConnection();
        }
    }
    
    private void closeConnection() {
        connected = false;
        
        // Stop listener thread
        if (listenerThread != null && listenerThread.isAlive()) {
            listenerThread.interrupt();
            try {
                listenerThread.join(1000); // Wait up to 1 second for thread to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Close streams and socket
        try {
            if (input != null) {
                input.close();
                input = null;
            }
            if (output != null) {
                output.close();
                output = null;
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
        
        // Update GUI
        SwingUtilities.invokeLater(() -> {
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
            messageField.setEnabled(false);
            sendButton.setEnabled(false);
            statusLabel.setText("Disconnected");
            statusLabel.setForeground(new Color(255, 193, 7));
            userListModel.clear();
            updateOnlineUsersCount();
            
            appendSystemMessage("Disconnected from server.");
        });
    }
    
    private void startListenerThread() {
        listenerThread = new Thread(() -> {
            try {
                while (connected && !Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                    try {
                        Message message = (Message) input.readObject();
                        if (message != null) {
                            handleIncomingMessage(message);
                        }
                    } catch (EOFException e) {
                        // Server closed connection
                        SwingUtilities.invokeLater(() -> {
                            appendSystemMessage("Server closed the connection.");
                            closeConnection();
                        });
                        break;
                    } catch (IOException e) {
                        if (connected && !socket.isClosed()) {
                            SwingUtilities.invokeLater(() -> {
                                appendSystemMessage("Connection error: " + e.getMessage());
                                closeConnection();
                            });
                        }
                        break;
                    } catch (ClassNotFoundException e) {
                        SwingUtilities.invokeLater(() -> {
                            appendSystemMessage("Error processing message: " + e.getMessage());
                        });
                    }
                }
            } catch (Exception e) {
                if (connected) {
                    SwingUtilities.invokeLater(() -> {
                        appendSystemMessage("Unexpected error: " + e.getMessage());
                        closeConnection();
                    });
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
    
    private void handleIncomingMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case BROADCAST:
                    appendMessage(message.getSender(), message.getContent(), message.getTimestamp(), false, false);
                    break;
                case PRIVATE:
                    appendMessage(message.getSender(), message.getContent(), message.getTimestamp(), false, true);
                    break;
                case USER_LIST:
                    updateUserList(message.getContent());
                    break;
                case CONNECT:
                    appendSystemMessage(message.getContent());
                    break;
                case DISCONNECT:
                    appendSystemMessage(message.getContent());
                    break;
            }
            
            // Auto-scroll to bottom
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    private void updateUserList(String userListString) {
        userListModel.clear();
        if (userListString != null && !userListString.isEmpty()) {
            String[] users = userListString.split(",");
            for (String user : users) {
                if (!user.trim().isEmpty()) {
                    userListModel.addElement(user.trim());
                }
            }
        }
        updateOnlineUsersCount();
    }
    
    private void updateOnlineUsersCount() {
        onlineUsersLabel.setText("👥 Online Users (" + userListModel.getSize() + ")");
    }
    
    private void sendMessage() {
        if (!connected) return;
        
        String messageText = messageField.getText().trim();
        if (messageText.isEmpty()) return;
        
        try {
            Message message;
            boolean isPrivate = false;
            String displayContent = messageText;
            
            if (messageText.startsWith("@")) {
                // Private message
                int spaceIndex = messageText.indexOf(' ');
                if (spaceIndex > 1) {
                    String recipient = messageText.substring(1, spaceIndex);
                    String content = messageText.substring(spaceIndex + 1);
                    message = new Message(username, recipient, content, Message.MessageType.PRIVATE);
                    displayContent = "To " + recipient + ": " + content;
                    isPrivate = true;
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid private message format. Use: @username message", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                // Broadcast message
                message = new Message(username, "all", messageText, Message.MessageType.BROADCAST);
            }
            
            output.writeObject(message);
            output.flush();
            
            // Display sent message
            appendMessage("You", displayContent, System.currentTimeMillis(), true, isPrivate);
            
            messageField.setText("");
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to send message: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            closeConnection();
        }
    }
    
    private void appendMessage(String sender, String content, long timestamp, boolean isSent, boolean isPrivate) {
        String processedContent = processEmojis(content);
        String timestampStr = new SimpleDateFormat("HH:mm").format(new Date(timestamp));
        
        String messageClass = isSent ? "message-sent" : "message-received";
        String bubbleClass = isPrivate ? "message-bubble private-message" : "message-bubble";
        
        String html = String.format("""
            <div class="message-container %s">
                <div class="%s">%s</div>
                <div class="message-info">%s • %s</div>
            </div>
            """, messageClass, bubbleClass, processedContent, sender, timestampStr);
        
        try {
            HTMLDocument doc = (HTMLDocument) chatArea.getDocument();
            doc.insertAfterEnd(doc.getDefaultRootElement(), html);
        } catch (Exception e) {
            System.err.println("Error appending message: " + e.getMessage());
        }
    }
    
    private void appendSystemMessage(String content) {
        String html = String.format("<div class=\"system-message\">%s</div>", content);
        
        try {
            HTMLDocument doc = (HTMLDocument) chatArea.getDocument();
            doc.insertAfterEnd(doc.getDefaultRootElement(), html);
        } catch (Exception e) {
            System.err.println("Error appending system message: " + e.getMessage());
        }
    }
    
    private String processEmojis(String text) {
        String processed = text;
        for (Map.Entry<String, String> entry : EMOJI_MAP.entrySet()) {
            processed = processed.replace(entry.getKey(), entry.getValue());
        }
        return processed;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Client client = new Client();
            client.setVisible(true);
        });
    }
}
