# Java Chat Application Makefile

# Directories
SRCDIR = src
BINDIR = bin

# Java compiler
JAVAC = javac
JAVA = java

# Compiler flags
JAVACFLAGS = -d $(BINDIR) -cp $(SRCDIR)

# Source files
SOURCES = $(wildcard $(SRCDIR)/*.java)
CLASSES = $(SOURCES:$(SRCDIR)/%.java=$(BINDIR)/%.class)

# Default target
all: compile

# Compile all Java source files
compile: $(BINDIR)
	$(JAVAC) $(JAVACFLAGS) $(SOURCES)
	@echo "Compilation successful!"

# Create bin directory
$(BINDIR):
	mkdir -p $(BINDIR)

# Run the server
server: compile
	$(JAVA) -cp $(BINDIR) Server

# Run the client
client: compile
	$(JAVA) -cp $(BINDIR) Client

# Run server with custom port
server-port: compile
	@read -p "Enter port number: " port; \
	$(JAVA) -cp $(BINDIR) Server $$port

# Clean compiled files
clean:
	rm -rf $(BINDIR)
	@echo "Cleaned compiled files"

# Show help
help:
	@echo "Available targets:"
	@echo "  all        - Compile all source files (default)"
	@echo "  compile    - Compile all source files"
	@echo "  server     - Run the chat server on default port 8080"
	@echo "  client     - Run the chat client"
	@echo "  server-port - Run server with custom port"
	@echo "  clean      - Remove compiled files"
	@echo "  help       - Show this help message"

.PHONY: all compile server client server-port clean help
