
import java.awt.*;
import javax.swing.*;

public class Emulator{
    static Chip8GUIunit window = new Chip8GUIunit();
    static Chip8 cpu;
    public static void main(String[] args) throws InterruptedException{
        cpu = new Chip8(window);
        //emulation loop
        while(true){
            //cpu locking mechanism for op_FX0A
            if(window.key_update_flag){
                
                cpu.cpu_lock = false;
                //if last called opcode is op_FX0A
                cpu.registers[cpu.save_x] = window.latest_key;
            }
            window.key_update_flag = false;
            if(!cpu.cpu_lock){
                //emulate a single cycle
                cpu.emulateCycle();
                //check draw flag if graphics need to be updated

                if(cpu.draw_flag){
                    cpu.draw_flag = false;
                    window.update_screen(cpu.screen_data);
                }
                
                //update key presses
                cpu.key_state = window.key_state;
            }
            Thread.sleep(3);
        }
    }
}