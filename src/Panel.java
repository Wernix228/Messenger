import javax.swing.*;
import java.awt.*;

public class Panel extends JPanel {

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        if (!Main.isConnected) {
            g2.drawImage(Main.screen, 0, 0, getWidth(), getHeight(), null);
        }else if(Main.clientScreen != null){
            g2.drawImage(Main.clientScreen, 0, 0, getWidth(), getHeight(), null);
        }
    }

}
