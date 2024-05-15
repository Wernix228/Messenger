import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static BufferedImage screen;
    public static boolean programIsActive = true;
    public static boolean isConnected = false;
    public static BufferedImage clientScreen;
    public static String ownMessage = null;
    public static String clientMessage = null;

    private static JFrame frame;
    private static int screenWidth;
    private static int screenHeight;
    private static Robot robot;
    private static Panel panel;
    private static JTextArea messageArea;
    private static JTextField messageOutput;
    private static JTextField name;
    private static final int FPS = 30;

    public static void main(String[] args) throws AWTException, UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        createFrame();

        frame.repaint();

        start();
    }

    public static void start() {

        double drawInterval = (double) 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (true) {
            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / drawInterval;

            lastTime = currentTime;

            if (delta >= 1) {
                render();
                delta--;
            }
        }

    }

    private static void render() {
        screen = robot.createScreenCapture(new Rectangle(0, 0, screenWidth, screenHeight));
        //-------------------------------------------
        double scaleX = screenWidth/panel.getWidth();
        double scaleY = screenHeight/panel.getHeight();
        Graphics2D gr = (Graphics2D)screen.getGraphics();
        gr.scale(scaleX,scaleY);
        gr.dispose();
        

        if(clientMessage!=null){
            messageArea.insert(clientMessage, 0);
            clientMessage = null;
        }

        panel.repaint();
    }
    private static void createFrame() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, AWTException{
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        screenWidth = screenSize.width;
        screenHeight = screenSize.height;

        robot = new Robot();

        frame = new JFrame();

        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(100, 100, 1120, 630);
        frame.setLayout(null);

        panel = new Panel();
        panel.setBounds(10, 10, (int) (frame.getWidth()/1.5), (int) (frame.getHeight()/1.5));

        frame.add(panel);

        JButton button = new JButton("Quit");

        button.setBounds(10, (int) (frame.getHeight()/1.5) + 20, 150, 100);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                programIsActive = false;
                System.exit(0);
            }
        });

        JButton host = new JButton("HOST");
        JButton connect = new JButton("CONNECT");

        JTextField textField = new JTextField();

        host.setBounds(button.getX() + 160, button.getY(), 150, 100);
        connect.setBounds(button.getX() + 320, button.getY(), 150, 100);
        textField.setBounds(button.getX() + 480, button.getY(), 150, 100);

        messageArea = new JTextArea();

        messageArea.setVisible(false);
        messageArea.setBounds((int) ((frame.getWidth()/1.5) + 20), 50, 300, (int) (frame.getHeight()/1.5));
        messageArea.setEditable(false);

        messageOutput = new JTextField();
        messageOutput.setBounds(messageArea.getX(), messageArea.getY() + messageArea.getHeight() + 40, 300, 30);
        messageOutput.setVisible(false);

        name = new JTextField();
        name.setBounds(messageArea.getX(), messageArea.getY()-40, 300, 30);
        name.setVisible(false);

        host.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ServerSocket serverSocket = new ServerSocket(Integer.parseInt(textField.getText()));

                    Server server = new Server(serverSocket);
                    server.start();
                    connect.setVisible(false);
                    host.setVisible(false);
                    textField.setVisible(false);
                    messageArea.setVisible(true);
                    messageOutput.setVisible(true);
                    name.setVisible(true);
                } catch (IOException | LineUnavailableException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] ipport = textField.getText().split(":");
                try {
                    Socket socket = new Socket(ipport[0], Integer.parseInt(ipport[1]));
                    Client client = new Client(socket);
                    client.start();
                    connect.setVisible(false);
                    host.setVisible(false);
                    textField.setVisible(false);
                    messageArea.setVisible(true);
                    messageOutput.setVisible(true);
                    name.setVisible(true);
                } catch (IOException | LineUnavailableException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        frame.add(button);
        frame.add(host);
        frame.add(connect);
        frame.add(textField);
        frame.add(messageArea);
        frame.add(messageOutput);
        frame.add(name);

        messageOutput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ownMessage = name.getText() + ": " + messageOutput.getText() + "\n";
                messageArea.insert(ownMessage, 0);
                messageOutput.setText("");
            }
        });
    }

}
