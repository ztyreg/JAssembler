import java.util.*;
import java.io.*;

public class Instructions {
    /**
     * This class is used to translate
     * MIPS (real) insructions to binary machine code
     * and vise versa
     */
    private Map<String, List<String>> int_map = new HashMap<String, List<String>>();
    private List<List<String>> regex_map = new ArrayList<List<String>>();
    private static final String COMMA_DELIMITER = ",";
    /* instructions can be put in separate files */
    private static final String [] MIPS_INT = {"./mips_int.csv"};
    /* regular expression for MIPS instructions */
    private static final String [] MIPS_REGEX = {"./mips_regex.csv"};

    public Instructions() {
        String line;
        BufferedReader br;
        try {
            for (String f:MIPS_INT) {
                br = new BufferedReader(new FileReader(f));
                /* header */
                br.readLine();
                /* instructions */
                while ((line = br.readLine()) != null) {
                    String [] values = line.split(COMMA_DELIMITER);
                    int_map.put(values[0], Arrays.asList(values).subList(1, values.length));
                }
            }
            for (String f:MIPS_REGEX) {
                br = new BufferedReader(new FileReader(f));
                /* header */
                br.readLine();
                /* instructions */
                while ((line = br.readLine()) != null) {
                    String [] values = line.split(COMMA_DELIMITER);
                    regex_map.add(Arrays.asList(values));
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean isMIPS(String [] tokens) {
        return int_map.get(tokens[0]) != null;
    }


    public String getBinary(String [] tokens, Map<String, Long> labels) {
        long int_inst = getInt(tokens, labels);
        return longToBinary(int_inst, 32);
    }

    private long getInt (String [] tokens, Map<String, Long> labels) {
        /* look up instruction */
        List<String> fields = int_map.get(tokens[0]);

        /* deal with offset */
        long offset = 0;
        for (int i = 0; i < tokens.length; i++) {
            //System.out.print(tokens[i] + ' ');
            int lb = tokens[i].indexOf("(");
            int rb = tokens[i].indexOf(")");
            if (lb >= 0) {
                offset = Long.parseLong(tokens[i].substring(0, lb));
                tokens[i] = tokens[i].substring(lb + 1, rb);
            }
        }

        /* iterate through the fields */
        /* substitute them for numbers */
        ListIterator<String> it = fields.listIterator();
        List<Long> inst = new ArrayList<Long>();
        while (it.hasNext()) {
            String field = it.next();
            if (field.equals("ARG1")) inst.add(tokenToInt(tokens[1], labels));
            else if (field.equals("ARG2")) inst.add(tokenToInt(tokens[2], labels));
            else if (field.equals("ARG3")) inst.add(tokenToInt(tokens[3], labels));
            else if (field.equals("address")) inst.add(offset);
            else if (field.equals("label")) inst.add(tokenToInt(tokens[3], labels));
            else inst.add(Long.parseLong(field, 16));
        }

        switch (fields.size()) {
            /* J */
            case 2:
                return inst.get(0) << 26 | inst.get(1);
            /* break */
            case 3:
                return inst.get(0) << 26 | inst.get(1) << 6 | inst.get(2);
            /* I */
            case 4:
                return inst.get(0) << 26 | inst.get(1) << 21 | inst.get(2) << 16 | inst.get(3);
            /* R */
            case 6:
                return inst.get(0) << 26 | inst.get(1) << 21 | inst.get(2) << 16 | inst.get(3) << 11 | inst.get(4) << 6 | inst.get(5);
        }

        return 0xffff;
    }


    private static String longToBinary (long n, int numOfBits) {
        String binary = "";
        for(int i = 0; i < numOfBits; ++i, n/=2) {
            if (n % 2 == 0) binary = "0" + binary;
            else if (n % 2 == 1) binary = "1" + binary;
        }
        return binary;
    }


    private long tokenToInt(String reg, Map<String, Long> labels) {
        /**
         * reg could be:
         * a register name
         * a label
         * an immediate
         */
        if (reg.equalsIgnoreCase("$zero") || reg.equalsIgnoreCase("R0") || reg.equalsIgnoreCase("$0")) return 0;
        if (reg.equalsIgnoreCase("$at") || reg.equalsIgnoreCase("R1") || reg.equalsIgnoreCase("$1")) return 1;
        if (reg.equalsIgnoreCase("$v0") || reg.equalsIgnoreCase("R2") || reg.equalsIgnoreCase("$2")) return 2;
        if (reg.equalsIgnoreCase("$v1") || reg.equalsIgnoreCase("R3") || reg.equalsIgnoreCase("$3")) return 3;
        if (reg.equalsIgnoreCase("$a0") || reg.equalsIgnoreCase("R4") || reg.equalsIgnoreCase("$4")) return 4;
        if (reg.equalsIgnoreCase("$a1") || reg.equalsIgnoreCase("R5") || reg.equalsIgnoreCase("$5")) return 5;
        if (reg.equalsIgnoreCase("$a2") || reg.equalsIgnoreCase("R6") || reg.equalsIgnoreCase("$6")) return 6;
        if (reg.equalsIgnoreCase("$a3") || reg.equalsIgnoreCase("R7") || reg.equalsIgnoreCase("$7")) return 7;
        if (reg.equalsIgnoreCase("$t0") || reg.equalsIgnoreCase("R8") || reg.equalsIgnoreCase("$8")) return 8;
        if (reg.equalsIgnoreCase("$t1") || reg.equalsIgnoreCase("R9") || reg.equalsIgnoreCase("$9")) return 9;
        if (reg.equalsIgnoreCase("$t2") || reg.equalsIgnoreCase("R10") || reg.equalsIgnoreCase("$10")) return 10;
        if (reg.equalsIgnoreCase("$t3") || reg.equalsIgnoreCase("R11") || reg.equalsIgnoreCase("$11")) return 11;
        if (reg.equalsIgnoreCase("$t4") || reg.equalsIgnoreCase("R12") || reg.equalsIgnoreCase("$12")) return 12;
        if (reg.equalsIgnoreCase("$t5") || reg.equalsIgnoreCase("R13") || reg.equalsIgnoreCase("$13")) return 13;
        if (reg.equalsIgnoreCase("$t6") || reg.equalsIgnoreCase("R14") || reg.equalsIgnoreCase("$14")) return 14;
        if (reg.equalsIgnoreCase("$t7") || reg.equalsIgnoreCase("R15") || reg.equalsIgnoreCase("$15")) return 15;
        if (reg.equalsIgnoreCase("$s0") || reg.equalsIgnoreCase("R16") || reg.equalsIgnoreCase("$16")) return 16;
        if (reg.equalsIgnoreCase("$s1") || reg.equalsIgnoreCase("R17") || reg.equalsIgnoreCase("$17")) return 17;
        if (reg.equalsIgnoreCase("$s2") || reg.equalsIgnoreCase("R18") || reg.equalsIgnoreCase("$18")) return 18;
        if (reg.equalsIgnoreCase("$s3") || reg.equalsIgnoreCase("R19") || reg.equalsIgnoreCase("$19")) return 19;
        if (reg.equalsIgnoreCase("$s4") || reg.equalsIgnoreCase("R20") || reg.equalsIgnoreCase("$20")) return 20;
        if (reg.equalsIgnoreCase("$s5") || reg.equalsIgnoreCase("R21") || reg.equalsIgnoreCase("$21")) return 21;
        if (reg.equalsIgnoreCase("$s6") || reg.equalsIgnoreCase("R22") || reg.equalsIgnoreCase("$22")) return 22;
        if (reg.equalsIgnoreCase("$s7") || reg.equalsIgnoreCase("R23") || reg.equalsIgnoreCase("$23")) return 23;
        if (reg.equalsIgnoreCase("$t8") || reg.equalsIgnoreCase("R24") || reg.equalsIgnoreCase("$24")) return 24;
        if (reg.equalsIgnoreCase("$t9") || reg.equalsIgnoreCase("R25") || reg.equalsIgnoreCase("$25")) return 25;
        if (reg.equalsIgnoreCase("$k0") || reg.equalsIgnoreCase("R26") || reg.equalsIgnoreCase("$26")) return 26;
        if (reg.equalsIgnoreCase("$k1") || reg.equalsIgnoreCase("R27") || reg.equalsIgnoreCase("$27")) return 27;
        if (reg.equalsIgnoreCase("$gp") || reg.equalsIgnoreCase("R28") || reg.equalsIgnoreCase("$28")) return 28;
        if (reg.equalsIgnoreCase("$sp") || reg.equalsIgnoreCase("R29") || reg.equalsIgnoreCase("$29")) return 29;
        if (reg.equalsIgnoreCase("$fp") || reg.equalsIgnoreCase("R30") || reg.equalsIgnoreCase("$30")) return 30;
        if (reg.equalsIgnoreCase("$ra") || reg.equalsIgnoreCase("R31") || reg.equalsIgnoreCase("$31")) return 31;
        /* deal with labels */
        if (labels.get(reg) != null) return labels.get(reg);
        /* immediate */
        try {
            int imm = Integer.parseInt(reg);
            return (imm >= 0 ? imm : imm + 65536);
        } catch (NumberFormatException e) {
            System.out.println("Error: label not found!");
            return -1;
        }
    }

    public String getInstruction(String binary) {
        /**
         * Algorithm for getting an instruction:
         * If machine code matches all constant fields of an instruction,
         * then it should be translated to it.
         */

        String type = findInstruction(binary, regex_map);
        if (type == null) {
            return "(Data part or error)";
            //System.exit(-1);
        }

        List<String> fields = int_map.get(type);

        String inst = type;

        /**
         * * Find the argument number
         * * Get argument token from binary
         *   * If it is an I-instruction with offset, the offset is the second to process
         *   * If it is an I-instruction with immediate, the immediate is the last argument
         *   * J-instruction and break are the same as above
         *   * Constants are skipped
         *   * Else, just add an R
         */
        int a1, a2, a3, label, address;
        switch (fields.size()) {
            case 2:
                a1 = fields.indexOf("ARG1");
                if (a1 >= 0) inst += " " + Integer.parseInt(binary.substring(6, 32), 2); 
                break;
            case 3:
                a1 = fields.indexOf("ARG1");
                if (a1 >= 0) inst += " " + Integer.parseInt(binary.substring(6, 26), 2); 
                break;
            case 4:
                a1 = fields.indexOf("ARG1");
                a2 = fields.indexOf("ARG2");
                a3 = fields.indexOf("ARG3");
                label = fields.indexOf("label");
                address = fields.indexOf("address");
                
                if (address >= 0) {
                    /* can be 1 or 2 arguments */
                    if (a2 >= 0) {
                        //TODO should I use signed here?
                        inst += " R" + Integer.parseInt(binary.substring(1+5*a1, 6+5*a1), 2);
                        inst += " " + toSigned(Integer.parseInt(binary.substring(16, 32), 2));
                        inst += "(R" + Integer.parseInt(binary.substring(1+5*a2, 6+5*a2), 2) + ")"; 
                    } else if (a1 >= 0) {
                        inst += " " + toSigned(Integer.parseInt(binary.substring(16, 32), 2));
                        inst += "(R" + Integer.parseInt(binary.substring(1+5*a1, 6+5*a1), 2) + ")";
                    }
                } else if (label >= 0) {
                    inst += " R" + Integer.parseInt(binary.substring(1+5*a1, 6+5*a1), 2); 
                    /* can be 1 or 2 arguments */
                    if (a2 >= 0) inst += " R" + Integer.parseInt(binary.substring(1+5*a2, 6+5*a2), 2); 
                    int unsigned = Integer.parseInt(binary.substring(16, 32), 2);
                    inst += " " + toSigned(unsigned);
                    
                } else {
                    /* immediate */
                    /* can be 2 or 3 arguments */
                    /* and the last is immediate */
                    inst += " R" + Integer.parseInt(binary.substring(1+5*a1, 6+5*a1), 2); 
                    if (a3 >= 0) inst += " R" + Integer.parseInt(binary.substring(1+5*a2, 6+5*a2), 2); 
                    int unsigned = Integer.parseInt(binary.substring(16, 32), 2);
                    inst += " " + toSigned(unsigned);
                }
                break;
            case 6:
                /* index 1: (6, 11); index 2: (11, 16) ... */
                a1 = fields.indexOf("ARG1");
                a2 = fields.indexOf("ARG2");
                a3 = fields.indexOf("ARG3");
                if (a1 >= 0) inst += " R" + Integer.parseInt(binary.substring(1+5*a1, 6+5*a1), 2); 
                if (a2 >= 0) inst += " R" + Integer.parseInt(binary.substring(1+5*a2, 6+5*a2), 2); 
                if (a3 >= 0) inst += " R" + Integer.parseInt(binary.substring(1+5*a3, 6+5*a3), 2); 
                break;
        }

        return inst;
    }

    private int toSigned(int n) {
        return n >= 32758 ? n - 65536 : n;
    }

    private String findInstruction(String binary, List<List<String>> regex_map) {
        for (List<String> a:regex_map) {
            if (binary.matches(a.get(1)))
                return a.get(0);
        }
        return null;
    }

}































