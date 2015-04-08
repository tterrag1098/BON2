package com.github.parker8283.bon2;

import java.awt.EventQueue;

public class BON2 {

    public static void main(String[] args) {
        if(args.length > 0) {
            parseArgs(args);
        } else {
            launchGui();
        }
    }

    private static void parseArgs(String[] args) {
        //TODO Parse Args (duh)
    }

    private static void launchGui() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    BON2Gui frame = new BON2Gui();
                    frame.setVisible(true);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
