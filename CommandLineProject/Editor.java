// Java Program to create a text editor using java
import java.awt.*;
import javax.swing.*;
import java.io.*; 
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.plaf.metal.*;
import javax.swing.table.*;
import javax.swing.text.*;
/**/
@SuppressWarnings("serial")
class Editor extends JFrame implements ActionListener { 
    // Text component 
    private JTextArea tin = new JTextArea(1, 1); 
    private JTextArea tbin = new JTextArea(1, 1); 
    private JTextArea tcoe = new JTextArea(1, 1); 
    private JTextArea tasm = new JTextArea(1, 1); 
    private JTextArea ttemp = new JTextArea(1, 1);
    private JTextArea ttemp2 = new JTextArea(1, 1);
    private JScrollPane s1 = new JScrollPane(tin);
    private JScrollPane sbin = new JScrollPane(tbin);
    private JScrollPane scoe = new JScrollPane(tcoe);
    private JScrollPane sasm = new JScrollPane(tasm);
    private JScrollPane st = new JScrollPane(ttemp);

    private String opened_file_name = "";

    String[] columnNames = {"Address", "00", "01", "02", "03", "Instruction"};

    Object[][] data = {{"This", "is", "a", "simple", "test", true} };

    private DefaultTableModel model = new DefaultTableModel(columnNames, 0) ;
    private final JTable table = new JTable(model);
    private JScrollPane stable = new JScrollPane(table);


