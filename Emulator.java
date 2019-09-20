public class Emulator{
    public static void main(String[] args) throws InterruptedException{
        Chip8GUIunit window = new Chip8GUIunit();
        Chip8 cpu = new Chip8(window);

        //emulation loop
        while(true){
            //cpu locking mechanism for op_FX0A
            if(window.key_update_flag){
                window.key_update_flag = false;
                cpu.cpu_lock = false;
                //if last called opcode is op_FX0A
                cpu.registers[cpu.save_x] = window.latest_key;
            }
            if(!cpu.cpu_lock){
                //emulate a single cycle
                cpu.emulateCycle();
                //check draw flag if graphics need to be updated
                if(cpu.draw_flag){
                    cpu.draw_flag = false;
                }
                
                //update key presses
                cpu.key_state = window.key_state; //window.key_state.clone();
            }
            Thread.sleep(2);
        }
    }
}