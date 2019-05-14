import java.util.*;

public class DisassemblerUtils {
    private List<String> binary = new ArrayList<String>();
    public DisassemblerUtils() {
    }

    public String getAssembly(String line) {
        line = line.trim();

        Instructions inst = new Instructions();
        return inst.getInstruction(line);
    }

    /**
     * coe format:
     * semicolon separated
     * comma separated
     */
    public String coeToBin(String line) {
        System.out.println(line);
        /* empty */
        if (line.equals("")) return "";
        /* consume */
        if (line.contains("memory_initialization_radix")) return "";

        /* radix */
        if (line.split("=").length >= 2) line = line.split("=")[1];
        if (line.contains("memory_initialization_vector")) return "";

        /* remove comments */
        line = line.split(";")[0];

        line = line.replaceAll("\\s", "");
        String [] tokens = line.split(",");


        String ret = "";
        for (String a :
                tokens) {
            a = a.replaceFirst("^0+(?!$)", "");
            /* test */
            System.out.println(a);
            Long inst = Long.parseLong(a, 16);
            String b = Long.toBinaryString(inst);
            while (b.length() < 32) {
                b = "0" + b;
            }
            ret += b + System.lineSeparator();
        }

        return ret;
    }
    
}
