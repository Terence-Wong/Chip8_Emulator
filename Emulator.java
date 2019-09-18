public class Emulator{
    public static void main(String[] args) {
        Chip8 cpu = new Chip8();
        Chip8GUIunit window = new Chip8GUIunit();
        //emulation loop
        while(true){
            //emulate a single cycle
            //cpu.emulateCycle();
            
            //check draw flag if graphics need to be updated
            if(cpu.draw_flag){


                cpu.draw_flag = false;
            }
            
            //update key presses
            cpu.key_state = window.key_state.clone();
        }
    }
}