Hiccup
======

[![Build Status](https://travis-ci.org/weavejester/hiccup.svg?branch=master)](https://travis-ci.org/weavejester/hiccup)

Hiccup is a library for representing HTML in Clojure. It uses vectors
to represent elements, and maps to represent an element's attributes.

Install
-------

Add the following dependency to your `project.clj` file:

    [hiccup "1.0.5"]

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

These snippets use the `hiccup.core/html` macro, which converts
fragments to strings. To produce an entire HTML document, use the
macros from `hiccup.page`: `html5`, `xhtml`, and `html4`, as
appropriate. These will, for example, add the appropriate doctypes
and namespace declarations:

```clojure
;; hiccup.page/html5 renders a page:
(hiccup.page/html5 [:span "foo"])
=> "<!DOCTYPE html>\n<html><span>foo</span></html>"

;; hiccup.core/html renders a snippet:
(hiccup.core/html [:span "foo"])
=> "<span>foo</span>"
```
