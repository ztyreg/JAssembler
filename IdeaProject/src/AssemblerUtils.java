import java.util.*;
import java.lang.*;
import java.io.*;

public class AssemblerUtils {
    //TODO you can define a line class
    private Map<String, Long> labels = new HashMap<String, Long>();
    private long pc = 0;
    /* pc for expansion */
    private Map<String, List<String>> pseudo_map = new HashMap<String, List<String>>();
    private static final String COMMA_DELIMITER = ",";
    private static final String [] MIPS_PSEUDO = {"./mips_pseudo.csv"};
    private List<Long> base_addr = new ArrayList<>();
    private int num_bases = 0;
    private List<Long> data_addr = new ArrayList<>();
    private List<Long> pc_list = new ArrayList<>();
    private boolean is_first_instruction = true;
    private long final_pc = 0;

    public AssemblerUtils() {
        String line;
        BufferedReader br;
        try {
            for (String f:MIPS_PSEUDO) {
                br = new BufferedReader(new FileReader(f));
                /* header */
                br.readLine();
                /* instructions */
                while ((line = br.readLine()) != null) {
                    String [] values = line.split(COMMA_DELIMITER);
                    pseudo_map.put(values[0], Arrays.asList(values).subList(1, values.length));
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public List<Long> getBases() {
        return base_addr;
    }

    public List<Long> getPC() {
        return pc_list;
    }

    public long getInitPC() {return base_addr.get(0);}

    public long getFinalPC() {return final_pc;}

    public List<Long> getDataAddress() { return data_addr; }

    public AssemblyLine getBin(AssemblyLine al) {
        String [] tokens = al.getTokens();
        if (tokens.length == 0) return al;
        String type = al.getType();
        if (type.length() == 0) return al;

        /* get binary instruction */
        Instructions inst = new Instructions();
        if (inst.isMIPS(tokens)) {
            String bin_inst = inst.getBinary(tokens, labels);
            al.addBinary(bin_inst);

        } else if (type.equalsIgnoreCase("db") ||
                type.equalsIgnoreCase("dw") ||
                type.equalsIgnoreCase("dd")) {
            al = addData(al, tokens);
        } else {
            System.out.println(type + ": Instruction not found!");
        }

        return al;
    }

    private AssemblyLine addData(AssemblyLine al, String [] tokens) {
        /* data is already patched */
        /* only need to convert data to bit string */
        int byte_count = 0;
        for (int i = 1; i < tokens.length; i++) { /* for each data field */
            if (tokens[i].contains("\'")) {
                /* string literal */
                String s = tokens[i].replace("\'", "");
                for (int j = 0; j < s.length(); j++){
                    char c = s.charAt(j);        
                    String b = intToString8Bits((int)c);
                    al.addBinary(b);
                    byte_count += 1;
                }

            } else {
                /* numbers */
                String s = tokens[i];
                /* 2 numbers - 8 bits */
                if (s.contains("0x")) s = s.split("0x")[1];
                for (int j = s.length() - 2; j >= 0; j -= 2) {
                    int m = Integer.parseInt(s.substring(j, j+2), 16);
                    String b = intToString8Bits(m);
                    al.addBinary(b);
                    byte_count += 1;
                }
            }
        }
//
//        /* patch to 32 bits */
//        int rem = byte_count % 4;
//        if (rem > 0) {
//            for (int j = 0; j < 4 - rem; j++) {
//                al.addBinary("00000000");
//            }
//        }

        return al;
    }
    

    public String scanLabels(AssemblyLine al) {
        final_pc = pc;
        System.out.print(Long.toString(pc, 16) + "\t");
        System.out.println(al.getLine());

        /* put labels */
        String fill_zeros = "";
        if (al.hasLabel()) {
            String lb = al.getLabel();
            if (lb.equalsIgnoreCase("BaseAddre")) {
                num_bases++;
                /* there can be more than one baseAddre */
                long addre = Long.parseLong(al.getType(), 16);
                /* used in the editor to print address */
                if (pc == 0) {
                    /* Starting address */
                    pc = addre;
                    base_addr.add(pc);
                    return "";
                } else {
                    if (pc > addre) return "Error: address must be greater than program counter!";
                    while (pc < addre) {
                        fill_zeros += "db 00;" + System.lineSeparator();
                        pc += 1;
                    }
                    base_addr.add(addre);
                    return fill_zeros;
                }
            } else if (lb.equalsIgnoreCase("DataAddre")) {
                long addre = Long.parseLong(al.getType(), 16);
                if (pc == 0) {
                    /* Starting address */
                    pc = addre;
                    data_addr.add(pc);
                    return "";
                } else {
                    if (pc > addre) return "Error: address must be greater than program counter!";
                    while (pc < addre) {
                        fill_zeros += "db 00;" + System.lineSeparator();
                        pc += 1;
                    }
                    data_addr.add(addre);
                    return fill_zeros;
                }
            } else {
                labels.put(lb, pc);
                System.out.println("Label:\t" + lb + "\tat " + pc);
            }
        }

        if (pc == 0 && al.isInstruction() && is_first_instruction) {
            /* default */
            base_addr.add(pc);
            is_first_instruction = false;
        }

        if (! al.isInstruction()) return al.getLine();

        String [] tokens = al.getTokens();
        String type = tokens[0];

        /* Data pseudoinstructions */
        long inc;
        String ret = "";
        switch (type.toLowerCase()) {
            case "equ":
                if (tokens.length == 3) {
                    labels.put(tokens[0], Long.parseLong(tokens[2]));
                    System.out.println("Constant:\t" + tokens[0] + "\tis " + tokens[2]);
                    return "";
                }
            case "resb":
                /* iniitalize uninitialized spaces to 0 */
                // TODO
                inc = Integer.parseInt(tokens[1], 10);
                for (int i = 0; i < inc; i++) {
                    pc += 1;
                    ret += System.lineSeparator() + "db 00";
                }
                return ret;
            case "resw":
                // TODO
                inc = Integer.parseInt(tokens[1], 10);
                for (int i = 0; i < inc; i++) {
                    pc += 2;
                    ret += System.lineSeparator() + "dw 0000";
                }
                return ret;
            case "resd":
                // TODO
                inc = Integer.parseInt(tokens[1], 10);
                for (int i = 0; i < inc; i++) {
                    pc += 4;
                    ret += System.lineSeparator() + "dd 00000000";
                }
                return ret;
            case "db":
            case "dw":
            case "dd":
                inc = dataInstructionLengthInBytes(tokens);
                ret = patchDataInstruction(tokens);
                pc += inc;
                return ret;
            default:
                pc_inc(4);
        }

        /** MIPS instructions */
        List<String> fields = pseudo_map.get(type);
        if (fields == null) return al.getLine();

        /** MIPS pseudoinstructions */
        String expansion = "";
        for (String a:fields) {
            if (a.equals("ARG1")) expansion += " " + tokens[1];
            else if (a.equals("ARG2")) expansion += " " + tokens[2];
            else if (a.equals("ARG3")) expansion += " " + tokens[3];
            else if (a.equals("NEWLINE")) {
                /* pseudoinstruction expanded to more than 1 instruction */
                pc_inc(4);
                expansion += System.lineSeparator();
            } else if (a.equals("LABEL+1")) {
                labels.put("_LABEL_" + (pc + 4), pc + 4);
                expansion += " " + "_LABEL_" + (pc + 4);
            } else if (a.equals("LABEL+2")) {
                labels.put("_LABEL_" + (pc + 8), pc + 8);
                expansion += " " + "_LABEL_" + (pc + 8);
            } else if (a.equals("LABEL+3")) {
                labels.put("_LABEL_" + (pc + 12), pc + 12);
                expansion += " " + "_LABEL_" + (pc + 12);
            }
            else expansion += " " + a;
        }
        return expansion;
    }

    private void pc_inc(long n)
    {
        pc += n;
        pc_list.add(pc);
    }

    private void pc_set(long n)
    {
        pc = n;
        pc_list.add(pc);
    }

    public String intToString8Bits(int n)
    {
        String b = Integer.toBinaryString(n);
        int num_zeros = 8 - b.length();
        for (int k = 0; k < num_zeros; k++) {
            b = "0" + b;
        }
        return b;
    }

    public String longToHexString8Bits(long n)
    {
        String b = Long.toString(n, 16);
        int num_zeros = 8 - b.length();
        for (int k = 0; k < num_zeros; k++) {
            b = "0" + b;
        }
        return b;
    }

    private static int dataInstructionLengthInBytes(String [] tokens)
    {
        int num_bytes = 0;

        if (tokens.length < 2) return 0;

        int unit_bytes = 0;
        switch (tokens[0].toLowerCase()) {
            case "db":
                unit_bytes = 1; break;
            case "dw":
                unit_bytes = 2; break;
            case "dd":
                unit_bytes = 4; break;
        }

        for (int i = 1; i < tokens.length; i++) {
            String s = tokens[i];
            int inc = 0;
            if (s.contains("\'")) { /* String literal */
                s = s.replace("\'", "");
                inc = s.length();
                if (inc % unit_bytes != 0) inc = inc / unit_bytes * unit_bytes + unit_bytes; /* patch to unit_bytes */
                num_bytes += inc;
            } else { /* hex number */
                /* support two formats: 0x12 and 12 */
                if (s.contains("0x")) s = s.replace("0x", "");
                inc = (s.length() + 1) / 2;
                if (inc % unit_bytes != 0) inc = inc / unit_bytes * unit_bytes + unit_bytes; /* patch to unit_bytes */
                num_bytes += inc;
            }
        }
        return num_bytes;
    }

    private static String patchDataInstruction(String [] tokens)
    {
        if (tokens.length < 2) return "";

        int unit_bytes = 0;
        switch (tokens[0].toLowerCase()) {
            case "db":
                unit_bytes = 1; break;
            case "dw":
                unit_bytes = 2; break;
            case "dd":
                unit_bytes = 4; break;
        }

        StringBuilder ret = new StringBuilder(tokens[0] + " ");
        for (int i = 1; i < tokens.length; i++) {
            String s = tokens[i];
            if (s.contains("\'")) { /* String literal */
                s = s.replace("\'", "");
                ret.append("'");
                ret.append(s);
                if (s.length() % unit_bytes != 0) {
                    /* add trailing 0's */
                    for (int j = 0; j < unit_bytes - s.length() % unit_bytes; j++) {
                        ret.append("0");
                    }
                }
                ret.append("'");
                if (i < tokens.length - 1) ret.append(", ");
            } else { /* hex number */
                int unit_chars = 2 * unit_bytes;
                if (s.contains("0x")) s = s.replace("0x", "");
                /* add preceding 0's */
                if (s.length() % unit_chars != 0) {
                    /* add trailing 0's */
                    for (int j = 0; j < unit_chars - s.length() % unit_chars; j++) {
                        ret.append("0");
                    }
                }
                ret.append(s);
                if (i < tokens.length - 1) ret.append(", ");
            }
        }

        return ret.toString();

    }

}


