## 2.0.0-RC4 (2024-11-29)

* Fixed compiler bug that emitted unevaluated forms (#214)

## 2.0.0-RC3 (2024-02-11)

* Optimized amount of bytecode generated (#206)
* Fixed literal child elements from being escaped (#207)
* Fixed formatting of nil elements at runtime (#208)

## 2.0.0-RC2 (2023-10-05)

* Improved performance (#204)

## 2.0.0-RC1 (2023-06-21)

* Reverted behaviour of `hiccup.core/h` to 1.0 (#198)

## 2.0.0-alpha2 (2019-01-22)

* Fixed issue with dot-notation and non-literal classes (#151)

## 2.0.0-alpha1 (2017-01-15)

* Added `hiccup2.core` namespace that escapes strings automatically
* Added new syntax for class and style attributes
* Fixed issue with pre-compiled `html` macro accepting new mode bindings

## 1.0.5 (2014-01-25)

* Inverted container tag check to look for void tags instead
* Added apostrophes to list of characters to escape

## 1.0.4 (2013-07-21)

* Fixed merging of class and id attributes
* Fixed keyword rendering
* Added explicit ending tag for `<select>`

## 1.0.3 (2013-03-23)

* Allowed html5 root element to have arbitrary attributes
* Added support for `<optgroup>`
* Replaced render-html multimethod with protocol

## 1.0.2 (2012-09-15)

* Fixed bug with invalid URIs
* Fixed resolving of base-url

## 1.0.1 (2012-08-18)

* Added explit ending tags to `<section>`, `<aside>` and others
* Fixed to-uri to work with schemaless URLs

## 1.0.0 (2012-04-23)

* Initial release
