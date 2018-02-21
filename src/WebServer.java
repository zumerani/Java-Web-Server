import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class WebServer {
    public static void main (String[] args) {

        /* Check if number of arguments is four - necessary to run/start server. */
        if (args.length != 4) {
            System.out.println("Correct command: java WebServer -document_root / -port <port_number>");
        }

        int port = Integer.parseInt(args[3]); // Grab the port number to listen to.

        try {
            ServerSocket socket = new ServerSocket(port);
            while (true) {
                Socket s = socket.accept();
                HttpRequest request = new HttpRequest(s);
                Thread thread = new Thread(request); // Handle multi-threading.
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Something went wrong in creating the socket.");
        }
    }
}

class HttpRequest implements Runnable {
    Socket socket;

    public HttpRequest (Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            handleRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleRequest() throws Exception {
        InputStreamReader socketInputStream = new InputStreamReader(socket.getInputStream());
        BufferedReader socketInput = new BufferedReader(socketInputStream);

        BufferedOutputStream socketOutput = new BufferedOutputStream(socket.getOutputStream());
        PrintStream socketOutputStream = new PrintStream(socketOutput);

        String command = socketInput.readLine();
        System.out.println(command);

        String fileName = "";
        StringTokenizer tokenizedCommand = new StringTokenizer(command);

        try {

            /* Check to see if we can grab the file, if not we throw 'FileNotFoundException'. */
            if (tokenizedCommand.hasMoreElements() && tokenizedCommand.nextToken().equals("GET")) {
                fileName = tokenizedCommand.nextToken();

                if (fileName.equals("/")) { // This means we are trying to access index.html ... append index.html.
                    fileName += "index.html";
                }

                if (!fileName.equals("index.html")) {
                    fileName = "." + fileName; // Prepend a '.' to access the file.
                }

                File file = new File(fileName);

                /* Check to see if we can read the file. Check to see if we can access the file (permissions). */
                if (!(file.canRead()) && file.exists()) {
                    socketOutputStream.print("HTTP/1.0 403 Forbidden\n " + "Bad File: /" + file + "/");
                    socketOutputStream.close();
                    return;
                }

                FileInputStream fileInputStream = new FileInputStream(file);

                /* Below, we determine the content of the file. */
                String content = "";
                if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
                    content = "text/html";
                }
                else if (fileName.endsWith(".png")) {
                    content = "image/png";
                }
                else if (fileName.endsWith(".gif")) {
                    content = "image/gif";
                }
                else if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
                    content = "image/jpeg";
                }
                else if (fileName.endsWith(".css") || fileName.endsWith(".min.css")) {
                    content = "text/css";
                }
                else if (fileName.endsWith(".js")  || fileName.endsWith(".min.js")) {
                    content = "text/javascript";
                }
                else {
                    content = "text/plain";
                }

                /* Once we hit this point, the file does in fact exist, so we return 200. */
                socketOutputStream.print("HTTP/1.0 200 OK\r\n " + "Content-type: " + content + "\r\n\r\n");

                byte buffer[] = new byte[4096];
                int num;
                while ((num = fileInputStream.read(buffer)) > 0) {
                    socketOutputStream.write(buffer, 0, num);
                }

                socketOutputStream.close();
                socket.close();
                return;

            } else {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            socketOutputStream.print("HTTP/1.0 404 Not Found\r\n" + "Content-type: text/html\r\n\r\n" +
                    "<html><head></head><body>HTTP/1.0 404 Not Found<br>" + fileName + " not found</body></html>\n");
            socketOutputStream.close();
            socketInputStream.close();
            socket.close();
        }
    }
}