# Example instructions & usage guide:

        1: Compile Java files from inside /src/ folder:
            javac -d out core\*.java server\*.java ui\*.java testing\*.java
        - This command starts the chat server and 2 chat windows:
            java -ea -cp out testing.ChatTest --windowamount 2 
        - This command starts just a single window and no server. Remember to put the server IP as well:
            java -ea -cp out testing.ChatTest --noserver --windowamount 1 172.17.2.200
# Runtime args:

        [--noserver]: Don't start a server.
        [--windowamount]: Number of chat windows to create.
