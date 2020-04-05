package sdfomin.editor;

import sdfomin.parser.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * To run a program, press the button "Run" in the menu bar.
 * Base from: <a href="https://www.geeksforgeeks.org/java-swing-create-a-simple-text-editor/"a>here</a>
 * Seems like it wasn't very good example, but I focused on parser and interpreter.
 */
class Editor extends JFrame implements ActionListener {
    // Text component
    JTextArea textArea;

    // Frame
    JFrame frame;

    // Log panel
    JTextArea textAreaLog;

    // Constructor
    Editor() {

        // Create a frame
        frame = new JFrame("Editor");

        Font font = new Font("Arial", Font.PLAIN, 16);

        // Text component
        textArea = new JTextArea();
        textArea.setFont(font);

        textAreaLog = new JTextArea();
        textAreaLog.setEditable(false);
        textAreaLog.setRows(10);
        textAreaLog.setFont(font);

        // Create a menubar
        JMenuBar menuBar = new JMenuBar();

        // Create a menu for menu
        JMenu menuFile = new JMenu("File");

        // Create menu items
        JMenuItem mi1 = new JMenuItem("New");
        JMenuItem mi2 = new JMenuItem("Open");
        JMenuItem mi3 = new JMenuItem("Save");
        JMenuItem mi9 = new JMenuItem("Print");

        // Add action listener
        mi1.addActionListener(this);
        mi2.addActionListener(this);
        mi3.addActionListener(this);
        mi9.addActionListener(this);

        menuFile.add(mi1);
        menuFile.add(mi2);
        menuFile.add(mi3);
        menuFile.add(mi9);

        // Create a menu for menu
        JMenu menuEdit = new JMenu("Edit");

        // Create menu items
        JMenuItem mi4 = new JMenuItem("cut");
        JMenuItem mi5 = new JMenuItem("copy");
        JMenuItem mi6 = new JMenuItem("paste");

        // Add action listener
        mi4.addActionListener(this);
        mi5.addActionListener(this);
        mi6.addActionListener(this);

        menuEdit.add(mi4);
        menuEdit.add(mi5);
        menuEdit.add(mi6);

        JMenuItem menuRun = new JMenuItem("Run");
        menuRun.addActionListener(this);

        menuBar.add(menuFile);
        menuBar.add(menuEdit);
        menuBar.add(menuRun);

        JPanel panel = new JPanel();
        frame.add(panel);
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        panel.add(new JScrollPane(textAreaLog), BorderLayout.SOUTH);

        frame.setJMenuBar(menuBar);
        frame.setSize(500, 700);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                check(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                check(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            JetBrainsAstNode prev = (new JetBrainsParser("")).parse();

            public void check(DocumentEvent e) {
                try {
                    String program = e.getDocument().getText(0, e.getDocument().getLength());
                    JetBrainsParser updated = new JetBrainsParser(program);
                    JetBrainsAstNode nodeUpdated = updated.parse(); // try to parse for checking block statement update
//                    JetBrainsTaskParser.printTree(nodeUpdated);
                    if (JetBrainsParserLibrary.isBlockStatementUpdated(prev, nodeUpdated)) {
                        textAreaLog.append("Added block statement \n");
                    }
                    prev = nodeUpdated;

                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });
    }

    // If a button is pressed
    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();

        switch (s) {
            case "cut":
                textArea.cut();
                break;
            case "copy":
                textArea.copy();
                break;
            case "paste":
                textArea.paste();
                break;
            case "Save": {
                // Create an object of JFileChooser class
                JFileChooser j = new JFileChooser("f:");

                // Invoke the showsSaveDialog function to show the save dialog
                int r = j.showSaveDialog(null);

                if (r == JFileChooser.APPROVE_OPTION) {

                    // Set the label to the path of the selected directory
                    File fi = new File(j.getSelectedFile().getAbsolutePath());

                    try {
                        // Create a file writer
                        FileWriter wr = new FileWriter(fi, false);

                        // Create buffered writer to write
                        BufferedWriter w = new BufferedWriter(wr);

                        // Write
                        w.write(textArea.getText());

                        w.flush();
                        w.close();
                    } catch (Exception evt) {
                        JOptionPane.showMessageDialog(frame, evt.getMessage());
                    }
                }
                // If the user cancelled the operation
                else
                    JOptionPane.showMessageDialog(frame, "the user cancelled the operation");
                break;
            }
            case "Print":
                try {
                    // print the file
                    textArea.print();
                } catch (Exception evt) {
                    JOptionPane.showMessageDialog(frame, evt.getMessage());
                }
                break;
            case "Open": {
                // Create an object of JFileChooser class
                JFileChooser j = new JFileChooser("f:");

                // Invoke the showsOpenDialog function to show the save dialog
                int r = j.showOpenDialog(null);

                // If the user selects a file
                if (r == JFileChooser.APPROVE_OPTION) {
                    // Set the label to the path of the selected directory
                    File fi = new File(j.getSelectedFile().getAbsolutePath());

                    try {
                        // String
                        String s1;
                        StringBuilder sl;

                        // File reader
                        FileReader fr = new FileReader(fi);

                        // Buffered reader
                        BufferedReader br = new BufferedReader(fr);

                        // Initialize sl
                        sl = new StringBuilder(br.readLine());

                        // Take the input from the file
                        while ((s1 = br.readLine()) != null) {
                            sl.append("\n").append(s1);
                        }

                        // Set the text
                        textArea.setText(sl.toString());
                    } catch (Exception evt) {
                        JOptionPane.showMessageDialog(frame, evt.getMessage());
                    }
                }
                // If the user cancelled the operation
                else
                    JOptionPane.showMessageDialog(frame, "the user cancelled the operation");
                break;
            }
            case "New":
                textArea.setText("");
                break;
            case "Run":
                JetBrainsParser parser = new JetBrainsParser(textArea.getText());
                try {
                    ArrayList<Integer> output = (new JetBrainsInterpreter(parser.parse())).execute();
                    for (int i = 0; i < output.size(); ++i)
                        textAreaLog.append(output.get(i) + (i < output.size() - 1 ? ", " : "\n"));
                } catch (Exception ex) {
                    textAreaLog.append(ex.getMessage() + "\n");
                }
                break;
        }
    }

    // Main class
    public static void main(String[] args) {
        new Editor();
    }
}
