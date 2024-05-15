import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {

    private final ObjectInputStream reader;
    private final ObjectOutputStream writer;

    public Server(ServerSocket server) throws LineUnavailableException {
        try {
            Socket socket = server.accept(); //ждём подключения клиента
            System.out.println("new connection" + socket.getInetAddress() + ":" + socket.getPort());
            Main.isConnected = true;

            writer = new ObjectOutputStream(socket.getOutputStream());
            reader = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while (Main.programIsActive) {
            send();
            try {
                ClientStorage clientStorage = read();
                byte[] image = clientStorage.image;

                Main.clientScreen = ImageIO.read(new ByteArrayInputStream(image));
                if(clientStorage.message!=null){
                    Main.clientMessage = clientStorage.message;
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void send() {
        try {
            ClientStorage clientStorage = new ClientStorage();

            ByteArrayOutputStream baos = null;
            try {
                baos = new ByteArrayOutputStream();
                ImageIO.write(Main.screen, "jpeg", baos);
            } finally {
                try {
                    assert baos != null;
                    baos.close();
                } catch (Exception ignored) {
                }
            }

            clientStorage.image = baos.toByteArray();
            if(Main.ownMessage!=null) {
                clientStorage.message = Main.ownMessage;
                Main.ownMessage = null;
            }

            writer.writeObject(clientStorage);
            writer.flush();
        } catch (IOException e) {
            System.out.println("error, not important");
        }

    }

    private ClientStorage read() throws IOException, ClassNotFoundException {
        return (ClientStorage) reader.readObject();
    }
}
