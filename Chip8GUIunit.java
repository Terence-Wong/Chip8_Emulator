
 import javax.swing.*;
 import java.awt.event.*;
 import java.awt.*;
public class Chip8GUIunit extends JFrame implements KeyListener{
    public MyPanel GUIPanel;
    public int HEIGHT = 320, WIDTH = 640;

    public boolean key_update_flag = false;
    public boolean[] key_state = new boolean[16];
    public byte latest_key;

    public Chip8GUIunit(){
        GUIPanel = new MyPanel(WIDTH,HEIGHT);
        GUIPanel.addKeyListener(this);
        this.add(GUIPanel);
        this.setSize(WIDTH,HEIGHT);
        this.setVisible(true);
        this.addKeyListener(this);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void update_screen(boolean[][] screen_data){
        GUIPanel.draw_screen(screen_data);
    }

    public void make_sound(){
        System.out.println("Buzzzzzz...");
    }

    public void keyPressed(KeyEvent e) {
        byte in = -1;
        if((e.getKeyCode() >= (byte)'0' && e.getKeyCode() <= (byte)'9') ) { //max return value of .getKeyCode is much higher than byte
            in = (byte)(e.getKeyCode()-(int)'0');
            key_state[in] = true;
            latest_key = in;
            //System.out.println((char)in);
            key_update_flag = true;
        }
        if(e.getKeyCode() >= (byte)'A' && e.getKeyCode() <= (byte)'F'){
            in = (byte)(e.getKeyCode()-(int)'A' + 10);
            key_state[in] = true;
            latest_key = in;
            //System.out.println((char)in);
            key_update_flag = true;
        }
    }

    public void keyReleased(KeyEvent e) {
        byte in = -1;
        if((e.getKeyCode() >= (byte)'0' && e.getKeyCode() <= (byte)'9') ) { //max return value of .getKeyCode is much higher than byte
            in = (byte)(e.getKeyCode()-(int)'0');
            key_state[in] = false;
            latest_key = (byte)e.getKeyCode();
        }
        if((e.getKeyCode() >= (byte)'A' && e.getKeyCode() <= (byte)'F')){
            in = (byte)(e.getKeyCode()-(int)'A' + 10);
            key_state[in] = false;
            latest_key = (byte)e.getKeyCode();
        }
    }

    public void keyTyped(KeyEvent e){}
}
class MyPanel extends JPanel{
    boolean[][] screen_data = new boolean[1][1];
    Graphics g;
    int HEIGHT, WIDTH;
    MyPanel(int x, int y) {
        WIDTH = x;
        HEIGHT = y;
        this.setSize(x,y);
    }
    public void draw_screen(boolean[][] screen_data){
        this.screen_data = screen_data;
        repaint();
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Other painting stuff

        int pixel_height = HEIGHT/screen_data.length;
        int pixel_width = WIDTH/screen_data[0].length;


        for(int x = 0; x < screen_data[0].length; x++){
            for(int y = 0; y < screen_data.length; y++){
                if(screen_data[y][x]){
                    //System.out.println(x + " " + y);
                    g.fillRect(x*pixel_width, y*pixel_height, pixel_width, pixel_height);
                }
            }
        }
    }
}