package com.insilicalabs.swingbuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.util.Collection;
import java.util.function.Function;

import static com.insilicalabs.swingbuilder.Configurators.border;
import static com.insilicalabs.swingbuilder.Configurators.buttonGroup;
import static com.insilicalabs.swingbuilder.Configurators.contents;
import static com.insilicalabs.swingbuilder.Configurators.layout;
import static com.insilicalabs.swingbuilder.Configurators.leftComponent;
import static com.insilicalabs.swingbuilder.Configurators.rightComponent;
import static com.insilicalabs.swingbuilder.Configurators.text;
import static com.insilicalabs.swingbuilder.Configurators.viewport;
import static com.insilicalabs.swingbuilder.Creators.dummy;

/**
 * Created by jzwolak on 8/12/15.
 */
public class SwingBuilderBase {

    private final static Logger LOG = LoggerFactory.getLogger(SwingBuilderBase.class);
    protected final static String DUMMY_COMPONENT_TAG = "dummy component GUID tag: ad23cdca-bb81-4552-b9d9-b80fb21a6730";
    protected final static Configurators.Configurator NOOP = new Configurators.Configurator() {
        @Override
        protected void apply(Component c) {
            // no operation because this is the NOOP configurator
        }
    };

    public static Object includeWhen(boolean condition, Object... components) {
        if (condition) {
            return components;
        } else {
            return dummy();
        }
    }

    public static <T extends Component> T configure(T component, Object... configuration) {
        return configure(component, Function.identity(), configuration);
    }

    public static <T extends Component> T configure(T component, Function mapFunction, Object... configuration) {
        for (Object obj : configuration) {
            if (obj instanceof Configurators.Configurator) {
                ((Configurators.Configurator) obj).applyOnEDT(component);
            } else if (obj instanceof Object[]) {
                configure(component, mapFunction, (Object[]) obj);
            } else if (obj instanceof Collection) {
                configure(component, mapFunction, ((Collection)obj).toArray());
            } else {
                Object newObject = mapFunction.apply(obj);
                if (newObject != obj) {
                    configure(component, Function.identity(), newObject);
                } else {
                    LOG.warn("unknown object in configuration, perhaps a configurator is missing: " + obj, new Exception("stack trace for warning"));
                }
            }
        }
        return component;
    }

    public static JSplitPane configure(JSplitPane sp, Object... configuration) {
        configure(sp, (e) -> {
            if (e instanceof Component) {
                if (isDummy(sp.getLeftComponent())) {
                    return leftComponent((Component) e);
                } else if (isDummy(sp.getRightComponent())) {
                    return rightComponent((Component) e);
                } else {
                    System.err.println("WARNING: Did not add component to split pane because both panes already have a component.");
                }
            }
            return e;
        }, configuration);
        return sp;
    }

    public static JLabel configure(JLabel label, Object... configuration) {
        configure(label, (e) -> {
            if (e instanceof String) return text((String) e);
            return e;
        }, configuration);
        return label;
    }

    public static JTextField configure(JTextField textField, Object... configuration) {
        configure(textField, (e) -> {
            if (e instanceof String) return text((String) e);
            return e;
        }, configuration);
        return textField;
    }

    public static JScrollPane configure(JScrollPane sp, Object... configuration) {
        configure(sp, e -> {
            if (e instanceof Component) {
                return viewport((Component) e);
            }
            return e;
        }, configuration);
        return sp;
    }

    public static <T extends AbstractButton> T configure(T button, Object... configuration) {
        configure(button, (e) -> {
            if (e instanceof String) {
                return text((String) e);
            } else if (e instanceof ButtonGroup) {
                return buttonGroup((ButtonGroup) e);
            }
            return e;
        }, configuration);
        return button;
    }

    public static JPanel configure(JPanel p, Object... configuration) {
        configure(p, (e) -> {
                if (e instanceof Component) {
                    return contents(e);
                } else if (e instanceof LayoutManager || e instanceof LayoutManager2) {
                    return layout((LayoutManager) e);
                } else if (e instanceof Border) {
                    return border((Border) e);
                }
                return e;
            },
            configuration);
        return p;
    }

    static boolean isDummy(Object c) {
        return c instanceof JLabel && DUMMY_COMPONENT_TAG.equals(((JLabel) c).getText());
    }

}
