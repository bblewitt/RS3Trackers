package main.java.com.bblewitt.util;

import javax.swing.*;

public class CustomToolTip extends JToolTip {
    public CustomToolTip() {
        setUI(new CustomToolTipUI());
    }
}
