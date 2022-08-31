package com.insilicalabs.swingbuilder;

import com.insilicalabs.swingbuilder.components.ColorChooserButton;
import com.insilicalabs.swingbuilder.models.SBTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.insilicalabs.swingbuilder.SwingBuilderBase.isDummy;

/**
 * Created by jzwolak on 3/5/16.
 */
public class Configurators {

    private static final Logger LOG = LoggerFactory.getLogger(Configurators.class);

    public final static Configurator SELECTED = new Configurator() {
        @Override
        protected void apply(Component c) {
            invokeIfPresent(c, "setSelected", new Class[]{boolean.class}, true);
        }
    };

    public final static Configurator UNSELECTED = new Configurator() {
        @Override
        protected void apply(Component c) {
            invokeIfPresent(c, "setSelected", new Class[]{boolean.class}, false);
        }
    };

    private static Dimension createDimension(int width, int height) {
        if ( width < 0 && height < 0 ) return null;
        return new Dimension(width, height);
    }

    public static Configurator minimumSize(int width, int height) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.setMinimumSize(createDimension(width, height));
            }
        };
    }

    public static Configurator maximumSize(int width, int height) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.setMaximumSize(createDimension(width, height));
            }
        };
    }

    public static Configurator preferredSize(int width, int height) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.setPreferredSize(createDimension(width, height));
            }
        };
    }

    public static Configurator size(int width, int height) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.setSize(width, height);
            }
        };
    }

    public static Configurator extendedState(int state) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                JFrame frame = (JFrame)c;
                frame.setExtendedState(state);
            }
        };
    }

    public static Configurator columns(int columns) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                JTextField textField = (JTextField)c;
                textField.setColumns(columns);
            }
        };
    }

    public static Configurator cursor(int cursorId) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.setCursor(new Cursor(cursorId));
            }
        };
    }

    public static Configurator hide() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.setVisible(false);
            }
        };
    }

    public static Configurator show() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.setVisible(true);
            }
        };
    }

    public static Configurator visible(boolean visible) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.setVisible(visible);
            }
        };
    }

    public static Configurator name(String name) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.setName(name);
            }
        };
    }

    public static Configurator actionCommand(String actionCommand) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((AbstractButton)c).setActionCommand(actionCommand);
            }
        };
    }

    /**
     * Takes a resource location (relative to this class, Configurators) and creates an image from that resource.
     *
     * @param location
     * @return
     */
    public static Configurator iconImage(String location) {
        return iconImage(new ImageIcon(Configurators.class.getResource(location)).getImage());
    }

    public static Configurator iconImage(Image image) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JFrame) c).setIconImage(image);
            }
        };
    }

    public static Configurator defaultCloseOperation(int op) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JFrame) c).setDefaultCloseOperation(op);
            }
        };
    }

    public static Configurator tooltip(String text) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JComponent)c).setToolTipText(text);
            }
        };
    }

    public static Configurator icon(Icon icon) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                invokeIfPresent(c, "setIcon", new Class[]{Icon.class}, new Object[]{icon});
            }
        };
    }

    public static Configurator icon(String location) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                invokeIfPresent(c, "setIcon", new Class[]{Icon.class},
                    new Object[]{new ImageIcon(getClass().getResource(location))});
            }
        };
    }

    public static Configurator title(String title) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                invokeIfPresent(c, "setTitle", new Class[]{String.class}, title);
            }
        };
    }

    public static Configurator pane(String title, Component content) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                JTabbedPane tabbedPane = (JTabbedPane)c;
                tabbedPane.add(title,content);
            }
        };
    }

    public static <T> Configurator contents(Collection<T> collection, Function<T, ? extends JComponent> function) {
        java.util.List<JComponent> components = new ArrayList<>(collection.size());
        for (T element : collection) {
            components.add(function.apply(element));
        }
        return contents((Object[]) components.toArray(new JComponent[0]));
    }

    public static Configurator contents(Object... objects) {
        return new Configurator() {
            private void addComponents(Container container, final Object... components) {
                Function<Integer, Integer> skipDummies = (index) -> {
                    while ( index < components.length && isDummy(components[index]) ) index++;
                    return index;
                };
                int i = skipDummies.apply(0);
                while ( i < components.length ) {
                    Object object = components[i];
                    int objectIndex = i;
                    i = skipDummies.apply(i+1);
                    Component child;
                    if (object instanceof Object[]) {
                        LOG.info("component " + objectIndex + " is an array: " + object);
                        addComponents(container, (Object[]) object);
                        continue;
                    } else if (object instanceof Creators.Creator) {
                        child = ((Creators.Creator) object).create(container);
                    } else if (object instanceof Component) {
                        child = (Component) object;
                    } else if (object instanceof Collection) {
                        LOG.info("component " + objectIndex + " is a collection: " + object);
                        addComponents(container, ((Collection)object).toArray());
                        continue;
                    } else {
                        LOG.error("element passed to contents is not a component or supported type: " + object.getClass().getName());
                        LOG.error("element = " + object);
                        child = (Component) object;
                    }
                    // if next component is string, then consider it layout constraint, otherwise, just add the child
                    // w/o layout constraints.
                    if (i < components.length && components[i] instanceof String) {
                        // apply MigLayout constraint
                        container.add(child, components[i]);
                        i = skipDummies.apply(i+1);
                    } else {
                        container.add(child);
                    }
                }
            }

            @Override
            protected void apply(Component aComponent) {
                final Container container;
                if (aComponent instanceof RootPaneContainer) {
                    container = ((RootPaneContainer) aComponent).getContentPane();
                } else {
                    container = (Container) aComponent;
                }
                addComponents(container, objects);
                if ( container.isVisible() ) {
                    // revalidate causes layout to be redone
                    container.revalidate();
                    // repaint causes the container to cover removed components that are not covered by relaying out.
                    container.repaint();
                }
            }
        };
    }

    public static Configurator replacecontents(java.util.List list) {
        return replacecontents(list.toArray(new Object[0]));
    }

    public static Configurator replacecontents(Object... objects) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                Container container = (Container) c;
                container.removeAll();
                contents(objects).apply(c);
            }
        };
    }

    public static Configurator center() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                if (c instanceof Window) {
                    Window window = (Window)c;
                    window.setLocationRelativeTo(null);
                }
            }
        };
    }

    public static Configurator center(Component owner) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                if (c instanceof Window) {
                    Window window = (Window) c;
                    window.setLocationRelativeTo(owner);
                }
            }
        };
    }

    public static Configurator pack() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((Window) c).pack();
            }
        };
    }

    public static Configurator layout(LayoutManager lm) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((Container) c).setLayout(lm);
            }
        };
    }

    public static Configurator contentPane(Container container) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                RootPaneContainer rpc = ((RootPaneContainer) c);
                if (rpc.getContentPane() != container) {
                    rpc.setContentPane(container);
                }
            }
        };
    }

    public static Configurator font(Font font) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.setFont(font);
            }
        };
    }

    public static Configurator fontsize(float size) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                Font f = c.getFont();
                c.setFont(f.deriveFont(size));
            }
        };
    }

    public static Configurator bold() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                Font f = c.getFont();
                c.setFont(f.deriveFont(Font.BOLD));
            }
        };
    }

    public static Configurator text(String t) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                invokeIfPresent(c, "setText", new Class[]{String.class}, t);
            }
        };
    }

    public static Configurator rightComponent(Component rc) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JSplitPane) c).setRightComponent(rc);
            }
        };
    }

    public static Configurator leftComponent(Component lc) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JSplitPane) c).setLeftComponent(lc);
            }
        };
    }

    public static Configurator vertical() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JSplitPane) c).setOrientation(JSplitPane.VERTICAL_SPLIT);
            }
        };
    }

    public static Configurator horizontal() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JSplitPane) c).setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            }
        };
    }

    public static Configurator viewport(Component c) {
        return new Configurator() {
            @Override
            protected void apply(Component scrollpane) {
                ((JScrollPane) scrollpane).setViewportView(c);
            }
        };
    }

    public static Configurator onColorSelected(Consumer<Color> consumer) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ColorChooserButton ccb = (ColorChooserButton) c;
                ccb.addColorSelectedListener(consumer);
            }
        };
    }

    public static Configurator onItemStateChanged(ItemListener il) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JCheckBox) c).addItemListener(il);
            }
        };
    }

    /*
    This is probably not what I want. It receives events for all sorts of changes, including mouse over effects.
    See onItemStateChanged.
    */
    public static Configurator onStateChanged(ChangeListener cl) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JCheckBox) c).addChangeListener(cl);
            }
        };
    }

    public static Configurator onTextChanged(Consumer<String> handler) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JTextComponent) c).getDocument().addDocumentListener(new DocumentListener() {
                    private void update(DocumentEvent e) {
                        try {
                            handler.accept(e.getDocument().getText(0, e.getDocument().getLength()));
                        } catch (BadLocationException ex) {
                            new RuntimeException(ex);
                        }
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        update(e);
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        update(e);
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        // This is for attribute changes... ignore
                    }
                });
            }
        };
    }

    public static Configurator onFocusGained(FocusEventListener fel) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        fel.focusEvent(e);
                    }
                });
            }
        };
    }

    public static Configurator onFocusLost(FocusEventListener fel) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        fel.focusEvent(e);
                    }
                });
            }
        };
    }

    public static Configurator onWindowOpened(Consumer<WindowEvent> listener) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((Window) c).addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowOpened(WindowEvent e) {
                        listener.accept(e);
                    }
                });
            }
        };
    }

    public static Configurator onWindowClosing(Consumer<WindowEvent> listener) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((Window) c).addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        listener.accept(e);
                    }
                });
            }
        };
    }

    public static Configurator onWindowClosed(Consumer<WindowEvent> listener) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((Window) c).addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        listener.accept(e);
                    }
                });
            }
        };
    }

    public static Configurator emptyOnClose() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                if ( c instanceof JFrame ) {
                    JFrame frame = (JFrame) c;
                    if (!frame.getClass().isAssignableFrom(JFrame.class)) {
                        LOG.warn("potential memory leak. Cannot guarantee memory is freed after frame is disposed because" +
                            " JFrame has been subclassed to " + frame.getClass().getName());
                    }
                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            frame.removeWindowListener(this);
                            frame.removeAll();
                            frame.setContentPane(new JPanel());
                            frame.setJMenuBar(null);
                            frame.setTitle("disposed and emptied: "+frame.getTitle());
                            LOG.info("frame emptied: "+frame);
                        }
                    });
                } else if ( c instanceof JDialog ) {
                    JDialog dialog = (JDialog)c;
                    if (!dialog.getClass().isAssignableFrom(JDialog.class)) {
                        LOG.warn("potential memory leak. Cannot guarantee memory is freed after dialog is disposed " +
                            "because JDialog has been subclassed to " + dialog.getClass().getName());
                    }
                    dialog.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            dialog.removeWindowListener(this);
                            dialog.removeAll();
                            dialog.setContentPane(new JPanel());
                            dialog.setTitle("disposed and emptied: "+dialog.getTitle());
                        }
                    });
                } else {
                    LOG.warn("emptyOnClose not supported for " + c.getClass().getSimpleName());
                }
            }
        };
    }

    /**
     * There is a bug in Swing where memory is not freed, ever, from anything referenced by a Window. This includes
     * JFrame and JDialog. This configurator disassociates as many references as possible so that the Window does not
     * hold a reference to anything that may hold significant memory. The bug is not present on all platforms.
     * Read https://stackoverflow.com/questions/19781877/mac-os-java-7-jdialog-dispose-memory-leak
     * <p>
     *     I'm not clear on whether this bug is present in Java 9, scheduled to be released about a month from this
     *     writing.
     * </p>
     * @return
     */
    public static Object[] disposeAndEmptyOnClose() {
        return
            new Object[]{
                emptyOnClose(),
                new Configurator() {
                    @Override
                    protected void apply(Component c) {
                        if ( c instanceof JFrame ) {
                            ((JFrame)c).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                        } else if ( c instanceof JDialog ) {
                            ((JDialog)c).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                        }
                    }
                }
            };
    }

    public static Configurator disposeOnClose() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                if ( c instanceof JFrame ) {
                    ((JFrame) c).setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                } else if ( c instanceof JDialog ) {
                    ((JDialog) c).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                } else {
                    LOG.warn("component doesn't have supported type for disposeOnClose: "+c.getClass().getSimpleName());
                }
            }
        };
    }

    public static Configurator doNothingOnClose() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                if ( c instanceof JFrame ) {
                    ((JFrame) c).setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                } else if ( c instanceof JDialog ) {
                    ((JDialog) c).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                } else {
                    LOG.warn("component doesn't have supported type for doNothingOnClose: "+c.getClass().getSimpleName());
                }
            }
        };
    }

    public static Configurator onMouseClicked(MouseEventListener mel) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        mel.mouseEvent(e);
                    }
                });
            }
        };
    }

    public static Configurator onKeyPressed(KeyEventListener kl) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        kl.keyEvent(e);
                    }
                });
            }
        };
    }

    public static Configurator onAction(ActionListener al) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                invokeIfPresent(c, "addActionListener", new Class[]{ActionListener.class}, al);
            }
        };
    }

    public static Configurator onToggle(ChangeListener cl) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                if (c instanceof JToggleButton) {
                    ((JToggleButton) c).addChangeListener(cl);
                }
            }
        };
    }

    public static Configurator onSelect(ListSelectionListener lsl) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                JTable table = (JTable) c;
                table.getSelectionModel().addListSelectionListener(lsl);
            }
        };
    }

    public static Configurator select(Object item) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                JComboBox cb = (JComboBox) c;
                if (item instanceof String) {
                    for (int index = 0; index < cb.getItemCount(); index++) {
                        if (cb.getItemAt(index).toString().equals(item)) {
                            cb.setSelectedIndex(index);
                            break;
                        }
                    }
                } else {
                    cb.setSelectedItem(item);
                }
            }
        };
    }

    public static Configurator selectIndex(int index) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JComboBox) c).setSelectedIndex(index < -1 ? -1 : index);
            }
        };
    }

    public static Configurator selected(boolean selected) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((AbstractButton) c).setSelected(selected);
            }
        };
    }

    public static Configurator wraplines() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JTextArea) c).setLineWrap(true);
            }
        };
    }

    public static Configurator editable(boolean editable) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                invokeIfPresent(c, "setEditable", new Class[]{boolean.class}, new Object[]{editable});
            }
        };
    }

    public static Configurator disabled() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.setEnabled(false);
            }
        };
    }

    public static Configurator enabled(boolean enabled) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.setEnabled(enabled);
            }
        };
    }

    public static Configurator enabled() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.setEnabled(true);
            }
        };
    }

    public static Configurator focusable(boolean aFocusable) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.setFocusable(aFocusable);
            }
        };
    }

    public static Configurator margin(int top, int left, int bottom, int right) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                invokeIfPresent(c, "setMargin", new Class[]{Insets.class}, new Insets(top, left, bottom, right));
            }
        };
    }

    private static void safeSetBorder(Component c, Border border) {
        if (c instanceof JPanel || c instanceof JLabel) {
            ((JComponent) c).setBorder(border);
        } else if ( c instanceof AbstractButton && border == null) {
            ((AbstractButton) c).setBorderPainted(false);
        } else {
            System.err.println("WARNING: Skipping request to set " +
                "border to \"none\" on " + c.getClass().getName());
            System.err.println("WARNING: The Java API doesn't " +
                "recommend setting borders on components other than " +
                "JPanel and JLabel: " +
                "https://docs.oracle.com/javase/8/docs/api/javax/swing/JComponent.html#setBorder-javax.swing.border.Border-");
        }
    }

    public static Configurator border(Border b) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                safeSetBorder(c, b);
            }
        };
    }

    public static Configurator emptyborder(int top, int left, int bottom, int right) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                safeSetBorder(c, BorderFactory.createEmptyBorder(top, left, bottom, right));
            }
        };
    }

    public static Configurator noborder() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                if (c instanceof AbstractButton) {
                    ((AbstractButton) c).setBorderPainted(false);
                } else {
                    safeSetBorder(c, BorderFactory.createEmptyBorder());
                }
            }
        };
    }

    public static Configurator resizeWeight(double weight) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JSplitPane) c).setResizeWeight(weight);
            }
        };
    }

    public static Configurator floatable() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JToolBar) c).setFloatable(true);
            }
        };
    }

    public static Configurator notfloatable() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JToolBar) c).setFloatable(false);
            }
        };
    }

    public static Configurator transparent() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                //if (c instanceof AbstractButton) {
                    LOG.info("setting content area filled to false");
                    ((AbstractButton) c).setContentAreaFilled(false);
                //} else if (c instanceof JComponent) {
                //    ((JComponent)c).setOpaque(false);
                //}
            }
        };
    }

    public static Configurator background(Color color) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.setBackground(color);
            }
        };
    }

    public static Configurator color(Color color) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                c.setForeground(color);
            }
        };
    }

    public static Configurator horizontalScrollBarPolicy(int policy) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JScrollPane) c).setHorizontalScrollBarPolicy(policy);
            }
        };
    }

    public static Configurator verticalScrollBarPolicy(int policy) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JScrollPane) c).setVerticalScrollBarPolicy(policy);
            }
        };
    }

    public static Configurator buttonGroup(ButtonGroup buttonGroup) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                AbstractButton button = (AbstractButton) c;
                buttonGroup.add(button);
            }
        };
    }

    public static Configurator autoResizeOff() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                JTable table = (JTable) c;
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            }
        };
    }

    public static Configurator selectRow(Integer row) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                JTable table = (JTable) c;
                if (row!=null) {
                    table.getSelectionModel().setSelectionInterval(row, row);
                } else {
                    table.clearSelection();
                }
            }
        };
    }

    public static Configurator data(java.util.List data) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                JTable table = (JTable) c;
                SBTableModel model = (SBTableModel) table.getModel();
                model.setData(data);
            }
        };
    }

    public static Configurator row(Function<Object, Object[]> rowFn) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                JTable table = (JTable) c;
                SBTableModel model = (SBTableModel) table.getModel();
                model.setRowFn(rowFn);
            }
        };
    }

    public static Configurator columnNames(java.util.List<String> names) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                JTable table = (JTable) c;
                SBTableModel model = (SBTableModel) table.getModel();
                model.setColumnNames(names);
            }
        };
    }

    public static Configurator renderer(ListCellRenderer renderer) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                if (c instanceof JComboBox) {
                    JComboBox comboBox = (JComboBox) c;
                    comboBox.setRenderer(renderer);
                } else if (c instanceof JList) {
                    JList list = (JList) c;
                    list.setCellRenderer(renderer);
                } else {
                    new RuntimeException("could not set cell renderer");
                }
            }
        };
    }

    public static Configurator renderer(Class<?> klass, TableCellRenderer renderer) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                JTable table = (JTable) c;
                table.setDefaultRenderer(klass, renderer);
            }
        };
    }

    /**
     * Sets the items for a JComboBox or a JList. The two perform differently.
     * <p>For JComboBox:
     * if items[0] is an array, List, or Set then each element will be added as an item in this combo box. Otherwise,
     * each items[i] will be added as an item in the combo box. Strings are wrapped in an anonymous object to allow
     * for multiple strings with the same value because ProcessDB requires this to work properly.
     * </p>
     * <p>For JList: Like JComboBox but no string wrapping occurs.</p>
     *
     * @param items
     * @return
     */
    public static Configurator items(Object... items) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
                DefaultListModel listModel = new DefaultListModel();
                Consumer elementAdder = null;
                boolean wrapStrings = false;
                if (c instanceof JComboBox) {
                    wrapStrings = true;
                    elementAdder = comboBoxModel::addElement;
                } else if (c instanceof JList) {
                    elementAdder = listModel::addElement;
                }
                Object firstItem = items[0];
                Iterable iter = null;
                if (firstItem instanceof Object[]) {
                    iter = Arrays.asList((Object[]) firstItem);
                }
                if (firstItem instanceof java.util.List || firstItem instanceof Set) {
                    iter = (Iterable) firstItem;
                }
                if (iter == null) {
                    iter = Arrays.asList(items);
                }
                for (Object item : iter) {
                    if (wrapStrings && item instanceof String) item = wrapString((String) item);
                    elementAdder.accept(item);
                }
                if (c instanceof JComboBox) {
                    ((JComboBox) c).setModel(comboBoxModel);
                } else if (c instanceof JList) {
                    ((JList) c).setModel(listModel);
                }
            }
        };
    }

    public static Configurator preferLeft() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JComponent)c).setAlignmentX(Component.LEFT_ALIGNMENT);
            }
        };
    }

    public static Configurator preferVCenter() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((JComponent)c).setAlignmentY(Component.CENTER_ALIGNMENT);
            }
        };
    }

    // TODO: this should probably be alignTextLeft or alignContentsLeft, Why? Because there's also a
    //  Component.setAlignmentX, which sets the components preferred horizontal alignment... within the parent?
    //  Same for alignCenter and alignVCenter
    public static Configurator alignLeft() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                if ( c instanceof JLabel ) {
                    JLabel label = (JLabel)c;
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                }
            }
        };
    }

    public static Configurator alignCenter() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                if ( c instanceof JLabel ) {
                    JLabel label = (JLabel)c;
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                }
            }
        };
    }

    public static Configurator alignVCenter() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                if ( c instanceof JButton) {
                    JButton b = (JButton)c;
                    b.setVerticalTextPosition(SwingConstants.CENTER);
                    b.setVerticalAlignment(SwingConstants.CENTER);
                }
            }
        };
    }

    public static Configurator modal() {
        return modal(true);
    }

    public static Configurator modeless() {
        return modal(false);
    }

    public static Configurator modal(boolean m) {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                Dialog.ModalityType type = m ? Dialog.DEFAULT_MODALITY_TYPE : Dialog.ModalityType.MODELESS;
                ((Dialog) c).setModalityType(type);
            }
        };
    }

    public static Configurator alwaysOnTop() {
        return new Configurator() {
            @Override
            protected void apply(Component c) {
                ((Window) c).setAlwaysOnTop(true);
            }
        };
    }

    public static OwnerConfigurator owner(Window owner) {
        return new OwnerConfigurator(owner);
    }

    public static OwnerConfigurator getOwnerConfigurator(Object[] configuration) {
        for (Object object : configuration) {
            if (object instanceof OwnerConfigurator) {
                return (OwnerConfigurator) object;
            }
            if (object instanceof Object[]) {
                OwnerConfigurator ownerConfigurator = getOwnerConfigurator((Object[]) object);
                if (ownerConfigurator != null) return ownerConfigurator;
            }
        }
        return null;
    }

    private static Object wrapString(final String str) {
        return new Object() {
            @Override
            public String toString() {
                return str;
            }
        };
    }

    /**
     * If method with <code>name</code> is present, invoke it.  Otherwise, do nothing.
     * When searching for the method, the leaf of the class hierarchy is used and the
     * method arguments must also match <code>argTypes</code>.
     *
     * @param c
     * @param name
     * @param argTypes
     * @param args
     */
    protected static void invokeIfPresent(Component c, String name, Class[] argTypes, Object... args) {
        try {
            Method method;
            method = c.getClass().getMethod(name, argTypes);
            method.invoke(c, args);
        } catch (NoSuchMethodException e) {
            // Not an error... just don't call the not present method.
            LOG.info("method not present: " + name);
        } catch (InvocationTargetException | IllegalAccessException e) {
            // This may not be an error either, but let's let the developer know.
            LOG.warn("The following exception may be okay, but it also may be unexpected.  " +
                "Please verify that the method \"" + name + "\" can be called on " + c.getClass().getName() +
                " and that is your intent.", e);
        }
    }

    public interface FocusEventListener {
        void focusEvent(FocusEvent me);
    }

    public interface MouseEventListener {
        void mouseEvent(MouseEvent me);
    }

    public interface KeyEventListener {
        void keyEvent(KeyEvent ke);
    }

    public static class OwnerConfigurator extends Configurator {
        private Window owner;

        public OwnerConfigurator(Window owner) {
            this.owner = owner;
        }

        public Window getOwner() {
            return owner;
        }

        @Override
        protected void apply(Component c) {
            ((JDialog) c).setLocationRelativeTo(owner);
        }
    }

    public static abstract class Configurator {
        public final void applyOnEDT(Component c) {
            if (SwingUtilities.isEventDispatchThread()) {
                apply(c);
            } else {
                try {
                    SwingUtilities.invokeAndWait(() -> apply(c));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        /**
         * Do not call this method directly... it should be called within SwingBuilder, only.
         * The intent of this method is to be overridden by a configurator that sets some
         * properties on a Swing component that requires running on the EDT.  SwingBuilder is
         * designed to properly call this method without any effort by you.
         *
         * @param c
         */
        protected abstract void apply(Component c);
    }
}
