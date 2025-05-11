# Instructions & usage guide:

        1: Compile Java files from inside /src/ folder:
            javac -d out core\*.java server\*.java ui\*.java testing\*.java
        2: This command starts the chat server and 2 chat windows:
            java -ea -cp out testing.ChatTest --windowamount 2
# Runtime args:

        [--noserver]: Don't start a server.
        [--windowamount]: Number of chat windows to create.
