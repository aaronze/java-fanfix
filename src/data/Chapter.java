/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package data;

import java.util.ArrayList;

/**
 *
 * @author aaron
 */
public class Chapter {
    public String title;
    public String htmlText = "";
    
    public Chapter(String title) {
        this.title = title;
    }
    
    public void addHTML(String s) {
        htmlText += s;
    }
}
