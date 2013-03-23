Hiccup
======

[![Build Status](https://secure.travis-ci.org/weavejester/hiccup.png)](http://travis-ci.org/weavejester/hiccup)

Hiccup is a library for representing HTML in Clojure. It uses vectors
to represent elements, and maps to represent an element's attributes.

Install
-------

Add the following dependency to your `project.clj` file:

    [hiccup "1.0.3"]

Documentation
-------------

* [Wiki](https://github.com/weavejester/hiccup/wiki)
* [API Docs](http://weavejester.github.com/hiccup)
    
Syntax
------

Here is a basic example of Hiccup syntax:

```clojure
user=> (use 'hiccup.core)
nil
user=> (html [:span {:class "foo"} "bar"])
"<span class=\"foo\">bar</span>"
```

The first element of the vector is used as the element name. The second
attribute can optionally be a map, in which case it is used to supply
the element's attributes. Every other element is considered part of the
tag's body.

Hiccup is intelligent enough to render different HTML elements in
different ways, in order to accommodate browser quirks:

```clojure
user=> (html [:script])
"<script></script>"
user=> (html [:p])
"<p />"
```

And provides a CSS-like shortcut for denoting `id` and `class`
attributes:

```clojure
user=> (html [:div#foo.bar.baz "bang"])
"<div id=\"foo\" class=\"bar baz\">bang</div>"
```

If the body of the element is a seq, its contents will be expanded out
into the element body. This makes working with forms like `map` and
`for` more convenient:

```clojure
user=> (html [:ul
               (for [x (range 1 4)]
                 [:li x])])
"<ul><li>1</li><li>2</li><li>3</li></ul>"
```
