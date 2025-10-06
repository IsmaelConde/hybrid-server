package es.uvigo.esei.dai.hybridserver;

import es.uvigo.esei.dai.hybridserver.dao.HTMLDao;
import es.uvigo.esei.dai.hybridserver.dao.HTMLDaoDB;
import es.uvigo.esei.dai.hybridserver.dao.HTMLMapDao;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HybridServer implements AutoCloseable {

    private final int servicePort;;
    private Thread serverThread;
    private boolean stop;
    private static int numClients;

    private ExecutorService executor;

    protected static Map<String,String> pages = new ConcurrentHashMap<>();
    private Connection connection;

    private final HTMLController htmlController;
    public HybridServer() {
        this(new Properties());
    }

    public HybridServer(Map<String, String> pages) {

        this(new Properties());

        for (Map.Entry<String, String> entry : pages.entrySet()) {
            try {
                this.htmlController.createPage(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                throw new RuntimeException("Error al inicializar el servidor con las páginas proporcionadas", e);
            }
        }
    }

    public HybridServer(Properties properties)  {
        // TODO Inicializar con los parámetros recibidos
        this.servicePort = Integer.parseInt(properties.getProperty("port", "8888"));
        numClients = Integer.parseInt(properties.getProperty("numClients", "50")); // En caso de que no se reciba el contenido, pone por defecto a 50

        executor = Executors.newFixedThreadPool(numClients);

        HTMLDao dao;
        if(properties.getProperty("db.url") != null){
            try {
                dao = new HTMLDaoDB(properties.getProperty("db.url"),
                        properties.getProperty("db.user"),
                        properties.getProperty("db.password"));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }else{
            dao = new HTMLMapDao();
        }
        htmlController = new HTMLController(dao);



        // Creamos conexion con la base de datos (Falta saber como iniciar la jdbc)
        /*
        try{
            this.connection = DriverManager.getConnection(properties.getProperty("db.url"),  properties.getProperty("db.user"), properties.getProperty("db.password"));
            System.out.println("Connection established: " +  this.connection.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        */
    }

    public HTMLController getHtmlController() {
        return htmlController;
    }

    public int getPort() {
        return this.servicePort; // Devuelve la variable de instancia
    }

    public void start() {
        this.stop = false;
        this.serverThread = new Thread() { // Dejamos que un hilo ejecute el servidor, ya que podemos tener varios servidores en el mismo JVM (hacer una especie de "servidores virtuales")
            @Override
            public void run() {
                try (final ServerSocket serverSocket = new ServerSocket(getPort())) {
                    while (true) {
                        Socket socket = serverSocket.accept();

                        // Pool de hilos (Para cuando los clientes se conectan al servidor)
                        executor.submit(new ClientThread(socket, htmlController,getPort()));
                        // ----------------
                        if (stop)
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };


        this.serverThread.start();
    }

    @Override
    public void close() {
        this.stop = true;

        try (Socket socket = new Socket("localhost", getPort())) {
            // Esta conexión se hace, simplemente, para "despertar" el hilo servidor
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            this.serverThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(executor != null) executor.shutdown();

        this.serverThread = null;
    }

    private void showHtml(OutputStream outStream, int code, String message) throws IOException {
        int statusCode;
        String reasonPhrase;
        switch (code) {
            case 200 -> { statusCode = 200; reasonPhrase = "OK"; }
            case 400 -> { statusCode = 400; reasonPhrase = "Bad Request"; }
            case 404 -> { statusCode = 404; reasonPhrase = "Not Found"; }
            case 500 -> { statusCode = 500; reasonPhrase = "Internal Server Error"; }
            default -> { statusCode = code; reasonPhrase = "Error"; }
        }

        String body = (code == 200) ? message : "<html><body><h1>" + statusCode + " " + reasonPhrase + "</h1><p>" + message + "</p>" + insertFooter() + "</body></html>";

        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        // Línea de estado
        outStream.write(("HTTP/1.1 " + statusCode + " " + reasonPhrase + "\r\n").getBytes(StandardCharsets.UTF_8));

        // Cabeceras
        outStream.write(("Content-Length: " + bodyBytes.length + "\r\n").getBytes(StandardCharsets.UTF_8));
        outStream.write("Content-Type: text/html\r\n".getBytes(StandardCharsets.UTF_8));
        outStream.write("\r\n".getBytes(StandardCharsets.UTF_8));

        // Cuerpo
        outStream.write(bodyBytes);
        outStream.flush();
    }


    private String insertFooter(){
        String footer =
                new StringBuilder().append("<hr><footer><a href=http://${ip_server}:").append(getPort()).append(">Volver al inicio</a></footer>").toString();

        return footer.replace("${ip_server}", "localhost");
    }
}
