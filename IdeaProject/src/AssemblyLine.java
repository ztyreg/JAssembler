import java.util.*;
import java.lang.*;
import java.io.*;

public class AssemblyLine {
    private String line;
    private String line_original;
    private List<String> binary = new ArrayList<String>();

    public AssemblyLine(String line) {
        this.line = line;
        line_original = line;
    }

    public String getLine() {
        return line_original;
    }

    public void addBinary(String bin) {
        binary.add(bin);
    }

    private void sortBinary() {
        /* same length */
        /* concatenate 8-bit binaries to 32-bit */
        if (binary.size() == 0) return;
        if (binary.get(0).length() == 32) return;

        List<String> binary_sorted = new ArrayList<String>();
        for (int i = 0; i < binary.size() / 4 * 4; i += 4) {
            String bin_32 = binary.get(i) + binary.get(i + 1) + binary.get(i + 2) + binary.get(i + 3);
            binary_sorted.add(bin_32);
        }
        String bin_32 = "";
        if (binary.size() % 4 != 0) {
            for (int i = binary.size()/4*4; i < binary.size()/4*4+4; i++) {
                bin_32 += (i < binary.size()) ? binary.get(i) : "00000000";
            }
            binary_sorted.add(bin_32);
        }
        binary = binary_sorted;
    }

    public String returnBin() {
        String ret = "";
        for (String a:binary)
            ret += a + System.lineSeparator();
        return ret;
    }

    public String returnBinNoNewLine() {
        String ret = "";
        for (String a:binary)
            ret += a;
        return ret;
    }

    public String returnHex() {
        Long decimal;
        String ret = "";
        String hex;
        for (String a:binary) {
            decimal = Long.parseLong(a, 2);
            hex = Long.toString(decimal, 16);
            int num_zeros = 8 - hex.length();
            for (int i = 0; i < num_zeros; i++) {
                hex = "0" + hex;
            }
            ret += hex + System.lineSeparator();
        }
        return ret;
    }

    public String returnCoe(boolean is_first) {
        String ret = "";

        if (is_first) ret += "memory_initialization_radix=16;\nmemory_initialization_vector=" + System.lineSeparator();
        Long decimal;
        String hex;
        for (String a:binary) {
            decimal = Long.parseLong(a, 2);
            hex = Long.toString(decimal, 16);
            int num_zeros = 8 - hex.length();
            for (int i = 0; i < num_zeros; i++) {
                hex = "0" + hex;
            }
            if (is_first) ret += hex;
            else ret += ", " + hex;
        }
        return ret;
    }

    public void printBin() {
        sortBinary();
        for (String a:binary)
            System.out.println(a);
    }

    public void printHex() {
        sortBinary();
        Long decimal;
        String hex;
        for (String a:binary) {
            decimal = Long.parseLong(a, 2);
            hex = Long.toString(decimal, 16);
            int num_zeros = 8 - hex.length();
            for (int i = 0; i < num_zeros; i++) {
                hex = "0" + hex;
            }
            System.out.println(hex);
        }
    }

    public void printCoe(boolean is_first) {
        sortBinary();
        if (is_first) System.out.println("memory_initialization_radix=16;\nmemory_initialization_vector=");
        Long decimal;
        String hex;
        for (String a:binary) {
            decimal = Long.parseLong(a, 2);
            hex = Long.toString(decimal, 16);
            int num_zeros = 8 - hex.length();
            for (int i = 0; i < num_zeros; i++) {
                hex = "0" + hex;
            }
            if (is_first) System.out.println(hex);
            else System.out.print(", " + hex);
        }
    }

    public boolean hasLabel() {
        line = line_original;
        if (line.split("//").length == 0) return false;
        line = line.split("//")[0];
        if (line.split("#").length == 0) return false;
        line = line.split("#")[0];
        if (line.split(";").length == 0) return false;
        line = line.split(";")[0];
        line = line.trim();
        return line.indexOf(":") >= 0;
    }

    public String getLabel() {
        //TODO null label
        if (hasLabel()) return line_original.split(":")[0].trim();
        else return "";
    }

    public boolean isInstruction() {
        line = line_original;
        if (line.split("//").length == 0) return false;
        line = line.split("//")[0];
        if (line.split("#").length == 0) return false;
        line = line.split("#")[0];
        if (line.split(";").length == 0) return false;
        line = line.split(";")[0];
        if (hasLabel()) {
            if (line.split(":").length < 2) return false;
            line = line.split(":")[1];
        }
        /* only whitespaces */
        if (line.trim().length() == 0) return false;
        return true;
    }

    public String removeComments() {
        line = line_original;
        if (line.split("//").length == 0) return "";
        line = line.split("//")[0];
        if (line.split("#").length == 0) return "";
        line = line.split("#")[0];
        if (line.split(";").length == 0) return "";
        line = line.split(";")[0];
        return line;
    }

    public String removeLabel() {
        line = line_original;
        if (hasLabel()) {
            /* no chars after ':' */
            if (line.split(":").length < 2) return "";
            return line.split(":")[1];
        }
        return line;
    }

    public String [] getTokens() {
        line = line_original;
        if (line.split("//").length == 0) return new String [] {""};
        line = line.split("//")[0];
        if (line.split("#").length == 0) return new String [] {""};
        line = line.split("#")[0];
        if (line.split(";").length == 0) return new String [] {""};
        line = line.split(";")[0];
        if (hasLabel()) {
            /* no chars after ':' */
            if (line.split(":").length < 2) return new String [] {""};
            line = line.split(":")[1];
        }
        line = line.trim();
        return line.split("[\\s,]+"); 
    }

    public String getType() {
        if (getTokens().length == 0) return "";
        return getTokens()[0];
    }
}
