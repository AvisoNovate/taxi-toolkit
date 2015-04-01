# taxi-toolkit

![taxi-toolkit@clojars](http://clojars.org/io.aviso/taxi-toolkit/latest-version.svg)

A Clojure library designed to help with writing integration tests using
[clj-webdriver](https://github.com/semperos/clj-webdriver).

## Usage

```clojure
(ns example.test
  (:require [clojure.test :refer :all]
            [clj-webdriver.taxi :refer :all]
            ;; Require all symbols from the index namespace. It contains
            ;; all symbols from the taxi-toolkit.
            [io.aviso.taxi-toolkit.index :refer :all]))

;; Declare all the UI elements on which you wish to assert later.
;; Functions (by-...) are so-called "selectors" - they return a map
;; in a form accepted by (taxi/find-element).
(def ui {:search-btn (by-ng-click "search()")
         :form [(by-class-name ".search-form")
                :name [:_ (by-ng-model "query.name")]
                :age  [:_ (by-ng-model "query.name")]]
         :results [(by-role "result-table")
                   :all-rows (by-class-name ".row")]})


(deftest user-interface
  ;; Register UI map
  (set-ui-spec! ui)

  (open-tested-page)

  ;; (assert-ui) accepts a map, where keys refer to the UI elements declared
  ;; in the UI map, and values are an assertions (or vector of assertions).
  (assert-ui {:filter-btn          [visible? (text= "Filter")]
              [:form :name]        visible?
              [:form :age]         visible?
              :results             hidden?})

  ;; (fill-form) is a helper to quickly input text into multiple UI elements.
  (fill-form {:name "Jorge Luis Borges"
              :age "115"})

  (a-click :filter-btn)
  (wait-for-visible :results)

  (assert-ui {:form                 hidden?
              :results              visible?
              ;; With the ^:all hint, assertions will be made on all
              ;; elements found with the given query, not only one.
              ;; (each) is a helper function to assert on all elements.
              ^:all [:results :all-rows]
                                    (each text= ["result1" "result2"
                                                 "result3"])})

   ;; If you need to use raw Taxi API to interact with UI elements, you can
   ;; use ($) and ($$) functions to get one or all matching elements
   ;; respectively, such as:
   (wait-until #(re-find #"cls1|cls2" (attribute ($ :search-btn) :class))))
```
