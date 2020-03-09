package com.demo;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class TestForm {
    private JButton downloadButton;
    private JTextField urlTextField;
    private JTextArea chapterTextArea;
    private JTextArea contentTextArea;
    private JButton closeButton;
    private JPanel mainPanel;
    private JButton pathButton;
    private JTextField pathTextField;
    private JTextField charsetTextField;
    private NovelDocument novelDocument;

    public NovelDocument getNovelDocument() {
        return novelDocument;
    }

    public void setNovelDocument(NovelDocument novelDocument) {
        this.novelDocument = novelDocument;
    }

    public TestForm() {
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                novelDocument=new NovelDocument(urlTextField.getText(), chapterTextArea.getText(), contentTextArea.getText(), pathTextField.getText(), charsetTextField.getText());
                novelDocument.download();
                JOptionPane.showMessageDialog(null, "download completed");
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        pathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser=new JFileChooser();
                jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );
                jFileChooser.showDialog(new JLabel(), "选择");
                File file=jFileChooser.getSelectedFile();
                if (file==null){}
                else if(file.isDirectory()){
                }else {
                    pathTextField.setText(file.getAbsolutePath());
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("TestForm");
        frame.setSize(800, 600);
        frame.setContentPane(new TestForm().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
