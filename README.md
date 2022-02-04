# Swing Builder
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/jzwolak/swing-builder/blob/main/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.insilicalabs/swing-builder.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.insilicalabs%22)


A DSL defined completely within Java to make user interface definition code flow hierarchically like the user interface
and be more declarative.

    import static com.insilicalabs.swingbuilder.Creators.*;
    import static com.insilicalabs.swingbuilder.Configurators.*;
    
    public class HelloWorld {
        public static void main(String[] args) {
            frame(
                panel(
                    label("Hello World!")
                ),
                pack(),
                show()
            )
        }
    }

Yes, that really works in _pure Java_.

## Why?

* As UIs get more complex, it's important to be able to see where the UI relates to the code and vice versa.
* This is 100% _interoperable_ with existing legacy applications! See below for examples.
* Did I mention this is 100% interoperable with existing legacy applications? That means you can integrate it slowly,
  as time permits, into your project. Writing all new code in Swing Builder with no fear of non-interoperability with
  legacy code.
* Writing UIs is _fast_ compared to any other way of creating Swing UIs I've tried.
* UIs are maintainable. No need to wrangle with XML, JSON, or proprietary tools for converting those to Java.
* UI code doesn't break as IDE tools progress (or don't) or development team changes IDEs. Ever use Netbeans GUI builder
  anyone?

## Legacy Interoperability

Say you have legacy code and wish to add something to it or add it to a new Swing Builder created UI. Here are two
examples illustrating both cases.

Add a Swing Builder panel to a legacy panel.

    // The legacy panel is created by whatever legacy code...
    JPanel legacyPanel = ...
    // now configure legacy panel with swing builder code. `contents` will "append" (by calling `JPanel.add`)
    // components.
    configure( legacyPanel,
        contents(
            panel(
                label("All the goodies go here.")
            )
        )
    )

Add a legacy panel to a Swing Builder panel.

    JPanel legacyPanel = ...
    JPanel sbPanel = panel(
        label("Here's the legacy panel you requested:"),
        legacyPanel
    );

You might be wondering why `contents` appears in the first example and not the second. In fact, it doesn't need to
appear in the first example, either. It's optional. I put `contents` in the first example so that I may reference it
and talk about what it does. The `configure` function is called implicitly from within the `panel` function. The
`configure` function will automatically detect any `Component`s in its argument list and pass them to an implicit call
to the `contents` function.
