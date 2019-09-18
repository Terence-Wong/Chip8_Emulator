
 import javax.swing.*;
 import java.awt.event.*;
 import java.awt.*;
public class Chip8GUIunit extends JPanel implements KeyListener{
    public static JFrame frame = new JFrame("Chip8 System");
    public static int HEIGHT = 640, WIDTH = 320;

    public boolean[] key_state = new boolean[255];

    public Chip8GUIunit(){
        frame.setSize(WIDTH,HEIGHT);
        frame.setVisible(true);
        frame.addKeyListener(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void make_sound(){
        System.out.println("Beep. Boop.");
    }

    public void paint(Graphics g){
        super.paint(g);//clears screen
        Graphics2D g2 = (Graphics2D) g;
    
    }

    public void keyPressed(KeyEvent e) {
        byte in = -1;
        if(e.getKeyCode() < 255){ //max return value of .getKeyCode is much higher than byte
            in = (byte)e.getKeyCode();
            key_state[in] = true;
        }
    }

    public void keyReleased(KeyEvent e) {
        byte in = -1;
        if(e.getKeyCode() < 255){ //max return value of .getKeyCode is much higher than byte
            in = (byte)e.getKeyCode();
            key_state[in] = false;
        }
    }

    public void keyTyped(KeyEvent e){
        System.out.println(e.getKeyChar());
    }
}