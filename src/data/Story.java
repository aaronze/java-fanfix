/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package data;

import static fanfix.FanFix.cleanString;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.net.URL;
import java.util.Date;

/**
 *
 * @author aaron
 */
public class Story {
    public String[] authors;
    public String title;
    public String workID;
    
    private ArrayList<Chapter> chapters = new ArrayList<>();
    public int readChapters;
    public int chaptersWritten;
    public int currentChapter;
    
    public String published;
    public String updated;
    public String chapterInfo;
    public String wordCount;
    
    public String summary;
    public String notes;
    
    public String rating;
    public String[] archiveWarnings;
    public String[] tags;
    
    public boolean hasUnread = false;
    
    public long lastDownloaded = 0;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Story) {
            Story story = (Story)obj;
            
            return story.workID.equals(workID);
        }
        
        return super.equals(obj);
    }
    
    public Chapter readChapter(int index) {
        if (index >= readChapters) {
            readChapters = index;
        }
        
        hasUnread = readChapters != chaptersWritten;
        
        return chapters.get(index);
    }

    @Override
    public String toString() {
        if (hasUnread)
            return "* " + title + " *";
        
        return title;
    }
    
    public void download() {
        long time = new Date().getTime();

        // Only refresh every 1 hour
        if (time - lastDownloaded < 3600000) {
            return;
        }
        
        lastDownloaded = time;
        
        title = title.replaceAll("\\.", "");
        title = title.replaceAll("\\?", "");
        
        ArrayList<String> page = getPage(getPathOf(workID, title, authors));
        
        Story story = this;
        story.chapters = new ArrayList<>();
        
        String chapterTitle = "";
        
        for (int i = 0; i < page.size(); i++) {
            String line = page.get(i);
            
            if (line.contains("<dl class=\"tags\">")) {
                while (!(line = page.get(++i)).contains("</dl>")) {
                    if (line.contains("Rating")) {
                        ArrayList<String> rating = removeTags(page.get(++i));
                        story.rating = rating.get(rating.size()-1);
                        continue;
                    }
                    if (line.contains("Archive Warning")) {
                        ArrayList<String> warnings = removeTags(page.get(++i));
                        story.archiveWarnings = warnings.toArray(new String[0]);
                        continue;
                    }
                    if (line.contains("Additional Tags")) {
                        ArrayList<String> tags = removeTags(page.get(++i));
                        story.tags = tags.toArray(new String[0]);
                        continue;
                    }
                    if (line.contains("Published")) {
                        String[] sp = line.split(":");
                        story.published = sp[sp.length-1];
                        continue;
                    }
                    if (line.contains("Updated")) {
                        String[] sp = line.split(":");
                        story.updated = sp[sp.length-1];
                        continue;
                    }
                    if (line.contains("Chapters")) {
                        String[] sp = line.split(":");
                        story.chapterInfo = sp[sp.length-1];
                        continue;
                    }
                    if (line.contains("Words")) {
                        String[] sp = line.split(":");
                        story.wordCount = sp[sp.length-1];
                        continue;
                    }
                }
            }
            
            if (line.contains("<h2 class=\"heading\">")) {
                ArrayList<String> t = removeTags(line);
                chapterTitle = t.get(t.size()-1);
            }
            
            if (line.contains("<!--chapter content-->")) {
                Chapter chapter = new Chapter(chapterTitle);
                while (!(line = page.get(++i)).equals("</div>")) {
                    if (line.contains("<div class=")) continue;
                    
                    chapter.addHTML(line);
                    
                }
                story.chapters.add(chapter);
            }
            
            if (line.contains("<div id=\"chapters\"")) {
                Chapter chapter = new Chapter(chapterTitle);
                while (!(line = page.get(++i)).equals("</div>")) {
                    chapter.addHTML(line);
                }
                story.chapters.add(chapter);
            }
        }
        
        String completed = chapterInfo.split("/")[0];
        chaptersWritten = new Integer(cleanString(completed)).intValue();
    }
    
    public String getPathOf(String workID, String title, String... authors) {
        String htmlTitle = title.replaceAll(" ", "%20");
        
        String authorLine = "";
        
        for (int i = 0; i < authors.length - 1; i++) {
            authorLine += authors[i] + "-";
        }
        authorLine += authors[authors.length-1];
        
        String authorPath = authorLine.substring(0, 2);
        
        String path = "http://archiveofourown.org/downloads/" + authorPath + "/" + authorLine + "/" + workID + "/" + htmlTitle + ".html";
        return path;
    }
    
    public static ArrayList<String> getPage(String path) {
        ArrayList<String> page = new ArrayList<>();
        
        try {
            URL url = new URL(path);
            InputStream is = url.openStream();
            BufferedReader bi = new BufferedReader(new InputStreamReader(is));
            
            String line;
            
            while ((line = bi.readLine()) != null)
                page.add(line);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return page;
    }

    public static ArrayList<String> removeTags(String s) {
        boolean inTag = false;
        ArrayList<String> text = new ArrayList<>();
        String plainText = "";
        
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            
            if (c == '<') {
                if (!plainText.isEmpty()) {
                    if (!plainText.equals(", ") && !plainText.equals(" ")) 
                        text.add(plainText);
                    plainText = "";
                }
                
                inTag = true;
                continue;
            }
            if (c == '>') {
                inTag = false;
                continue;
            }
            
            if (!inTag) {
                plainText += c;
            }
        }
        
        return text;
    }
}
