package com.insilicalabs.swingbuilder;

import com.insilicalabs.swingbuilder.components.ColorChooserButton;
import com.insilicalabs.swingbuilder.models.SBTableModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import java.awt.Component;
import java.awt.Dialog;

import static com.insilicalabs.swingbuilder.Configurators.OwnerConfigurator;
import static com.insilicalabs.swingbuilder.Configurators.contentPane;
import static com.insilicalabs.swingbuilder.Configurators.getOwnerConfigurator;
import static com.insilicalabs.swingbuilder.Configurators.layout;
import static com.insilicalabs.swingbuilder.Configurators.pack;

/**
 * Created by jzwolak on 3/5/16.
 */
public class Creators {
    public static MigLayout migLayout() { return new MigLayout(); }
    public static MigLayout migLayout(
        String layoutContraints,
        String columnConstraints,
        String rowConstraints
    ) {
        return new MigLayout(layoutContraints,columnConstraints,rowConstraints);
    }

    public static Creator separator(Object... configuration) {
        return new Creator() {
            @Override
            public Component create(Component parent) {
                if (parent instanceof JToolBar) {
                    component = new JToolBar.Separator();
                } else {
                    component = new JSeparator();
                }
                return SwingBuilderBase.configure(component, configuration);
            }
        };
    }

    public static JLabel label(Object... configuration) {
        return SwingBuilderBase.configure(new JLabel(), configuration);
    }

    public static JList list(Object... configuration) {
        return SwingBuilderBase.configure(new JList(), configuration);
    }

    public static JTable table(Object... configuration) {
        return SwingBuilderBase.configure(new JTable(new SBTableModel()), configuration);
    }

    public static JEditorPane editorPane(Object... configuration) {
        JEditorPane ep = new JEditorPane();
        SwingBuilderBase.configure(ep, configuration);
        return ep;
    }

    public static JTextArea textArea(Object... configuration) {
        return SwingBuilderBase.configure(new JTextArea(), configuration);
    }

    public static JTextField textField(Object... configuration) {
        return SwingBuilderBase.configure(new JTextField(), configuration);
    }

    public static JScrollPane scrollPane(Object... configuration) {
        return SwingBuilderBase.configure(new JScrollPane(), configuration);
    }

    public static JSplitPane splitPane(Object... configuration) {
        JSplitPane sp = new JSplitPane();
        sp.setLeftComponent(dummy());
        sp.setRightComponent(dummy());
        return SwingBuilderBase.configure(sp, configuration);
    }

    public static Component dummy() {
        return new JLabel(SwingBuilderBase.DUMMY_COMPONENT_TAG);
    }

    public static JCheckBox checkBox(Object... configuration) {
        JCheckBox box = new JCheckBox();
        SwingBuilderBase.configure(box, configuration);
        return box;
    }

    public static JToggleButton toggleButton(Object... configuration) {
        return SwingBuilderBase.configure(new JToggleButton(), configuration);
    }

    public static JRadioButton radioButton(Object... configuration) {
        return SwingBuilderBase.configure(new JRadioButton(), configuration);
    }

    public static JButton button(Object... configuration) {
        return SwingBuilderBase.configure(new JButton(), configuration);
    }

    public static JComboBox comboBox(Object... configuration) {
        return SwingBuilderBase.configure(new JComboBox(), configuration);
    }

    public static JToolBar toolbar(Object... configuration) {
        return SwingBuilderBase.configure(new JToolBar(), configuration);
    }

    public static JButton colorChooserButton(Object... configuration) {
        ColorChooserButton b = new ColorChooserButton();
        SwingBuilderBase.configure(b, configuration);
        return b;
    }
    
    public static JMenuItem menuItem(Object... configuration) {
        JMenuItem mi = new JMenuItem();
        SwingBuilderBase.configure(mi, configuration);
        return mi;
    }

    public static JTabbedPane tabbedPane(Object... configuration) {
        return SwingBuilderBase.configure(new JTabbedPane(),configuration);
    }

    public static JPanel panel(Object... configuration) {
        return SwingBuilderBase.configure(new JPanel(), layout(migLayout()), configuration);
    }

    public static JDialog dialog(Object... configuration) {
        JDialog dialog;
        OwnerConfigurator ownerConfigurator = getOwnerConfigurator(configuration);
        if (ownerConfigurator != null) {
            dialog = new JDialog(ownerConfigurator.getOwner(), Dialog.DEFAULT_MODALITY_TYPE);
            // Don't bother removing the ownerConfigurator because it may be in a nested array. And it won't hurt
            // to call it twice.
            //configuration = Arrays.stream(configuration).filter((e)->e!=ownerConfigurator).toArray();
            return SwingBuilderBase.configure(dialog, contentPane(panel()), configuration, pack(), ownerConfigurator);
        } else {
            dialog = new JDialog();
            return SwingBuilderBase.configure(dialog, contentPane(panel()), configuration);
        }
    }

    public static JFrame frame(Object... configuration) {
        return SwingBuilderBase.configure(new JFrame(), contentPane(panel()), configuration);
    }

    public abstract static class Creator {
        Component component = null;
        public Component getComponent() {
            return component;
        }
        abstract Component create(Component parent);
    }
}
