/*
TODO
complete loadROM function with no input - default rom
change CPUReset function to reset CPU with specified rom - loadROM(with param)

if this doesnt work its cuz the roms probably loading the short datatypes
incorrectly/ dealing with byte opcodes wrong

when doing any arithmetic, convert to int first

the decode errors show opcodes as decimals

check op_DXYN function for inverted x y sprites
*/

import java.util.Stack;
import java.awt.image.RescaleOp;
import java.io.*;

public class Chip8{
    boolean show_opcode_logs = false;


    int save_x;

    short delay_timer = 0, sound_timer = 0;

    public boolean[] key_state = new boolean[16];
    boolean[][] screen_data = new boolean[32][64]; 
    boolean draw_flag = false;
    boolean cpu_lock = false;

    byte[] memory = new byte[0xFFF]; // 4095 bytes Game Memory
    int[] registers = new int[16];    // 8-bit Data Registers (V0 - VF)
    int addressI;                     // 16-bit Address Register I
    int programCounter;               // 16-bit Program Counter
    Stack programStack = new Stack();   // 16-bit program stack

    Chip8GUIunit window = null;

    short[] chip8_font_set = {
        0xF0, 0x90, 0x90, 0x90, 0xF0, // 0  , starts at address 0
        0x20, 0x60, 0x20, 0x20, 0x70, // 1  , 5
        0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2  , 10 ...
        0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
        0x90, 0x90, 0xF0, 0x10, 0x10, // 4
        0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
        0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
        0xF0, 0x10, 0x20, 0x40, 0x40, // 7
        0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
        0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
        0xF0, 0x90, 0xF0, 0x90, 0x90, // A
        0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
        0xF0, 0x80, 0x80, 0x80, 0xF0, // C
        0xE0, 0x90, 0x90, 0x90, 0xE0, // D
        0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
        0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    };

