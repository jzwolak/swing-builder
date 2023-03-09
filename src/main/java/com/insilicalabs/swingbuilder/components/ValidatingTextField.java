package com.insilicalabs.swingbuilder.components;

import com.insilicalabs.swingbuilder.Configurators;
import com.insilicalabs.swingbuilder.SwingBuilderBase;

import javax.swing.JTextField;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static com.insilicalabs.swingbuilder.components.ValidatingTextField.Converter.NULL_CONVERSION;
import static com.insilicalabs.swingbuilder.components.ValidatingTextField.Validator.ALWAYS_VALID;

/**
 * Created by jzwolak on 9/28/16.
 */
@SuppressWarnings("all")
public class ValidatingTextField<T> extends JTextField {

    private Validator validator = ALWAYS_VALID;
    private Converter<T> converter = NULL_CONVERSION;
    private Color DEFAULT_COLOR_WHEN_INVALID = Color.RED;
    private Color colorWhenValid=null;
    private boolean shouldHighlightWhenInvalid;
    
    public ValidatingTextField(Converter<T> converter) {
        this((input)->{
            try {
                converter.convert(input);
                return true;
            } catch (Exception ex) {
                return false;
            }
        },converter);
    }

    public ValidatingTextField(Validator validator, Converter<T> converter) {
        this(validator,converter,false);
    }
    
    public ValidatingTextField(Validator validator, Converter<T> converter, boolean aShouldColorWhenInvalid) {
        this.validator = validator;
        this.converter = converter;
        setShouldHighlightWhenInvalid(aShouldColorWhenInvalid);
    }

    public T getValue() {
        return converter.convert(getText());
    }

    public boolean hasValidInput() {
        return validator.isValid(getText());
    }
    
    public void setShouldHighlightWhenInvalid(boolean aShouldHighlightWhenInvalid) {
        if ( shouldHighlightWhenInvalid == aShouldHighlightWhenInvalid ) return;
        shouldHighlightWhenInvalid = aShouldHighlightWhenInvalid;
        if (shouldHighlightWhenInvalid) {
            colorWhenValid = getBackground();
            this.addKeyListener(colorWhenInvalidListener);
        } else {
            this.removeKeyListener(colorWhenInvalidListener);
            if ( colorWhenValid != null ) setBackground(colorWhenValid);
        }
    }
    
    private void colorOnValid() {
        setBackground(hasValidInput()?colorWhenValid:DEFAULT_COLOR_WHEN_INVALID);
    }
    
    private KeyListener colorWhenInvalidListener = new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            colorOnValid();
        }
    };
    
    public static Configurators.Configurator highlightWhenInvalidInput() {
        return new Configurators.Configurator() {
            @Override
            protected void apply(Component c) {
                ValidatingTextField vtf = (ValidatingTextField)c;
                vtf.setShouldHighlightWhenInvalid(true);
            }
        };
    }

    public interface Validator {
        boolean isValid(String input);
        Validator ALWAYS_VALID = (input) -> true;
    }

    public interface Converter<O> {
        O convert(String input);
        Converter NULL_CONVERSION = (input) -> input;
    }

    public static ValidatingTextField<Double> doublefield(Object... configuration) {
        ValidatingTextField<Double> textField = new ValidatingTextField(
            (input) -> Double.parseDouble(input)
        );
        textField.setText("0.0");
        return (ValidatingTextField<Double>) SwingBuilderBase.configure(textField, configuration);
    }

    public static ValidatingTextField<Integer> integerfield(Object... configuration) {
        ValidatingTextField<Integer> textField = new ValidatingTextField(
            (input) -> Integer.parseInt(input)
        );
        textField.setText("0");
        return (ValidatingTextField<Integer>) SwingBuilderBase.configure(textField, configuration);
    }

    /**
     * This field supports a proprietary format for integer input text that helps users with large integers. The 
     * format is <i>&lt;int&gt;((e|E)&lt;int&gt;)?</i>. In laymen's terms: an integer optionally followed by a case 
     * insensitive "e" and another integer. This is scientific notation without the possibility of a decimal place. 
     * There is the additional restriction that the integer after the "e" must be unsigned (zero or greater).
     * @param configuration
     * @return
     */
    public static ValidatingTextField<Integer> largeintegerfield(Object... configuration) {
        Converter<Integer> largeIntegerConverter = new Converter<Integer>() {
            @Override
            public Integer convert(String input) {
                try {
                    Integer output = Integer.parseInt(input);
                    return output;
                } catch (NumberFormatException ex) {
                    // do nothing, we'll try the next format later.
                }
                String[] parts = input.split("[eE]");
                String digits = parts[0];
                String tensPower = parts[1];
                if ( parts.length > 2 ) {
                    throw new NumberFormatException("expected a single \"e\" and found more than one: "+input);
                }
                Integer tensPowerI = Integer.parseInt(tensPower);
                if ( tensPowerI < 0 ) {
                    throw new NumberFormatException("tens power must be >=0: "+tensPowerI);
                }
                return Integer.parseInt(digits)*(int)Math.pow(10,tensPowerI);
            }
        };
        ValidatingTextField<Integer> textField = new ValidatingTextField(
            (input) -> largeIntegerConverter.convert(input)
        );
        textField.setText("0");
        return (ValidatingTextField<Integer>) SwingBuilderBase.configure(textField, configuration);
    }

}
