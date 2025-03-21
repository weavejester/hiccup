# Hiccup [![Build Status](https://github.com/weavejester/hiccup/actions/workflows/test.yml/badge.svg)](https://github.com/weavejester/hiccup/actions/workflows/test.yml)

Hiccup is a library for representing HTML in Clojure. It uses vectors
to represent elements, and maps to represent an element's attributes.

## Install

Add the following dependency to your `deps.edn` file:

    hiccup/hiccup {:mvn/version "2.0.0-RC5"}

Or to your Leiningen `project.clj` file:

    [hiccup "2.0.0-RC5"]

## Documentation

* [Wiki](https://github.com/weavejester/hiccup/wiki)
* [API Docs](http://weavejester.github.io/hiccup)

## Syntax

Here is a basic example of Hiccup syntax:

```clojure
user=> (require '[hiccup2.core :as h])
nil
user=> (str (h/html [:span {:class "foo"} "bar"]))
"<span class=\"foo\">bar</span>"
```

The first element of the vector is used as the element name. The second
attribute can optionally be a map, in which case it is used to supply
the element's attributes. Every other element is considered part of the
tag's body.

Hiccup is intelligent enough to render different HTML elements in
different ways, in order to accommodate browser quirks:

```clojure
user=> (str (h/html [:script]))
"<script></script>"
user=> (str (h/html [:p]))
"<p />"
```

And provides a CSS-like shortcut for denoting `id` and `class`
attributes:

```clojure
user=> (str (h/html [:div#foo.bar.baz "bang"]))
"<div id=\"foo\" class=\"bar baz\">bang</div>"
```

If the body of the element is a seq, its contents will be expanded out
into the element body. This makes working with forms like `map` and
`for` more convenient:

```clojure
user=> (str (h/html [:ul
                     (for [x (range 1 4)]
                       [:li x])]))
"<ul><li>1</li><li>2</li><li>3</li></ul>"
```

Strings are automatically escaped:

```clojure
user=> (str (h/html [:p "Tags in HTML are written with <>"]))
"<p>Tags in HTML are written with &lt;&gt;</p>"
```

To bypass this, use the `hiccup2.core/raw` function:

```clojure
user=> (str (h/html [:p (h/raw "Hello <em>World</em>")]))
"<p>Hello <em>World</em></p>"
```

This can also be used for doctype declarations:

```clojure
user=> (str (h/html (h/raw "<!DOCTYPE html>") [:html {:lang "en"}]))
"<!DOCTYPE html><html lang=\"en\"></html>"
```

## Differences between Hiccup 1 and 2

In brief: Hiccup 1 doesn't escape strings by default, while Hiccup 2
does. They occupy different namespaces to ensure backward compatibility.

In Hiccup 1, you use the `h` function to escape a string - that is,
ensure that unsafe characters like `<`, `>` and `&` are converted into
their equivalent entity codes:

```clojure
(h1/html [:div "Username: " (h1/h username)])
```

In Hiccup 2 strings are escaped automatically, but the return value from
the `html` macro is a `RawString`, rather than a `String`. This ensures
that the `html` macro can still be nested.

```clojure
(str (h2/html [:div "Username: " username]))
```

It's recommended to use Hiccup 2 where possible, particularly if your
app handles user data. Use of the non-core namespaces, such as
`hiccup.page`, should be avoided with Hiccup 2.

## License

Copyright © 2023 James Reeves

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