    public Chip8(Chip8GUIunit gui){
        CPUReset();
        window = gui;
    }
    public Chip8(){
        CPUReset();
    }
    public void CPUReset(){
        addressI = 0;
        programCounter = 0x200;
        registers = new int[16];
        screen_data = new boolean[32][64];
        programStack = new Stack();
        draw_flag = false;
        cpu_lock = false;

        sound_timer = 0;
        delay_timer = 0;
        //load in ROM
        for(int i = 0; i < 80; i++){
            memory[i] = (byte)chip8_font_set[i];
        }

        //loadROM();
        //loadROM("invader.bin");
        loadROM("Tic-Tac-Toe [David Winter].ch8");
    }
    private void loadROM(){
        /*
        test draw program
        memory[0x200] = (byte)0x60;
        memory[0x201] = (byte)0x00;
        memory[0x202] = (byte)0xF0;
        memory[0x203] = (byte)0x29;
        memory[0x204] = (byte)0xD5;
        memory[0x205] = (byte)0x55;
        */
        memory[0x200] = (byte)0x60;
        memory[0x201] = (byte)0x00;

        memory[0x202] = (byte)0x22;
        memory[0x203] = (byte)0x08;

        memory[0x204] = (byte)0x60;
        memory[0x205] = (byte)0x05;

        memory[0x206] = (byte)0x22;
        memory[0x207] = (byte)0x08;

        memory[0x208] = (byte)0x00;
        memory[0x209] = (byte)0x00;

        memory[0x20A] = (byte)0xF0;
        memory[0x20B] = (byte)0x29;

        memory[0x20C] = (byte)0xD0;
        memory[0x20D] = (byte)0x05;

        memory[0x20E] = (byte)0x00;
        memory[0x20F] = (byte)0xEE;

        /*memory[0x200] = (byte)0xa3;
        memory[0x201] = (byte)0xd3;*/
    }
    private void loadROM(String filename){
        try{
            FileInputStream fs = new FileInputStream(filename);
            fs.read(memory,0x200,0xFFF - 0x200);
            fs.close();
            System.out.println("hi");
            System.out.println("hi");
            System.out.println("hi");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void emulateCycle(){
        //System.out.println("hi");
        decodeOpcode(fetchNextOpcode());


        //update timers
        update_timers();
    }

    /*
     * function updates delay and sound timers iteratively. Actual cycle timing of executions handled by Emulator
     */
    private void update_timers(){
        if(delay_timer > 0){
            delay_timer--;
        }
        if(sound_timer > 0){
            if(sound_timer == 1){
               make_sound(); 
            }
            sound_timer--;
        }
    }
    
    private void make_sound(){
        if(window != null){
            window.make_sound();
        }
    }

    /*
     * chip8 opcodes are 8-bits long, function is combining 2 4-bit addresses from memory
     */

    private short fetchNextOpcode(){
        short r = 0;
        r |= memory[programCounter] & 0xFF;
        //System.out.print("0x"+Integer.toHexString(r) + " \t");
        r <<= 8;
        r |= memory[programCounter+1] & 0xFF;


        if(show_opcode_logs){
            System.out.print("0x"+Integer.toHexString(r & 0xFFFF) + " \t|\t");
            for(int x = 0; x < 8; x++){
                System.out.print("0x"+Integer.toHexString(memory[programCounter+x] & 0xFF) + " ");
            }
            System.out.println("");
            System.out.print("0d"+ (r) + " \t|\t");
            for(int x = 0; x < 8; x++){
                System.out.print("0d"+(memory[programCounter+x]) + " ");
            }
            System.out.println("");
            System.out.println("");
        }
        
        programCounter += 2;
        //System.out.println("0x"+Integer.toHexString(r));
        return r;
    }

    /*
     * function decodes opcode and executes one of
       many opcode functions
     */

    private void decodeOpcode(short opcode){ 
        //System.out.println("0x"+Integer.toHexString(opcode));
        switch(opcode & 0xF000){
            case 0x0000:
                switch(opcode & 0x000F){
                    case 0x0000:
                        op_00E0();
                    break;
                    case 0x000E:
                        op_00EE();
                    break;
                    default:
                        printError("unknown opcode (0NNN): " + opcode, opcode);
                    break;
                }
            break;
            case 0x1000:
                op_1NNN(opcode);
            break;
            case 0x2000:
                op_2NNN(opcode);
            break;
            case 0x3000:
                op_3XNN(opcode);
            break;
            case 0x4000:
                op_4XNN(opcode);
            break;
            case 0x5000:
                op_5XY0(opcode);
            break;
            case 0x6000:
                op_6XNN(opcode);
            break;
            case 0x7000:
                op_7XNN(opcode);
            break;
            case 0x8000:
                switch(opcode & 0x000F){
                    case 0x0000:
                        op_8XY0(opcode);
                    break;
                    case 0x0001:
                        op_8XY1(opcode);
                    break;
                    case 0x0002:
                        op_8XY2(opcode);
                    break;
                    case 0x0003:
                        op_8XY3(opcode);
                    break;
                    case 0x0004:
                        op_8XY4(opcode);
                    break;
                    case 0x0005:
                        op_8XY5(opcode);
                    break;
                    case 0x0006:
                        op_8XY6(opcode);
                    break;
                    case 0x0007:
                        op_8XY7(opcode);
                    break;
                    case 0x000E:
                        op_8XYE(opcode);
                    break;
                    default:
                        printError("unknown opcode: " + opcode, opcode);
                    break;
                }
            break;
            case 0x9000:
                op_9XY0(opcode);
            break;
            case 0xA000:
                op_ANNN(opcode);
            break;
            case 0xB000:
                op_BNNN(opcode);
            break;
            case 0xC000:
                op_CXNN(opcode);
            break;
            case 0xD000:
                op_DXYN(opcode);
            break;
            case 0xE000:
                switch(opcode & 0x000F){
                    case 0x000E:
                        op_EX9E(opcode);
                    break;
                    case 0x0001:
                        op_EXA1(opcode);
                    break;
                    default:
                        printError("unknown opcode: " + opcode, opcode);
                    break;
                }
            break;
            case 0xF000:
                switch(opcode & 0x00FF){
                    case 0x0007:
                        op_FX07(opcode);
                    break;
                    case 0x000A:
                        op_FX0A(opcode);
                    break;
                    case 0x0015:
                        op_FX15(opcode);
                    break;
                    case 0x0018:
                        op_FX18(opcode);
                    break;
                    case 0x001E:
                        op_FX1E(opcode);
                    break;
                    case 0x0029:
                        op_FX29(opcode);
                    break;
                    case 0x0033:
                        op_FX33(opcode);
                    break;
                    case 0x0055:
                        op_FX55(opcode);
                    break;
                    case 0x0065:
                        op_FX65(opcode);
                    break;
                    default:

                        printError("unknown opcode: " + opcode, opcode);
                    break;
                }
            break;
            default:
                printError("unknown opcode: " + opcode, opcode);
            break;
        }
    }

    //Clears the screen. 
    private void op_00E0(){
        draw_flag = true;
        screen_data = new boolean[32][64]; 
    }
    //Returns from a subroutine. 
    private void op_00EE(){
        programCounter = (Integer)programStack.pop();
        programCounter += 2;
    }
    //
    private void op_1NNN(short opcode){
        programCounter = opcode & 0x0FFF;
    }
    // Calls subroutine at NNN. 
    private void op_2NNN(short opcode){
        programStack.push(programCounter-2);
        programCounter = opcode & 0x0FFF;
    }
    // 3/5 check
    private void op_3XNN(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int nn = (opcode & 0x00FF);
        if ((registers[x] & 0xFF) == nn){
            programCounter += 2;//
        }
    }
    // 3/5 check
    private void op_4XNN(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int nn = (opcode & 0x00FF);
        if ((registers[x] & 0xFF) != nn){
            programCounter += 2;//
        }
    }
    //3/5 check
    private void op_5XY0(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        if ((registers[x] & 0xFF) == (registers[y] & 0xFF)){
            programCounter += 2;
        }
    }

    //Sets VX to NN. 
    private void op_6XNN(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        byte nn = (byte)(opcode & 0x00FF);
        registers[x] = nn;
    }
    //3/5 check
    private void op_7XNN(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        byte nn = (byte)(opcode & 0x00FF);
        registers[x] = (registers[x] & 0xFF) + (nn & 0xFF);
    }
    // 2.5/5 check
    private void op_8XY0(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        registers[x] = registers[y];
    }
    // 2.5/5 check
    private void op_8XY1(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        registers[x] |= registers[y];  //bit operation might not work in byte form,nvm works
    }
    // 2.5/5 check
    private void op_8XY2(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        registers[x] &= registers[y];  //bit operation might not work in byte form
    }
    // 2.5/5 check
    private void op_8XY3(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        registers[x] ^= registers[y];  //bit operation might not work in byte form
    }
    // 1.5 check
    private void op_8XY4(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        int z = (registers[x] & 0xFF) + (registers[y] & 0xFF);
        registers[0xF] = 0;
        if(z > 255){
            registers[0xF] = 1;
        }
        //possible problem fitting large number into x
        registers[x] = (byte)(z);
    }
    //1/5 check
    private void op_8XY5(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        int z = (registers[x] & 0xFF) - (registers[y] & 0xFF);
        registers[0xF] = 1;
        if((registers[y] & 0xFF) > (registers[x] & 0xFF)){
            registers[0xF] = 0;
            z += 255;
        }
        registers[x] = (byte)z;
    }
    // 2.5 check
    private void op_8XY6(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        //int y = (opcode & 0x00F0) >> 4; y is unused
        registers[0xF] = (byte)(registers[x] & 0x01);
        registers[x] = registers[x] >>> 1;
    }
    //1/5 check
    private void op_8XY7(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        int z = (registers[y] & 0xFF) - (registers[x] & 0xFF);
        registers[0xF] = 1;
        if((registers[x] & 0xFF) > (registers[y] & 0xFF)){
            registers[0xF] = 0;
            z += 255;
        }
        registers[x] = (byte)z;
    }
    //2.5 check
    private void op_8XYE(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        registers[0xF] = (registers[x] & 0x80) >>> 7;
        registers[x] = (registers[x] & 0xFF) << 1;
    }
    //1/5 check
    private void op_9XY0(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        if ((registers[x] & 0xFF) != (registers[y] & 0xFF)){
            programCounter += 2;
        }
    }
    //check 2/5
    private void op_ANNN(short opcode){
        addressI = (short)(opcode & 0x0FFF);
    }
    //check 1.5/5
    private void op_BNNN(short opcode){
        programCounter = (opcode & 0x0FFF) + (registers[0] & 0xFF);
    }
    //0.5/check
    private void op_CXNN(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        byte nn = (byte)(opcode & 0x00FF);
        registers[x] = ((int) Math.round(Math.random()*(255))) & nn;
    }
    // draws I's sprite at (X,Y) of height N  /// hard check sometime?
    private void op_DXYN(short opcode){;

        draw_flag = true;
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        int n = (opcode & 0x000F);


        System.out.println("draw called \nx: " + x + "\ny:" + y + "\nh: " + n);

        registers[0xF] = 0;
        for(int row = 0; row < n; row++){
            byte data = memory[(addressI & 0xFFFF) + row];
            
            for(int column = 0; column < 8; column++){
                int mask = 1 << (7-column);
                if( ( (data & mask) >>> (7-column) ) == 1){
                    int coordx = registers[x] + column;
                    int coordy = registers[y] + row;

                    if(coordx >= screen_data[0].length){
                        coordx -= screen_data[0].length;
                    }
                    if(coordy >= screen_data.length){
                        coordy -= screen_data.length;
                    }

                    if(screen_data[coordy][coordx]){  /// might be inverted
                        registers[0xF] = 1;
                    }
                    screen_data[coordy][coordx] = !screen_data[coordy][coordx];
                }
            }
        }

        //System.out.println("DXYN called");
    }

    private void op_EX9E(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        if(key_state[(registers[x] & 0xFF)]){ //
            programCounter += 2;
        }
    }

    private void op_EXA1(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        if(!key_state[(registers[x] & 0xFF)]){   ////
            programCounter += 2;
        }
    }
    //
    private void op_FX07(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        registers[x] = (delay_timer & 0xFFFF);// % Byte.MAX_VALUE); // no documentation, assume correct implementation. Error converting short to byte otherwise
    }
    private void op_FX0A(short opcode){
        cpu_lock = true;
        save_x = (opcode & 0x0F00) >> 8;
        System.out.println("FXOA called");
    }
    private void op_FX15(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        delay_timer = (short)(registers[x] & 0xFF);
    }
    private void op_FX18(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        sound_timer = (short)(registers[x] & 0xFF);
    }
    // f-ing works
    private void op_FX1E(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        addressI = ( (addressI & 0xFFFF) + (registers[x] & 0xFF) ); // check for carry flag?
    }
    // //sets I to v0's character in font set
    private void op_FX29(short opcode){
        int x = (opcode & 0x0F00) >> 8;

        if(0 <= (registers[x] & 0xFF) && (registers[x] & 0xFF) <= 0xF){
            addressI = ((registers[x] & 0xFF)*5);
        }else{
            System.out.println("(FX29) unrecognized character: " + (char)(registers[x] & 0xFF));
            //outside character set in chip 8 (0-F)
        }
    }
    //f-ing works
    private void op_FX33(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int decimal = registers[x] & 0xFF;
        memory[(addressI & 0xFFFF)]     = (byte) ( (decimal / 100) % 10 );
        memory[(addressI & 0xFFFF) +1]     = (byte) ( (decimal / 10 ) % 10 );
        memory[(addressI & 0xFFFF) +2]     = (byte) ( (decimal      ) % 10 );  
    }
    //f-ing works
    private void op_FX55(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        for(int i = 0; i <= x; i++){
            memory[(addressI & 0xFFFF) + i] = (byte)(registers[i] & 0xFF);
        }
    }
    //f-ing works
    private void op_FX65(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        for(int i = 0; i <= x; i++){
            registers[i] = memory[(addressI & 0xFFFF) + i];
        }
    }



    public void printError(String e, short opcode){
        System.out.println(e);
        System.out.println("0x"+Integer.toHexString(opcode & 0xFFFF) + "");
        System.out.println("");
    }

}


