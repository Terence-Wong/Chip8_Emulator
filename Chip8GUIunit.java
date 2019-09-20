
 import javax.swing.*;
 import java.awt.event.*;
 import java.awt.*;
public class Chip8GUIunit extends JFrame implements KeyListener{
    //public static JFrame frame = new JFrame("Chip8 System");
    public Panel GUIPanel;
    public int HEIGHT = 320, WIDTH = 640;

    public boolean key_update_flag = false;
    public boolean[] key_state = new boolean[100];
    public byte latest_key;

    public Chip8GUIunit(){
        GUIPanel = new Panel(WIDTH,HEIGHT);
        GUIPanel.addKeyListener(this);
        this.setSize(WIDTH,HEIGHT);
        this.setVisible(true);
        this.addKeyListener(this);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void update_screen(){
        GUIPanel.repaint();
    }

    public void make_sound(){
        System.out.println("Buzzzzzz...");
    }

    public void keyPressed(KeyEvent e) {
        byte in = -1;
        if(e.getKeyCode() < 255){ //max return value of .getKeyCode is much higher than byte
            in = (byte)e.getKeyCode();
            key_state[in] = true;
            latest_key = (byte)e.getKeyCode();
            System.out.println((char)in);
            key_update_flag = true;
        }
    }

    public void keyReleased(KeyEvent e) {
        byte in = -1;
        if(e.getKeyCode() < 255){ //max return value of .getKeyCode is much higher than byte
            in = (byte)e.getKeyCode();
            key_state[in] = false;
            //System.out.println((char)in);
        }
    }

    public void keyTyped(KeyEvent e){}
}
class Panel extends JPanel{
    Panel(int x, int y) {
        this.setSize(x,y);
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Other painting stuff
    }
}