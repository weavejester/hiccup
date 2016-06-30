(ns hiccup.element
  "Functions for creating HTML elements."
  (:require [hiccup.def :refer [defelem]]
            [hiccup.util :as util]))

(defn javascript-tag
  "Wrap the supplied javascript up in script tags and a CDATA section."
  [script]
  [:script {:type "text/javascript"}
    (str "//<![CDATA[\n" script "\n//]]>")])

(defelem link-to
  "Wraps some content in a HTML hyperlink with the supplied URL."
  [url & content]
  [:a {:href (util/to-uri url)} content])

(defelem mail-to
  "Wraps some content in a HTML hyperlink with the supplied e-mail
  address. If no content provided use the e-mail address as content."
  [e-mail & [content]]
  [:a {:href (str "mailto:" e-mail)}
   (or content e-mail)])

(defelem unordered-list
  "Wrap a collection in an unordered list."
  [coll]
  [:ul (for [x coll] [:li x])])

(defelem ordered-list
  "Wrap a collection in an ordered list."
  [coll]
  [:ol (for [x coll] [:li x])])

(defelem image
  "Create an image element."
  ([src]     [:img {:src (util/to-uri src)}])
  ([src alt] [:img {:src (util/to-uri src), :alt alt}]))

(def js-lazy-load-self-replace
  "var xmlhttp:ID = new XMLHttpRequest();
   xmlhttp:ID.onreadystatechange = function() {
    if (xmlhttp:ID.readyState == XMLHttpRequest.DONE ) {
	var innerHTML = xmlhttp:ID.responseText;
        document.getElementById(':ID').innerHTML = innerHTML;
        //console.log('done with ajax call. response'+innerHTML);
    }
  };

  xmlhttp:ID.open('GET', ':URL', true);
  xmlhttp:ID.send();")

(defelem lazy-element
  "Return a 'lazy' element which makes an AJAX request to the
specified url, then overwrites itself with the reponse contents.
An optinal tmp-contents are shown while AJAX is executed"
  ([url] (lazy-element url nil))
  ([url tmp-contents]
   (let [id (str (rand-int Integer/MAX_VALUE))
         javascript (util/format-keywords
                     js-lazy-load-self-replace
                     {:ID id :URL url})]
     [:div {:id id}
      (javascript-tag javascript)
      tmp-contents])))
