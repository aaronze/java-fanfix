/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import data.Chapter;
import data.Story;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
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
    static ArrayList<Story> stories = new ArrayList<>();
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
        
        refreshFavorites();
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
    
    public void cheatRefresh() {
        new Thread() {
            @Override
            public void run() {
                progressBar.setValue(100 / stories.size());
                
                // Create search query
                for (int j = 0; j < stories.size(); j+=15) {
                    int size = stories.size()-j;
                    if (size > 15) size = 15;
                    
                    String query = "http://archiveofourown.org/works/search?utf8=%E2%9C%93&work_search[query]=";

                    for (int i = 0; i < size; i++) {
                        Story story = stories.get(i+j);
                        query += getStoryPhpCheck(story) + "+||+";
                    }
                    query += getStoryPhpCheck(stories.get(stories.size()-1));

                    try {
                        ArrayList<String> text = Story.getPage(query);
                        partition(text, j, stories.size());
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
        }.start();
    }
    
    public void partition(ArrayList<String> page, int start, int total) throws Exception {
        String authors = "";
        String title = "";
        String workID = "";
        int chapters;
        
        int counter = start;
        
        for (int i = 0; i < page.size(); i++) {
            String line = page.get(i);
            
            if (line.contains("<a href=\"/works/")) {
                int begin = line.indexOf("works/") + 6;
                int end = line.indexOf("</a>");
                
                String s = line.substring(begin, end);
                s = s.replaceAll("\"", "");
                
                String[] st = s.split(">");
                workID = st[0];
                title = st[1];
            }
            else if (line.contains("rel=\"author\">")) {
                int begin = line.indexOf("rel=\"author\">") + 13;
                int end = line.indexOf("</a>");
                
                authors = line.substring(begin, end);
            }
            else if (line.contains("<dt>Chapters:</dt>")) {
                line = page.get(++i);
                
                int begin = line.indexOf("<dd>") + 4;
                int end = line.indexOf("</dd>");
                
                String chapterText = line.substring(begin, end);
                
                String[] st = chapterText.split("/");
                
                chapters = new Integer(st[0]).intValue();
                
                updateStory(authors, title, workID, chapters);
                
                int val = ++counter * 100 / total;
                progressBar.setValue(val);
                repaint();
            }
        }
    }
    
    public static void updateStory(String authors, String title, String workID, int chapters) {
        Story story = getStory(workID);
        
        if (story != null) {
            if (story.readChapters < chapters) {
                story.hasUnread = true;
            }
        }
    }
    
    public static Story getStory(String id) {
        for (Story story : stories) {
            if (story.workID.equals(id)) return story;
        }
        return null;
    }
    
    public String getStoryPhpCheck(Story story) {
        //       (       workID         &  &           authors             )
        return "%28" + story.workID + "%26%26" + story.getAuthorLine() + "%29";
    }
    
    public void refreshFavorites() {
        // Cheaty, cheaty cheater-tastic
        cheatRefresh();
    }
    
    public void addNewStory() {
        AddFanfic add = new AddFanfic(this, true);
        add.setVisible(true);
        
        String title = add.getTxtTitle();
        String author = add.getTxtAuthors();
        String id = add.getTxtID();
        
        addStory(title, author, id);
    }
    
    public void addStory(String title, String author, String id) {
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
        
        try {
            story.download();
            setMainStory(story);
        } catch (Exception e) {
            System.out.println(e);
        }
        repaint();
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
        jMenuItem5 = new javax.swing.JMenuItem();
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

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem5.setText("Add From Clipboard");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem5);

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
        final Story story = listModel.get(index);
        if (story.preLoad()) {
            setMainStory(story);
            repaint();
        }
        
        new Thread() {
            @Override
            public void run() {
                story.download();
                setMainStory(story);
                updateChapterBox();
                repaint();
            }
        }.start();
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

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        // Add from clipboard
        try {
            String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            
            if (data != null) {
                String downloadLink = AddFanfic.getDownloadLinkForURL(data);
                
                String[] st = downloadLink.split("/");
                String authors = st[1];
                String workID = st[2];
                String title = st[3];
                
                title = title.replaceAll("%20", " ");
                
                addStory(title, authors, workID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        repaint();
    }//GEN-LAST:event_jMenuItem5ActionPerformed

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
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JEditorPane storyArea;
    private javax.swing.JList storyList;
    // End of variables declaration//GEN-END:variables
}
