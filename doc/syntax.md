# Syntax

Hiccup provides a concise syntax for representing HTML using Clojure
data structures. This syntax is not only used by the Hiccup library
itself, but also many other tools in the Clojure ecosystem.


## Basic syntax

Vectors that begin with a keyword represent HTML elements. For
example:

```clojure
[:span]
```

Produces:

```html
<span></span>
```

Any value placed after the keyword is treated as the body of the
element. This allows elements to be nested. For example:

```clojure
[:div [:span] [:span]]
```

```html
<div>
  <span></span>
  <span></span>
</div>
```

Strings are rendered as plain text. So:

```clojure
[:span "Hello world"]
```

```html
<span>Hello world</span>
```

And adjacent strings are concatenated together.

```clojure
[:span "foo" "bar"]
```

```html
<span>foobar</span>
```

A map can be optionally specified as the second value in the
vector. This map represents the attributes of the element. The keys
should be keywords, and the values should be strings.

```clojure
[:span {:class "foo"} "Hello world"]
```

```html
<span class="foo">Hello world</span>
```

## Syntax sugar

Beyond the basic syntax, Hiccup supplies additional syntax sugar that
can make your code more concise.

### Objects

Values of types that have no special meaning are converted into
strings before being rendered. So for example:

```clojure
[:span 42]
```

Is equivalent to:

```clojure
[:span "42"]
```

### Seqs

Seqs (including lists) are automatically expanded out. This means that
an structure like:

```clojure
[:span '("foo" "bar")]
```

Is equivalent to:

```clojure
[:span "foo" "bar"]
```

This is particularly useful for iteration. For example:

```clojure
[:ul (for [i (range 3)] [:li i])]
```

```html
<ul>
  <li>0</li>
  <li>1</li>
  <li>2</li>
</ul>
```

### CSS selectors

The `#` character can be used to concisely specify the `id`
attribute. For example:

```clojure
[:span#foo "Hello world"]
```

```html
<span id="foo">Hello world</span>
```

While the `.` character can be used to specify the `class` attribute.

```clojure
[:span.foo "Hello world"]
```

```html
<span class="foo">Hello world</span>
```

These two syntaxes can be combined to produce elements with both an id
and one or more classes.

```clojure
[:span#foo.bar.baz "Hello world"]
```

```html
<span id="foo" class="bar baz">Hello world</span>
```