    /**
     * ButtonListener invokes assembler
     */
    class ButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String name = ((JButton)e.getSource()).getText();
            if (name.equals("bin")) {
                sbin.setVisible(true);
                scoe.setVisible(false);
                sasm.setVisible(false);
                stable.setVisible(false);
            } else if (name.equals("coe")) {
                sbin.setVisible(false);
                scoe.setVisible(true);
                sasm.setVisible(false);
                stable.setVisible(false);
            } else if (name.equals("asm")) {
                sbin.setVisible(false);
                scoe.setVisible(false);
                sasm.setVisible(true);
                stable.setVisible(false);
            } else if (name.equals("table")) {
                sbin.setVisible(false);
                scoe.setVisible(false);
                sasm.setVisible(false);
                stable.setVisible(true);
            }
        }
    }

    class OpenListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String s = e.getActionCommand();
            JFileChooser j = new JFileChooser("f:");
            j.setAcceptAllFileFilterUsed(false);

            if (s.equals("coe")) j.addChoosableFileFilter(new OpenFileFilter("coe","Xilinx coe file (.coe)") );
            else if (s.equals("bin")) j.addChoosableFileFilter(new OpenFileFilter("bin","Binary file (.bin)") );
            else if (s.equals("asm" )) j.addChoosableFileFilter(new OpenFileFilter("asm","Assembly language file (.asm)") );
            // Create an object of JFileChooser class
            // Invoke the showsOpenDialog function to show the save dialog
            int r = j.showOpenDialog(null);
            // If the user selects a file
            /* open file */
            if (r == JFileChooser.APPROVE_OPTION) {
                // Set the label to the path of the selected directory
                File fi = new File(j.getSelectedFile().getAbsolutePath());
                opened_file_name = j.getSelectedFile().getAbsolutePath();
                setTitle(opened_file_name);
                try {
                    // String
                    String s1 = "", sl = "";
                    // File reader
                    FileReader fr = new FileReader(fi);
                    // Buffered reader
                    BufferedReader br = new BufferedReader(fr);
                    // Initilize sl
                    sl = br.readLine();
                    // Take the input from the file
                    while ((s1 = br.readLine()) != null) {
                        sl = sl + "\n" + s1;
                    }
                    // Set the text
                    tin.setText(sl);
                }
                catch (Exception evt) {
                }
            }

            translate(s);
        }
    }


    class SaveListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String s = e.getActionCommand(); 
            JFileChooser j = new JFileChooser("f:"); 
            j.setAcceptAllFileFilterUsed(false);

            if (s.equals("coe") || s.equals("bin") || s.equals("asm")) {
                j.addChoosableFileFilter(new OpenFileFilter(s, s));
                int r = j.showSaveDialog(null); 
                if (r == JFileChooser.APPROVE_OPTION) { 
                    // Set the label to the path of the selected directory 
                    String fn;
                    if (j.getSelectedFile().getName().indexOf("." + s) >= 0)
                        fn = j.getSelectedFile().getAbsolutePath();
                    else
                        fn = j.getSelectedFile().getAbsolutePath() + "." + s; 

                    File fi = new File(fn);
                    try { 
                        // Create a file writer 
                        FileWriter wr = new FileWriter(fi, false); 
                        // Create buffered writer to write 
                        BufferedWriter w = new BufferedWriter(wr); 
                        // Write
                        switch (s)
                        {
                            case "bin": w.write(tbin.getText()); break;
                            case "coe": w.write(tcoe.getText()); break;
                            case "asm": w.write(tasm.getText()); break;
                        }
                        w.flush();
                        w.close(); 
                    } 
                    catch (Exception evt) { } 
                } 
            } else if (s.equals("Save editor")) {
                File fi = null;
                if (opened_file_name.equals("")) {
                    // Create an object of JFileChooser class 
                    // Invoke the showsSaveDialog function to show the save dialog 
                    int r = j.showSaveDialog(null); 
                    if (r == JFileChooser.APPROVE_OPTION) { 
                        // Set the label to the path of the selected directory 
                        String fn;
                        fn = j.getSelectedFile().getAbsolutePath();

                        fi = new File(fn);
                        opened_file_name = fn;
                    }
                } else { fi = new File(opened_file_name); }
                setTitle(opened_file_name);

                try { 
                    // Create a file writer 
                    FileWriter wr = new FileWriter(fi, false); 
                    // Create buffered writer to write 
                    BufferedWriter w = new BufferedWriter(wr); 
                    // Write 
                    w.write(tin.getText()); 
                    w.flush(); 
                    w.close(); 
                } 
                catch (Exception evt) { } 
            }

            String [] names = opened_file_name.split("\\.");
            translate(names[names.length - 1]);

        }
    }

    private void translate(String s) {
        /* assemble */
        tbin.setText("");
        tcoe.setText("");
        tasm.setText("");
        ttemp.setText("");
        ttemp2.setText("");
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        AssemblerUtils testutil = new AssemblerUtils();
        AssemblerUtils autil = new AssemblerUtils();
        DisassemblerUtils dutil = new DisassemblerUtils();
        if (s.equals("asm")) {
            /* first pass */
            for (String line : tin.getText().split("\\n")) {
                ttemp.append(autil.scanLabels(new AssemblyLine(line)) + System.lineSeparator());
            }

            for (String line : ttemp.getText().split("\\n")) {
                /* translate to bin, but could have 8-bit line */
                ttemp2.append(autil.getBin(new AssemblyLine(line)).returnBin());
            }

            /* bin could have 8-bit instruction (db) */
            /* here we merge 8-bit lines */
            int line_count = 0;
            String line_temp = "";
            for (String bin : ttemp2.getText().split("\\n")) {
                if (bin.length() == 32) tbin.append(bin + System.lineSeparator());
                else {
                    line_temp += bin;
                    if (line_count == 3) {
                        tbin.append(line_temp + System.lineSeparator());
                        line_temp = "";
                        line_count = 0;
                    } else {
                        line_count ++;
                    }
                }
            }

            for (String bin : tbin.getText().split("\\n")) {
                String inst = dutil.getAssembly(bin) + System.lineSeparator();
                tasm.append(inst);
            }

            /* bin to coe */
            tcoe.append("memory_initialization_radix=16;\nmemory_initialization_vector=" + System.lineSeparator());
            for (String bin : tbin.getText().split("\\n")) {
                tcoe.append(autil.longToHexString8Bits(Long.parseLong(bin, 2)));
                tcoe.append(", ");
            }
            tcoe.append(";");

        } else if (s.equals("bin")) {
            /* just copy */
            for (String bin : tin.getText().split("\\n")) {
                tasm.append(dutil.getAssembly(bin) + System.lineSeparator());
            }

            for (String line : tasm.getText().split("\\n")) {
                ttemp.append(autil.scanLabels(new AssemblyLine(line)) + System.lineSeparator());
            }
            /* second pass */
            boolean coe_first = true;
            for (String line : ttemp.getText().split("\\n")) {
                tbin.append(autil.getBin(new AssemblyLine(line)).returnBin());
                tcoe.append(autil.getBin(new AssemblyLine(line)).returnCoe(coe_first));
                coe_first = false;
            }
            tcoe.append(";");
        } else if (s.equals("coe")) {
            for (String line :
                    tin.getText().split("\\n")) {
                String b =  dutil.coeToBin(line);
                if (! b.equals("")) tbin.append(dutil.coeToBin(line));
            }

            for (String bin : tbin.getText().split("\\n")) {
                tasm.append(dutil.getAssembly(bin) + System.lineSeparator());
            }

            for (String line : tasm.getText().split("\\n")) {
                ttemp.append(autil.scanLabels(new AssemblyLine(line)) + System.lineSeparator());
            }
            /* second pass */
            boolean coe_first = true;
            for (String line : ttemp.getText().split("\\n")) {
                tcoe.append(autil.getBin(new AssemblyLine(line)).returnCoe(coe_first));
                coe_first = false;
            }
            tcoe.append(";");
        }

        dutil = new DisassemblerUtils();
        long pc = autil.getInitPC();
        boolean is_data_part = false;
        List<Long> base_list = autil.getBases();
        List<Long> data_list = autil.getDataAddress();
        /* pc_list should be end_address - base_address */
        System.out.println();
        System.out.print("base_list is ");
        for (Long aLong : base_list) { System.out.print(Long.toString(aLong, 16) + " "); }
        System.out.println();
        System.out.print("data_list is ");
        for (Long aLong : data_list) { System.out.print(Long.toString(aLong, 16) + " "); }
        System.out.println();

        /* update table */
        for (String bin : tbin.getText().split(System.lineSeparator())) {
            if (base_list.indexOf(pc) >= 0) is_data_part = false;
            if (data_list.indexOf(pc) >= 0) is_data_part = true;
            String inst = dutil.getAssembly(bin) + System.lineSeparator();

            List<Object> d = new ArrayList<>();
            d.add(Long.toString(pc, 16));
            for (int i = 0; i < 32; i+=8) {
                long decimal = Long.parseLong(bin.substring(i, i + 8),2);
                String hexStr = Long.toString(decimal,16);
                d.add(hexStr);
            }
            if (is_data_part) d.add("(Data)");
            else d.add(inst);
            model.addRow(d.toArray());
            pc += 4;
        }


        
    }

    private ButtonListener bl = new ButtonListener();
    private OpenListener ol = new OpenListener();
    private SaveListener sl = new SaveListener();
    // Constructor 
    public Editor() { 
        // Create a frame 
        try { 
            // Set metl look and feel 
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel"); 
            // Set theme to ocean 
            MetalLookAndFeel.setCurrentTheme(new OceanTheme()); 
        } 
        catch (Exception e) { 
        } 

        // Create a menubar 
        JMenuBar mb = new JMenuBar(); 
        // Create amenu for menu 
        JMenu m1 = new JMenu("File"); 
        // Create menu items 
        JMenuItem m1i1 = new JMenuItem("New");
        JMenuItem m1i3 = new JMenuItem("Save editor");
        JMenuItem m1i9 = new JMenuItem("Print");
        //a submenu
        JMenu sm1 = new JMenu("Open");
        sm1.setMnemonic(KeyEvent.VK_O);
        JMenuItem sm1i1 = new JMenuItem("asm");
        JMenuItem sm1i2 = new JMenuItem("bin");
        JMenuItem sm1i3 = new JMenuItem("coe");
        sm1i1.addActionListener(ol);
        sm1i2.addActionListener(ol);
        sm1i3.addActionListener(ol);
        sm1.add(sm1i1); sm1.add(sm1i2); sm1.add(sm1i3);
        //a submenu
        JMenu sm2 = new JMenu("Save result as");
        sm1.setMnemonic(KeyEvent.VK_S);
        JMenuItem sm2i1 = new JMenuItem("asm");
        JMenuItem sm2i2 = new JMenuItem("bin");
        JMenuItem sm2i3 = new JMenuItem("coe");
        sm2i1.addActionListener(sl);
        sm2i2.addActionListener(sl);
        sm2i3.addActionListener(sl);
        sm2.add(sm2i1); sm2.add(sm2i2); sm2.add(sm2i3);
        //TODO
        sm2i1.addActionListener(this);
        // Add action listener 
        m1i1.addActionListener(this); 
        m1i1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK)); // Set a keyboard shortcut
        m1i9.addActionListener(this);
        m1i9.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK)); // Set a keyboard shortcut
        //sm1.addActionListener(this);
        //sm1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK & InputEvent.SHIFT_DOWN_MASK)); // Set a keyboard shortcut
        m1i3.addActionListener(sl);
        m1i3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK)); // Set a keyboard shortcut
        sm2i1.addActionListener(sl);
        sm2i2.addActionListener(sl);
        sm2i3.addActionListener(sl);

        m1.add(m1i1); 
        m1.add(sm1);
        m1.add(m1i3); 
        m1.add(sm2);
        m1.add(m1i9); 
        // Create amenu for menu 
        JMenu m2 = new JMenu("Edit"); 
        // Create menu items 
        JMenuItem m2i1 = new JMenuItem("cut"); 
        JMenuItem m2i2 = new JMenuItem("copy"); 
        JMenuItem m2i3 = new JMenuItem("paste"); 
        // Add action listener 
        m2i1.addActionListener(this); 
        m2i2.addActionListener(this); 
        m2i3.addActionListener(this); 
        m2.add(m2i1); 
        m2.add(m2i2); 
        m2.add(m2i3); 
        // menu: close
        JMenuItem mc = new JMenuItem("Exit"); 
        mc.addActionListener(this); 
        mb.add(m1); 
        mb.add(m2); 
        mb.add(mc); 
        // bottons
        JButton bbin = new JButton("bin");
        JButton bcoe = new JButton("coe");
        JButton basm = new JButton("asm");
        JButton btable = new JButton("table");
        JLabel lconvert = new JLabel();
        lconvert.setText("View as: ");
        // SpringLayout
        setJMenuBar(mb); 
        Container contentPane = this.getContentPane();
        SpringLayout layout = new SpringLayout();
        layout.putConstraint(SpringLayout.WEST, sbin, 5, SpringLayout.EAST, s1);
        //layout.putConstraint(SpringLayout.EAST, bbin, 0, SpringLayout.EAST, s1);
        layout.putConstraint(SpringLayout.WEST, btable, 5, SpringLayout.EAST, lconvert);
        layout.putConstraint(SpringLayout.WEST, bbin, 5, SpringLayout.EAST, btable);
        //layout.putConstraint(SpringLayout.WEST, bcoe, 0, SpringLayout.WEST, sbin);
        layout.putConstraint(SpringLayout.WEST, bcoe, 5, SpringLayout.EAST, bbin);
        layout.putConstraint(SpringLayout.WEST, basm, 5, SpringLayout.EAST, bcoe);
        layout.putConstraint(SpringLayout.WEST, bbin, 5, SpringLayout.EAST, btable);

        layout.putConstraint(SpringLayout.EAST, contentPane, 5, SpringLayout.EAST, sbin);
        layout.putConstraint(SpringLayout.NORTH, bbin, 5, SpringLayout.SOUTH, s1);
        layout.putConstraint(SpringLayout.NORTH, bcoe, 5, SpringLayout.SOUTH, s1);
        layout.putConstraint(SpringLayout.NORTH, basm, 5, SpringLayout.SOUTH, s1);
        layout.putConstraint(SpringLayout.NORTH, btable, 5, SpringLayout.SOUTH, s1);
        layout.putConstraint(SpringLayout.NORTH, lconvert, 5, SpringLayout.SOUTH, s1);
        layout.putConstraint(SpringLayout.SOUTH, s1, 0, SpringLayout.SOUTH, sbin);
        layout.putConstraint(SpringLayout.NORTH, s1, 0, SpringLayout.NORTH, sbin);
        layout.putConstraint(SpringLayout.SOUTH, contentPane, 5, SpringLayout.SOUTH, bbin);
        layout.putConstraint(SpringLayout.SOUTH, contentPane, 5, SpringLayout.SOUTH, bcoe);
        layout.putConstraint(SpringLayout.SOUTH, contentPane, 5, SpringLayout.SOUTH, basm);
        layout.putConstraint(SpringLayout.SOUTH, contentPane, 5, SpringLayout.SOUTH, btable);
        layout.putConstraint(SpringLayout.SOUTH, contentPane, 5, SpringLayout.SOUTH, lconvert);

        layout.putConstraint(SpringLayout.EAST, scoe, 0, SpringLayout.EAST, sbin);
        layout.putConstraint(SpringLayout.WEST, scoe, 0, SpringLayout.WEST, sbin);
        layout.putConstraint(SpringLayout.NORTH, scoe, 0, SpringLayout.NORTH, sbin);
        layout.putConstraint(SpringLayout.SOUTH, scoe, 0, SpringLayout.SOUTH, sbin);
        layout.putConstraint(SpringLayout.EAST, sasm, 0, SpringLayout.EAST, scoe);
        layout.putConstraint(SpringLayout.WEST, sasm, 0, SpringLayout.WEST, scoe);
        layout.putConstraint(SpringLayout.NORTH, sasm, 0, SpringLayout.NORTH, scoe);
        layout.putConstraint(SpringLayout.SOUTH, sasm, 0, SpringLayout.SOUTH, scoe);
        layout.putConstraint(SpringLayout.EAST, stable, 0, SpringLayout.EAST, scoe);
        layout.putConstraint(SpringLayout.WEST, stable, 0, SpringLayout.WEST, scoe);
        layout.putConstraint(SpringLayout.NORTH, stable, 0, SpringLayout.NORTH, scoe);
        layout.putConstraint(SpringLayout.SOUTH, stable, 0, SpringLayout.SOUTH, scoe);


        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        TableColumn column = null;
        for (int i = 0; i < 6; i++) {
            column = table.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(50);
            } else if (i == 5) {
                column.setPreferredWidth(220); //third column is bigger
            } else {
                column.setPreferredWidth(30);
            }
        }

        tin.setLineWrap(true);
        tbin.setLineWrap(true);
        tbin.setEditable(false);
        tcoe.setLineWrap(true);
        tcoe.setEditable(false);
        tasm.setLineWrap(true);
        tasm.setEditable(false);
        st.setVisible(false);
        sbin.setVisible(false);
        scoe.setVisible(false);
        sasm.setVisible(false);
        stable.setVisible(true);
        bbin.addActionListener(bl);
        bcoe.addActionListener(bl);
        basm.addActionListener(bl);
        btable.addActionListener(bl);
        setLayout(layout);


        add(bbin);
        add(bcoe);
        add(basm);
        add(btable);
        add(lconvert);
        add(s1); 
        add(sbin); 
        add(scoe); 
        add(sasm); 
        add(stable); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 800));
        setSize(800, 800);
        setVisible(true);
    } 

    // If a button is pressed 
    public void actionPerformed(ActionEvent e) 
    { 
        String s = e.getActionCommand(); 

        if (s.equals("cut")) { tin.cut(); } 
        else if (s.equals("copy")) { tin.copy(); } 
        else if (s.equals("paste")) { tin.paste(); } 
        else if (s.equals("Print")) { 
            try { 
                // print the file 
                tbin.print(); 
            } 
            catch (Exception evt) { 
                JOptionPane.showMessageDialog(this, evt.getMessage()); 
            } 
        } 
        else if (s.equals("New")) {
            opened_file_name = "";
            setTitle("[Unsaved] New File");
            tin.setText(""); 
        } 
        else if (s.equals("Exit")) { 
            System.exit(0);
        } 
    } 

    public static void run(final JFrame f, final int width, final int height) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    String path = new File(".").getCanonicalPath();
                    System.out.println(path);
                } catch (IOException e) { }

                f.setTitle("[Unsaved] New File");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setSize(width, height);
                f.setVisible(true);
            }
        });
    }


    // Main class 
    public static void main(String args[]) 
    { 
        run(new Editor(), 500, 500);
    } 
}
