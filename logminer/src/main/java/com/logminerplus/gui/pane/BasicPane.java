package com.logminerplus.gui.pane;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

public abstract class BasicPane {

    abstract public void setEditStatus(boolean isEdit);

    @SuppressWarnings("unchecked")
    public void loadComponentStatus(JComponent[] components, boolean isEdit) {
        for (int i = 0; i < components.length; i++) {
            JComponent cs = components[i];
            if (cs instanceof JComboBox) {
                JComboBox<String> component = (JComboBox<String>) cs;
                component.setEditable(isEdit);
                component.setEnabled(isEdit);
            } else if (cs instanceof JTextComponent) {
                JTextComponent component = (JTextComponent) cs;
                component.setEditable(isEdit);
            } else if (cs instanceof JButton) {
                JButton button = (JButton) cs;
                button.setEnabled(isEdit);
            }
        }
    }

}
