# Modern Java Chat Application

A modern, website-like chat application built in Java with a beautiful UI featuring styled chat bubbles, emoji support, and a responsive sidebar.

## Features

### 🎨 Modern UI Design
- **Website-like appearance** with clean, modern styling
- **Styled chat bubbles** with different colors for sent vs received messages
- **Gradient backgrounds** and smooth animations
- **Responsive layout** that adapts to different window sizes

### 💬 Chat Features
- **Real-time messaging** with instant delivery
- **Timestamped messages** showing when each message was sent
- **Emoji support** with both text shortcuts and emoji picker
- **Private messaging** using @username format
- **System messages** for connection status and user events

### 👥 User Management
- **Online users sidebar** with real-time updates
- **User status indicators** showing who's currently online
- **User count display** in the sidebar header
- **Double-click users** to start private conversations

### 🎯 Emoji Support
- **Text shortcuts** like `:)`, `:D`, `<3`, `:heart:`, etc.
- **Emoji picker** with clickable emoji buttons
- **Popular emojis** including reactions, faces, and symbols

## How to Use

### Starting the Application

1. **Compile the code:**
   ```bash
   javac -d bin src/*.java
   ```

2. **Start the server:**
   ```bash
   java -cp bin Server
   ```

3. **Start the client:**
   ```bash
   java -cp bin Client
   ```

### Using the Chat Interface

1. **Connect to Server:**
   - Click the "Connect" button
   - Enter server host (default: localhost)
   - Enter server port (default: 8080)
   - Enter your username

2. **Send Messages:**
   - Type your message in the input field
   - Press Enter or click "Send"
   - Use emoji shortcuts like `:)` or click the emoji button

3. **Private Messages:**
   - Type `@username message` to send a private message
   - Double-click a user in the sidebar to quickly start a private message

4. **Emoji Features:**
   - Click the 😊 button to open the emoji picker
   - Use text shortcuts like `:heart:`, `:fire:`, `:rocket:`, etc.

## UI Components

### Header
- Modern dark header with app title and connection controls
- Status indicator showing connection state
- Connect/Disconnect buttons with hover effects

### Chat Area
- HTML-styled messages with custom CSS
- Sent messages appear on the right with gradient background
- Received messages appear on the left with white background
- Private messages have special styling with yellow accent
- System messages are centered with subtle background

### Sidebar
- Shows online users with green status indicators
- Real-time user count updates
- Clickable user list for private messaging
- Clean, modern styling with hover effects

### Input Area
- Modern text input with rounded borders
- Emoji picker button
- Send button with gradient styling
- Responsive layout

## Technical Details

### Message Types
- **BROADCAST**: Public messages to all users
- **PRIVATE**: Direct messages between users
- **USER_LIST**: Updates to online user list
- **CONNECT**: User connection notifications
- **DISCONNECT**: User disconnection notifications

### Emoji Shortcuts
- `:)` → 😊
- `:(` → 😢
- `:D` → 😃
- `;)` → 😉
- `:P` → 😛
- `:O` → 😮
- `<3` → ❤️
- `:heart:` → ❤️
- `:thumbsup:` → 👍
- `:thumbsdown:` → 👎
- `:wave:` → 👋
- `:fire:` → 🔥
- `:rocket:` → 🚀
- `:star:` → ⭐
- `:check:` → ✅
- `:x:` → ❌

## File Structure

```
src/
├── Client.java          # Modern chat client with UI
├── Server.java          # Chat server
├── ClientHandler.java   # Handles individual client connections
├── Message.java         # Message data structure
└── User.java           # User data structure

bin/                     # Compiled classes
*.bat                   # Windows batch files for easy execution
*.sh                    # Linux/Mac shell scripts for easy execution
```

## Requirements

- Java 8 or higher
- Network connectivity for client-server communication

## Screenshots

The application features a modern, clean interface with:
- Dark header with gradient buttons
- Styled chat bubbles with timestamps
- Responsive sidebar with online users
- Emoji picker and text shortcuts
- Professional color scheme and typography

Enjoy chatting with the modern Java chat application! 🚀
