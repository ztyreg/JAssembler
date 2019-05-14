import java.util.*;
import java.io.*;
import javax.swing.*;

public class Assembler {
    public static void main(String[] args) {
        if (args.length < 3)
            System.out.println("Usage:\njava Assembler assemble ${PATH/TO/FILE}\njava Assembler disassemble ${PATH/TO/FILE}");
        /**
         * Usage:
         * java Assembler assemble ${PATH/TO/FILE} hex/bin/coe
         * java Assembler disassemble ${PATH/TO/FILE} asm
         */
		try {
            Assembler asm = new Assembler();
            if (args[0].equalsIgnoreCase("assemble"))
                asm.assemble(args);
            else if (args[0].equalsIgnoreCase("disassemble"))
                asm.disassemble(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    }


    public void assemble(String[] args) throws IOException {
        BufferedReader reader;
        String line;
        AssemblerUtils util = new AssemblerUtils();
        /* Read line */
        reader = new BufferedReader(new FileReader(args[1]));

        /** first pass */
        PrintWriter writer = new PrintWriter("aux", "UTF-8");

        line = reader.readLine();
        while (line != null) {
            writer.println(util.scanLabels(new AssemblyLine(line)));
            line = reader.readLine();
        }
        writer.close();

        /** second pass */
        reader = new BufferedReader(new FileReader("aux"));
        line = reader.readLine();
        AssemblyLine inst = null;

        boolean coe_first = true;
        while (line != null) {
            inst = util.getBin(new AssemblyLine(line));

            if (args[2].equalsIgnoreCase("coe")) inst.printCoe(coe_first);
            coe_first = false;
            if (args[2].equalsIgnoreCase("hex")) inst.printHex();
            if (args[2].equalsIgnoreCase("bin")) inst.printBin();

            line = reader.readLine();
        }
        if (args[2].equalsIgnoreCase("coe")) System.out.print(";");

        reader.close();
    }


    public void disassemble(String[] args) throws IOException {
        BufferedReader reader;
        DisassemblerUtils util = new DisassemblerUtils();
        /* Read line */
        reader = new BufferedReader(new FileReader(args[1]));
        /* first pass */
        String line = reader.readLine();
        String inst = "";

        //while (line != null) {
        //    if (args[2].equalsIgnoreCase("asm")) inst = util.getAssembly(line);
        //    System.out.println(inst);
        //    line = reader.readLine();
        //}


        while (line != null) {
            if (line.contains(";")) line = line.split(";")[0];
            if (args[2].equalsIgnoreCase("coe")) inst = util.coeToBin(line);
            line = reader.readLine();
        }
    }
}
