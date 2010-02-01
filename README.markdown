Hiccup
======

Hiccup is a library for representing HTML in Clojure. It uses vectors
to represent tags, and maps to represent a tag's attributes.

Syntax
------

Here is a basic example of Hiccup syntax:

    user=> (use 'hiccup.core)
    nil
    user=> (html [:span {:class "foo"} "bar"])
    "<span class=\"foo\">bar</span>"
    
The first element of the vector is used as the tag name. The second
attribute can optionally be a map, in which case it is used to supply
the tag's attributes. Every other element is considered part of the
tag's body.

Hiccup is intelligent enough to render different HTML tags in different
ways:

    user=> (html [:script])
    "<script></script>"
    user=> (html [:p])
    "<p />"

And provides a CSS-like shortcut for denoting `id` and `class`
attributes:

    user=> (html [:div#foo.bar.baz "bang"])
    "<div id=\"foo\" class=\"bar baz\">bang</div>"

If the body of the tag is a seq, its contents will be expanded out into
the tag body. This makes working with forms like `map` and `for` more
convenient:

    user=> (html [:ul
                   (for [x (range 1 4)]
                     [:li x])])
    "<ul><li>1</li><li>2</li><li>3</li></ul>"
