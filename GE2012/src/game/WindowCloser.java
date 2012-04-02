package game;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WindowCloser extends WindowAdapter {
    public WindowCloser(){
        this(true);
    }

    public WindowCloser(boolean exitOnClose){
        setExitOnClose(exitOnClose);
    }

    public void windowClosing(WindowEvent e){
        Window w = e.getWindow();
        w.setVisible(false);
        w.dispose();
        if(exitOnClose()){
            System.exit(0);
        }
    }

    protected boolean exitOnClose(){
        return this.exitOnClose;
    }

    protected void setExitOnClose(boolean b){
        exitOnClose = b;
    }

    private boolean exitOnClose = false;
}

