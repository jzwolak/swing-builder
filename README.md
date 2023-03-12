# Swing Builder
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/jzwolak/swing-builder/blob/main/LICENSE)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.insilicalabs/swing-builder/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.insilicalabs/swing-builder)



Swing Builder is a DSL defined completely within Java to make user interface definition code flow hierarchically like
the user interface itself and be more declarative.

For example,

```java
import static com.insilicalabs.swingbuilder.Configurators.*;
import static com.insilicalabs.swingbuilder.Creators.*;

public class HelloWorld {
    public static void main(String[] args) {
        frame(
            contents(
                label("Hello World!")
            ),
            pack(),
            show()
        );
    }
}
```

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
* Swing Builder is stable. It's been used in production code since 2014 and the core architecture is unchanged since
  then.

## Why Not?

Well... Let's be honest here, there are reasons not to use Swing Builder. As you'll see or have seen, the DSL is
limited by Java. Here are some short commings that aren't present in other DSLs (React, HTML, etc.; but note that those
DSLs aren't available in pure Java or 100% [interoperable](#legacy-interoperability) with legacy code,
See [Why?](#why)).

* Code within the hierarchy cannot contain forward references to other components within the hierarchy without those
  components being defined outside the hierarchy first (they can then, of course, be added to the hierarchy).
  See [Referencing Components](#referencing-components).
* Lack of view-as-a-function-of-state. When I set out to write Swing Builder I wanted React in Java. In fact, I didn't
  want to write anything new, I wanted to use something that existed and I explored many options. Nothing was working
  in Java and providing the kind of view-as-a-function-of-state experience I wanted. Swing Builder failed to reach this,
  too. I got closer than anything else I saw, but I still did not achieve this goal in pure Java.
* New projects probably should not use Swing Builder, or Swing for that matter. Swing Builder is targeting existing
  Swing based projects in order to help make the codebase a bit more modern in its maintainability, readability,
  and robustness. New projects have choices far superior using tools based on React, JavaFX, Electron, and others. I
  couldn't use those, so I made Swing Builder.
* Not all Swing components and methods are mapped. However, you can contribute to the project or extend Swing Builder
  yourself. The reason for not mapping all components and methods is simply that I haven't used them all and so I have
  had no need to map them all.
* There's more... I just don't feel as inspired to write about the shortcomings as I do the amazingingness. I use Swing
  Builder everyday for an enterprise application and every time I write code in it compared to times I enter parts of
  the code base written in various other UI creation tools or plain Swing... I breathe a breath of fresh air. It's no
  React, but if you're working with legacy code in Java, I think it's a super tool.

## Legacy Interoperability

Say you have legacy code and wish to add something to it or add it to a new Swing Builder created UI. Here are two
examples illustrating both cases.

Add a Swing Builder panel to a legacy panel.

```java
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
);
```

Add a legacy panel to a Swing Builder panel.

```java
JPanel legacyPanel = ...
JPanel sbPanel = panel(
    label("Here's the legacy panel you requested:"),
    legacyPanel
);
```

You might be wondering why `contents` appears in the first example and not the second. In fact, it doesn't need to
appear in the first example, either. It's optional. I put `contents` in the first example so that I may reference it
and talk about what it does. The `configure` function is called implicitly from within the `panel` function. In this
case, the `configure` function will automatically detect any `Component`s in its argument list and pass them to an
implicit call to the `contents` function because a `JPanel` is being configured. When in doubt, explicitly use the
`contents` function. See [Default Configurators](#default-configurators).

## State

Swing Builder provides a builtin class, `ModelBinder`, for handling arbitrary application state and syncing that state
with view components. This is not a panacea and leaves much room for improvement. However, I have found it does well in
many cases, is substantially better than nothing, and has not left me wanting enough to write a better solution, yet. I
see it's flaws clearly, though.

Here's a basic example of how to use state.

```java
ModelBinder<String> name = new ModelBinder<>("");

JLabel greetingLabel = label();

ActionListener greetingHandler = (actionEvent) -> {
    greetingLabel.setText("Hi "+name.getModel()+"!");
    name.setModel("");
};


frame(
    contents(
        layout(migLayout("flowy", "", "")),
        greetingLabel,
        textField(
            minimumSize( 100, 0),
            name.bind( (newText) -> text(newText) ),
            onTextChange( (newText) -> name.setModel(newText) ),
            onAction( greetingHandler )
        ),
        button(
            text( "Greet" ),
            onAction( greetingHandler )
        )
    ),
    pack(),
    center(),
    show()
);
```

The most glaring flaw with `ModelBinder` is its lack of understanding of mutable structures. It works well with
immutable structures like `String`, `Integer`, and the unmodifiable collections, but if a value changes within an
object, then `ModelBinder` has no way to know that the value changed and update the view. To handle this, you must call
`ModelBinder.update` after modifying a mutable object to let `ModelBinder` know to update the views it's bound to. A
better solution might be to require immutable data structures, though that may be another problem when trying to
target legacy applications, which is a goal of Swing Builder. So... this is something to consider another day.

## Architecture

Swing Builder divides actions into two basic categories: creators and configurators. There is a class for each of those:
`Creators` and `Configurators`. Inside you'll find many very short static functions that act as wrappers to Java Swing.
You may use this as a sort of self documentation when you want to figure out what's supported. Search for your favorite
Swing method or class in there to find the Swing Builder equivalent.

You'll note that I use different language than is customary in Java. I use "functions" instead of "methods". I'll also
say "create" instead of "instantiate". I'm doing this intentionally and consciously in order to shift away from an
object oriented way of thinking to a functional way of thinking and, in particular, a Swing Builder way of thinking.

`Creators` contains functions to create Swing components and these are created immediately on calling the creator. The
naming of the functions mirrors the component names with the following rules:

* remove the preceding "J",
* using leading lowercase, and
* maintain camelcase.

`Configurators` contains functions that declaratively define how the component should be configured. They _do not_
change the state of the component when they are called. Instead, they return what is called a `Configurator`. A
`Configurator` may be applied to the component (or many components, or the same component many times) in order to change
the component's state. A configurator of `background(Color.RED)` can be called all by itself without any component and
then applied to many components. Like this:

```java
Configurator redBackground = background(Color.RED);

JPanel myPanel = panel(
    label("I'm red!", redBackground),
    label("I'm not. :-("),
    label("I'm read, too!", redBackground)
);
```

A configurator, in this way, defines what should happen to a component, but does not do anything in itself.

When _does_ a `Configurator` get executed, you might be asking!? Good question! Creators execute all configurators
before they return. However, it is also possible to execute configurations on existing components or already configured
components using the `configure` function, like this:

```java
JLabel aLabel = label("Am I red?");
configure(aLabel, redBackground);
```

Or even something like this:

```java
JPanel myPanel = panel(
    aLabel = label("ooo... nice")
);

configure(aLabel, redBackground);
```

### Composite Configurations

A configuration can also be an `Object[]` or a `Collection`. Each object in the array or collection may in turn also be
an `Object[]` or `Collection` nested arbitrarily deep to the limit of the JVM stack (recursion is used to unpack the
nesting). In practice, it should be unlikely that 2-3 levels of depth ever happen, but if you automate creation of
configurations, maybe you'll create some seriously deep configurations.

For example:

```java
Object[] labelConfiguration = new Object[] {
    color(Color.YELLOW),
    background(COLOR.RED)
};

JPanel myPanel = panel(
    layout( migLayout("flowy", "", "") ),
    label("I was told this contrast would get your attention.", labelConfiguration),
    label("Yeah, but it hurts my eyes.", labelConfiguration)
);
```

### Default Configurators

You'll notice sometimes there are things being passed to the creator functions that are not configurators. In fact,
everything goes through a configurator. If what you see is not a configurator then the `configure` function will attempt
to find a default configurator based on the object type and the creator type. For instance, when using the `label` and
`button` creators there is a default configurator for `String` objects of `text`. When using the `panel` creator there
is a default configurator for `Component` objects of `contents`. And in fact, this mechanism is extensible.

Every creator calls its respective configure function (if there is one other than the default). That configure function
then calls the fully specified `configure` function with a custom `mapFunction`:

```java
public static <T extends Component> T configure(T component, Function mapFunction, Object... configuration) {
    ...
}
```

The `mapFunction` handles anything in `configuration` that is _not_ a `Configurator`, an `Object[]`, or a `Collection`.
You may wish to read the `configure` function. It's quite short and can explain this rather well. The `mapFunction` must
map its input to a `Configurator` because once it is called the return value will then be used as a raw `Configurator`
and not sent through the checks again for mapping, `Object[]`, and `Collection`.

### Extending

I encourage you to extend Swing Builder with your own configurators, creators, mapping functions, etc. for your specific
project. I have done so in my projects; Swing Builder is designed to be extensible. However, if you find there are Swing
components and methods not wrapped that you would like wrapped (and there are many), please consider submitting a pull
request.

## Antipatterns

In this section some antipatterns are covered as well as good patterns to use in their place.

### Referencing Components

It's not possible to have forward references to other components within the hierarchy without first defining them
outside the hierarchy. Though, it is possible to create a reference to a component within the hierarchy and use that
reference after it has been created. Here's an example.

```java
JTextField nameField;
JTextField addressField;
JTextField phoneField = textField(); // <- antipattern

JPanel form = panel(
    nameField = textField(),
    button( onAction( (actionEvent) -> {
        System.out.println(nameField.getText());    // <- works
        System.out.println(addressField.getText()); // <- fails!
        System.out.println(phoneField.getText());   // <- works, antipattern
    } )),
    addressField = textField(),
    phoneField
);
```

The above example shows `nameField` defined within the hierarchy before it is used and this works. The `addressField`
is defined after it is used and this fails. The `phoneField` illustrates how to get around the problem of placing a
component in the hierarchy _after_ it's used by defining it outside the hierarchy. In principle there is nothing wrong
with this, but if it is done specifically to get around this use problem... there's a better way.

In fact, getting state from components by calling them directly is generally an antipattern. Instead, components'
internal state (text, toggle state, selected state, etc.) should be bound to application state and it's the application
state that should be referenced. Here's the good pattern to use instead of the above antipattern.

```java
ModelBinder<String> name = new ModelBinder<>("");
ModelBinder<String> address = new ModelBinder<>("");
ModelBinder<String> phone = new ModelBinder<>("");

JPanel form = panel(
    textField( onTextChange( (text) -> name.setModel(text) ) ),
    button( onAction( (actionEvent) -> {
        System.out.println(name.getModel());
        System.out.println(address.getModel());
        System.out.println(phone.getModel());
    })),
    textField( onTextChange( (text) -> address.setModel(text) ) ),
    textField( onTextChange( (text) -> phone.setModel(text) ) )
);
```

The application state is now separate from the view. Perhaps more importantly, updates to the application state and the
view can be dynamically synced.


## Getting Started

Include the library from Maven central. If using Gradle, something like the following will work (assuming you've
included the Maven Central repository):

```
dependencies {
    implementation 'com.insilicalabs:swing-builder:0.1.1'
}
```

Include the necessary (or all) configurators and creators and start using. See the above examples on how to include and
use the classes and static methods (functions).
