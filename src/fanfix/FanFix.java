/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fanfix;

import gui.MainFrame;

/**
 *
 * @author aaron
 */
public class FanFix {
    public static void main(String[] args) {       
        MainFrame frame = new MainFrame();
        frame.setSize(1024, 768);
        frame.setDefaultCloseOperation(MainFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
    /*
     Cleans a html line
    */
    public static String cleanString(String s) {
        int start = 0;
        int end = s.length();
        
        // Remove whitespace at beginning of string
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ' ') {
                start++;
            } else {
                break;
            }
        }
        
        // Remove whitespace at end of string
        for (int i = s.length()-1; i >= 0; i--) {
            char c = s.charAt(i);
            if (c == ' ') {
                end--;
            } else {
                break;
            }
        }
         
        return s.substring(start, end);
    }
}
