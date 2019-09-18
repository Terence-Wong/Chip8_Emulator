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

    short delay_timer = 0, sound_timer = 0;

    public boolean[] key_state = new boolean[255];
    boolean[][] screenData = new boolean[64][32]; 
    boolean draw_flag = false;

    byte[] ro_Memory = new byte[0xFFF]; // 4095 bytes of Read-Only Game Memory
    byte[] registers = new byte[16];    // 8-bit Data Registers (V0 - VF)
    short addressI;                     // 16-bit Address Register I
    int programCounter;               // 16-bit Program Counter
    Stack programStack = new Stack();   // 16-bit program stack



    public Chip8(){
        CPUReset();
        
    }
    public void CPUReset(){
        addressI = 0;
        programCounter = 0x200;
        registers = new byte[16];
        screenData = new boolean[64][32];
        programStack = new Stack();
        draw_flag = false;

        sound_timer = 0;
        delay_timer = 0;
        //load in ROM
        loadROM();
    }
    private void loadROM(){

    }
    private void loadROM(String filename){
        try{
            FileInputStream fs = new FileInputStream(filename);
            fs.read(ro_Memory,0x200,0xFFF);
            fs.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void emulateCycle(){
        decodeOpcode(fetchNextOpcode());

        //update timers

        //sleep?
    }

    /*
     * chip8 opcodes are 8-bits long, function is combining 2 4-bit addresses from memory
     */

    private short fetchNextOpcode(){
        short r = ro_Memory[programCounter];
        r <<= 8;
        r |= ro_Memory[programCounter+1];
        programCounter += 2;
        return r;
    }

    /*
     * function decodes opcode and executes one of many opcode functions
     */

    private void decodeOpcode(short opcode){ 
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
                        printError("unknown opcode: " + opcode);
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
                        printError("unknown opcode: " + opcode);
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
                        printError("unknown opcode: " + opcode);
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
                        printError("unknown opcode: " + opcode);
                    break;
                }
            break;
            default:
                printError("unknown opcode: " + opcode);
            break;
        }
    }
    
    /*
     * function updates delay and sound timers iteratively. Actual cycle timing of executions handled by emulateCycle function
     */
    private void updateTimers(){
        if(delay_timer > 0){
            delay_timer--;
        }
        if(sound_timer > 0){
            if(sound_timer == 1){
               window.make_sound(); 
            }
            sound_timer--;
        }
    }
    
    private void op_00E0(){
        draw_flag = false;
        screenData = new boolean[64][32]; 
    }
    private void op_00EE(){
        programCounter = (Integer)programStack.pop();
    }
    private void op_1NNN(short opcode){
        programCounter = opcode & 0x0FFF;
    }
    private void op_2NNN(short opcode){
        programStack.push(programCounter);
        programCounter = opcode & 0x0FFF;
    }
    private void op_3XNN(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        byte nn = (byte)(opcode & 0x00FF);
        if (registers[x] == nn){
            programCounter += 2;
        }
    }
    private void op_4XNN(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        byte nn = (byte)(opcode & 0x00FF);
        if (registers[x] != nn){
            programCounter += 2;
        }
    }
    private void op_5XY0(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        if (registers[x] == registers[y]){
            programCounter += 2;
        }
    }
    private void op_6XNN(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        byte nn = (byte)(opcode & 0x00FF);
        registers[x] = nn;
    }
    private void op_7XNN(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        byte nn = (byte)(opcode & 0x00FF);
        registers[x] = (byte)((registers[x] & 0xFF) + (nn & 0xFF));
    }
    private void op_8XY0(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        registers[x] = registers[y];
    }
    private void op_8XY1(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        registers[x] |= registers[y];
    }
    private void op_8XY2(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        registers[x] &= registers[y];
    }
    private void op_8XY3(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        registers[x] ^= registers[y];
    }
    private void op_8XY4(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        int z = (registers[x] & 0xFF) + (registers[y] & 0xFF);
        registers[0xF] = 0;
        if(z > 255){
            registers[0xF] = 1;
        }
        registers[x] = (byte)z;
    }
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
    private void op_8XY6(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        registers[0xF] = (byte)(registers[x] & 0x01);
        registers[x] >>= 1;
    }
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
    private void op_8XYE(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        registers[0xF] = (byte)(registers[x] & 0x80 >> 8);
        registers[x] <<= 1;
    }
    private void op_9XY0(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        if (registers[x] != registers[y]){
            programCounter += 2;
        }
    }
    private void op_ANNN(short opcode){
        addressI = (short)(opcode & 0x0FFF);
    }
    private void op_BNNN(short opcode){
        programCounter = opcode & 0x0FFF + registers[0];
    }
    private void op_CXNN(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        byte nn = (byte)(opcode & 0x00FF);
        registers[x] = (byte)(((byte) (Math.random()*(255))-128) & nn);
    }
    private void op_DXYN(short opcode){
        draw_flag = true;
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        int n = (opcode & 0x000F);
        registers[0xF] = 0;
        for(int row = 0; row < n; row++){
            byte data = ro_Memory[addressI + row];
            for(int column = 0; column < 8; column++){
                int mask = 1 << (7-column);
                if((data & mask >> (7-column) )== 1){
                    int coordx = registers[x] + column;
                    int coordy = registers[y] + row;
                    if(screenData[coordy][coordx]){  /// might be inverted
                        registers[0xF] = 1;
                    }
                    screenData[coordy][coordx] = !screenData[coordy][coordx];
                }
            }
        }
    }
    private void op_EX9E(short opcode){}
    private void op_EXA1(short opcode){}
    private void op_FX07(short opcode){}
    private void op_FX0A(short opcode){

    }
    private void op_FX15(short opcode){

    }
    private void op_FX18(short opcode){
        
    }
    private void op_FX1E(short opcode){
        int x = (opcode & 0x0F00) >> 8;
        addressI += registers[x]; // check for carry flag?
    }
    private void op_FX29(short opcode){
        
    }
    private void op_FX33(short opcode){}
    private void op_FX55(short opcode){}
    private void op_FX65(short opcode){}



    public void printError(String e){
        System.out.println(e);
    }

}


