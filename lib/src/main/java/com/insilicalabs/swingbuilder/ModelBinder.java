package com.insilicalabs.swingbuilder;

import javax.swing.JComponent;
import java.awt.Component;
import java.util.ArrayList;
import java.util.function.Function;

/**
 * Created by jzwolak on 3/14/16.
 */
public class ModelBinder<T> {

    private T model;
    private java.util.List<Binding> bindings = new ArrayList<>();

    public ModelBinder(T model) {
        this.model = model;
    }

    public T getModel() {
        return model;
    }

    public void setModel(T model) {
        this.model = model;
        update();
    }
    
    public void updateModel(Function<T, T> updateFn) {
        this.model = updateFn.apply(this.model);
        update();
    }

    public void update() {
        for (Binding binding : bindings) {
            binding.update();
        }
    }

    private void registerBinding(Component c, Function<T, Configurators.Configurator> fn) {
        bindings.add(new Binding(c, fn));
    }

    public Configurators.Configurator bind(Function<T, Configurators.Configurator> function) {
        return new Configurators.Configurator() {
            @Override
            protected void apply(Component c) {
                registerBinding(c, function);
                function.apply(model).applyOnEDT(c);
            }
        };
    }

    private class Binding {
        private final Component component;
        private final Function<T, Configurators.Configurator> function;

        public Binding(Component c, Function<T, Configurators.Configurator> fn) {
            this.component = c;
            this.function = fn;
        }

        public void update() {
            function.apply(model).applyOnEDT(component);
        }
    }

    public interface ModelBoundCreator<T> {
        JComponent create(ModelGetter<T> model);
    }

    public interface ModelGetter<T> {
        T get();
    }

}
