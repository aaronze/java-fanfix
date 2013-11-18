/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import data.Chapter;
import data.Story;
import static fanfix.FanFix.cleanString;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.text.DefaultCaret;

/**
 *
 * @author aaron
 */
public class MainFrame extends javax.swing.JFrame {
    ArrayList<Story> stories = new ArrayList<>();
    DefaultListModel<Story> listModel;
    DefaultComboBoxModel<String> chapterModel;
    Story mainStory;
    int chapterReading;
    boolean ignoreChapterBox = false;
    
    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
        
        listModel = new DefaultListModel<>();
        storyList.setModel(listModel);
        
        chapterModel = new DefaultComboBoxModel<>();
        chapterBox.setModel(chapterModel);
        
        DefaultCaret caret = (DefaultCaret)storyArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        stories = new ArrayList<>();
        
        try {
            File file = new File("favs.txt");
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            
            String line;
            
            while ((line = reader.readLine()) != null) {
                String[] params = line.split(";");
                
                if (params.length >= 4) {
                    String workID = params[0];
                    int readChapters = new Integer(params[1]).intValue();
                    String title = params[2];
                    ArrayList<String> authors = new ArrayList<>();
                    for (int i = 3; i < params.length; i++) 
                        authors.add(params[i]);
                    Story story = getStory(workID, title, authors.toArray(new String[0]));
                    story.readChapters = readChapters;
                    stories.add(story);
                }
            }
            
        } catch (Exception e) {
            System.out.println("./favs.txt file missing. Creating a new one");
        }
        
        setStoriesAsList(stories);
    }
    
    public Story getStory(String workID, String title, String... authors) {
        Story story = new Story();
        story.authors = authors;
        story.title = title;
        story.workID = workID;
        
        return story;
    }
    
    public void saveFavorites() {
        try {
            File file = new File("favs.txt");
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            
            for (Story story : stories) {
                String line = story.workID + ";" + story.readChapters + ";" + story.title + ";";
                
                for (int i = 0; i < story.authors.length-1; i++) 
                    line += story.authors[i] + ";";
                line += story.authors[story.authors.length-1];
                
                writer.println(line);
            }
            
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setStoriesAsList(ArrayList<Story> stories) {
        listModel.clear();
        this.stories = stories;
        
        for (Story story : stories) {
            listModel.addElement(story);
        }
        
        repaint();
    }
    
    public void setMainStory(Story story) {
        try {
            mainStory = story;
            
            story.currentChapter = story.readChapters;

            if (story.currentChapter < 1) story.currentChapter = 1;
            
            Chapter chapter = story.readChapter(story.currentChapter-1);
            
            chapterReading = story.currentChapter-1;
            
            String text = "<html><head><title></title></head><body>";
            text += chapter.htmlText;
            text += "</body></html>";
            
            storyArea.setText(text);
            storyArea.setCaretPosition(0);
            
            if (mainStory.currentChapter > mainStory.readChapters) {
                mainStory.readChapters = mainStory.currentChapter;

                saveFavorites();
            }

            updateChapterBox();

            repaint();
            
            saveFavorites();
            
            chapterBox.setSelectedIndex(chapterReading);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void updateChapterBox() {
        ignoreChapterBox = true;
        
        chapterModel.removeAllElements();
            
        int read = mainStory.readChapters;
        
        for (int i = 0; i < mainStory.chaptersWritten; i++) {
            String s = "Chapter " + (i+1);
            
            if (i >= read) 
                s = "*" + s;
            
            chapterModel.addElement(s);
        }
        
        ignoreChapterBox = false;
    }
    
    public void refreshFavorites() {
        progressBar.getModel().setValue(0);
        
        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < stories.size(); i++) {
                    int val = (i+1) * 100 / stories.size();
                    progressBar.getModel().setValue(val);
                    repaint();

                    Story story = stories.get(i);
                    try {
                        story.download();

                        int userRead = story.readChapters;

                        String chapterInfo = story.chapterInfo;
                        String completed = chapterInfo.split("/")[0];
                        int authorWritten = new Integer(cleanString(completed)).intValue();

                        if (userRead < authorWritten) {
                            listModel.getElementAt(i).hasUnread = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        repaint();
    }
    
    public void addNewStory() {
        AddFanfic add = new AddFanfic(this, true);
        add.setVisible(true);
        
        String title = add.getTxtTitle();
        String author = add.getTxtAuthors();
        String id = add.getTxtID();
        
        if (title.isEmpty()) return;
        if (author.isEmpty()) return;
        if (id.isEmpty()) return;
        
        Story story = new Story();
        story.title = title;
        story.workID = id;
        story.authors = author.split("-");
        
        stories.add(story);
        listModel.addElement(story);
        saveFavorites();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu2 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        jScrollPane2 = new javax.swing.JScrollPane();
        storyList = new javax.swing.JList();
        jScrollPane3 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        chapterBox = new javax.swing.JComboBox();
        progressBar = new javax.swing.JProgressBar();
        jScrollPane5 = new javax.swing.JScrollPane();
        storyArea = new javax.swing.JEditorPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();

        jMenu2.setText("File");
        jMenuBar2.add(jMenu2);

        jMenu3.setText("Edit");
        jMenuBar2.add(jMenu3);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        storyList.setMaximumSize(new java.awt.Dimension(100, 2000));
        storyList.setMinimumSize(new java.awt.Dimension(100, 600));
        storyList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                storyListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(storyList);

        jScrollPane3.setViewportView(jEditorPane1);

        chapterBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chapterBoxActionPerformed(evt);
            }
        });

        storyArea.setEditable(false);
        storyArea.setContentType("text/html"); // NOI18N
        storyArea.setAutoscrolls(false);
        jScrollPane5.setViewportView(storyArea);

        jMenu1.setText("File");

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText("Add");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("Refresh");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);
        jMenu1.add(jSeparator1);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("Exit");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu4.setText("Help");

        jMenuItem4.setText("About");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem4);

        jMenuBar1.add(jMenu4);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(chapterBox, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE))
                    .addComponent(jScrollPane5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(chapterBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 606, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // Refresh button was pressed
        refreshFavorites();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // Exit button was pressed
        System.exit(0);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void storyListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_storyListValueChanged
        int index = evt.getLastIndex();
        Story story = listModel.get(index);
        story.download();
        
        setMainStory(story);
    }//GEN-LAST:event_storyListValueChanged

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // Add button was pressed
        addNewStory();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void chapterBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chapterBoxActionPerformed
        if (ignoreChapterBox) return;
        
        int chapter = chapterBox.getSelectedIndex();
        
        if (chapter == chapterReading) return;
        
        mainStory.currentChapter = chapter+1;
        
        if (mainStory.currentChapter > mainStory.readChapters) {
            mainStory.readChapters = mainStory.currentChapter;
            
            saveFavorites();
        }
        
        try {
            String text = "<html><head><title></title></head><body>";
            Chapter cha = mainStory.readChapter(mainStory.currentChapter-1);
            text += cha.htmlText;
            text += "</body></html>";

            chapterReading = mainStory.currentChapter-1;

            storyArea.setText(text);
            
            updateChapterBox();
        } catch (Exception e) {
            System.out.println(e);
        }
    }//GEN-LAST:event_chapterBoxActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        new About(this, true).setVisible(true);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox chapterBox;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JEditorPane storyArea;
    private javax.swing.JList storyList;
    // End of variables declaration//GEN-END:variables
}
