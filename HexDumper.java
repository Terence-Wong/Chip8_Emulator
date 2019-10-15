import java.io.*;

public class HexDumper{

    static byte[] memory = new byte[0xFFF]; // 4095 bytes Game Memory
    public static void main(String[]args) throws IOException{
        String filename = "Tic-Tac-Toe [David Winter].ch8";
        try{
            FileInputStream fs = new FileInputStream(filename);
            fs.read(memory,0x200,0xFFF - 0x200);
            fs.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        int programCounter = 0x200;
        while(programCounter < 0xFFF-2){
            short r = 0;
            r |= memory[programCounter] & 0xFF;
            //System.out.print("0x"+Integer.toHexString(r) + " \t");
            r <<= 8;
            r |= memory[programCounter+1] & 0xFF;
            programCounter += 2;

            System.out.println("0x" + Integer.toHexString(r & 0xFFFF));
        }
       
        
    }
}