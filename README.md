# taxi-toolkit

![taxi-toolkit@clojars](http://clojars.org/io.aviso/taxi-toolkit/latest-version.svg)

A Clojure library designed to help with writing integration tests using
[clj-webdriver](https://github.com/semperos/clj-webdriver).

## Demo

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
         :form       {:self (by-class-name ".search-form")
                      :name (by-ng-model "query.name")
                      :age  (by-ng-model "query.name")}
         :results    {:self (by-role "result-table")
                      :all-rows (by-class-name ".row")}})

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

## Usage

taxi-toolkit consists of a set of helpers designed to reduce boilerplate code
and improve readability of test suites written using clj-webdriver.

To use it, simply require an `index` namespace:

```clojure
(require [io.aviso.taxi-toolkit.index :refer :all])
```

### UI maps

Obtaining reference to an element can be pretty verbose. taxi-toolkit
provides set of helper functions useful in common scenarios.

In each test suite you first need to declare a UI map (or maps). UI map tells
taxi-toolkit how to find each element(s) you need later in your test cases.
Each UI map needs to be registered with tookit using `set-ui-spec!` function.

For example:

```clojure
(def ui {:submit-btn (by-exact-text "Submit")
         :cancel-btn (by-exact-text "Cancel")
         :even-rows  (by-xpath "//tr[position() mod 2 = 0]")
         :menu       {:self            (by-role "user-menu")
                      :change-password (by-ng-click "changePassword()")
                      :log-out         (by-ng-click "logOut()")}})

(set-ui-spec! ui)
```

You can target decendants of top level UI elements (one level supported).

```
(def ui {:parent {:self      (...)
                  :decendant (..)}})
```
- see the `:menu` element in an example above.

#### set-ui-spec!

`(set-ui-spec! ui-map1 ...)`

Sets UI map (or maps) for use withing taxi-toolkit. This means that all toolkit
functions such as `$`, `$$` or `assert-ui` will br able to find elements using
their label (which is a key in a map).

#### $

```clojure
;; Find element
`($ :menu)`

;; Same as above (find element)
`($ :menu :self)`

;; Find decendant to :menu
`($ :menu :log-out)`

;; Find by x-path
`($ (by-xpath "//*[contains(@data-x, 'something')]"))`
```

Used to find one element matching the query. The preferred way of using this method
is to pass the key(s) specifying element in the UI map.

Alternatively, you can use a selector directly.

#### $$

`($$ :even-rows)`

`($$ (by-xpath "//*[contains(@data-x, 'something')]"))`

Same as `$`, but returns not one, but all matching elements.

### Simple selectors

Selectors are simple functions that return the same map as
accepted by `taxi/find-element`.

taxi-toolkit comes with a basic set of selectors, as well as set of selectors
useful in Angular application testing. You can easily write your own. Don't
forget to create a pull request then!

#### by-attribute

`(by-attribute "data-src" "abc")`

Finds element by any attributes value.

#### by-class-name

`(by-class-name "class1")`

Finds element by CSS class name.

#### by-css

`(by-css ".class1.class2")`

Finds element by CSS selector.

#### by-id

`(by-id "some-element")`

Finds an element by DOM ID.

#### by-exact-text

`(by-exact-text "Some label")`

Finds an element which contents match exact text given.

#### by-name

`(by-name "some-name")`

Finds an element by name attribute.

#### by-partial-text

`(by-partial-text "lab")`

Finds an element which contents contain given text.

#### by-role

`(by-role "some-menu")`

Finds element by its role attribute.

#### by-xpath

`(by-xpath "//*[position=" 1 "]")`

Uses all passed parameters (concatenated) to form an XPath expression.

### Angular selectors

Set of selectors useful when testing Angular applications.

#### by-ng-bind

`(by-ng-bind "some.thing")`

Finds element by the value of `ng-bind` attribute.

#### by-ng-click

`(by-ng-click "someFn()")`

Finds element by the value of `ng-click` attribute.

#### by-ng-model

`(by-ng-model "people")`

Finds element by the value of `ng-model` attribute.

#### by-ng-grid-header

`(by-ng-grid-header "Column 1")`

`(by-ng-grid-header "Column 2" 3)`

If two parameters are given, finds a cell in a column with the header passed as
a first parameter, and row number passed as a second parameter.

If no row number is passed, selects each cell in a column.

### Complex UI selectors

Advanced selectors.

#### by-label

`(by-label "abc")`

Finds a `<div>` in a Bootstrap panel next to an element with the given label.
I.e.:

```html
   <div class="panel">
     <div class="row">
        <div class="col-md-2">abc</div>
        <div class="col-md-10">This will be found</div>
     </div>
   </div>
```

### Assertions

taxi-toolkit contains a set of functions helpful in writing readable test cases.

#### asseert-ui

`(assert-ui m)`

Accepts a map of element - assertion pairs. Each assertion will be run over
given element. If any of the assertions fails, `assert-ui` fails.

By default, for each element key, one element is found. If you wish to assert
on all matching elements, you need to use `^:all` hint. See (`each`).

If, in the UI map, the element you want to assert on is a nested element,
refer to it in a vector.

This function required UI map to be set.

Example:

```clojure
(assert-ui {:submit-btn      hidden?
            :cancel-btn      [visible? (text= "Cancel")]
            [:menu :log-out] [visible? (text= "Log out")]})
```

#### assert-nav

`(assert-nav el-vec url)`

Clicks each element in a vector and then waits until current URL matches the
given pattern. Assertion fails when webdriver times out.

Example:

```clojure
(assert-nav [:cancel-btn] "/some-page-url")
```

#### attr=

Asserts that element has an attribute with the given value.

```clojure
(assert-ui {:submit-btn (attr= "role" "some-role")})
```

#### count=

Asserts that selector matches the given number of elements.

```clojure
(assert-ui {^:all [:even-rows] (count= 3)})
```

#### disabled?

Complements `taxi/enabled?`.

#### each

`(each assert-fn expected-values-vector)

Helps with asserting when selector matches multiple elements. Runs a given
assertion on each matching element, expecting the corresponding value from the
vector given.

```clojure
(assert-ui {^:all [:even-rows] (each text= ["Row2" "Row4" "Row6"])})
```

#### focused?

Asserts that element is in focus.

```clojure
(assert-ui {:submit-btn focused?})
```

#### has-class?

Asserts that element has CSS class applied. Only classes applied using a HTML
`class` attribute are taken into an account:

```clojure
(assert-ui {:submit-btn (has-class? "btn-danger")})
```

#### has-no-class?

Complements `has-class?`.

#### hidden?

Complements `taxi/visible?`.

#### missing?

Asserts that element does not exist in the DOM.

```clojure
(assert-ui {:submit-btn missing?})
```

#### selected?

Asserts that a checkbox is selected.

```clojure
(assert-ui {:agree-checkbox selected?})
```

#### deselected?

Asserts that a checkbox in not selected.

```clojure
(assert-ui {:agree-checkbox deselected?})
```

#### text=

Asserts that element contains the given text.

```clojure
(assert-ui {:submit-btn (text= "Submit")})
```

### Waiters

When an action is performed, you often want to assert on the UI after it becomes
stable, i.e. no elements are added/removed without the next interaction.
This set of helpers will let you wait for various criteria to be met, and perform
assertions or other actions only afterwards.

#### wait-and-click

`(wait-and-click :menu :log-out)`

Waits for an element to appear in the DOM, then clicks it.

Same as:

```clojure
(wait-for :menu :log-out)
(a-click :menu :log-out)
```

#### wait-for

`(wait-for :menu :log-out)`

Waits for an element to appear in the DOM.

#### wait-for-enabled

`(wait-for-enabled :menu :log-out)`

Waits for an element to become enabled.

#### wait-for-ng-animations

`(wait-for-ng-animations)`

Waits for all Angular animations to complete.

**WARNING:** Due to the bug with angular-animate, this function currently
simply waits for 500 ms before proceeding.

#### wait-for-text

`(wait-for-text "Sign out" :menu :log-out)`

`(wait-for-text #"(?i)sign out" :menu :log-out)`

Waits for an element to contain the given text. Accepts either a string or a
regular expression pattern.

#### wait-for-present

`(wait-for-present :log-out)`

Waits for an element to exist in the DOM, and for it to become visible (`present?`). Convenient especially when asserting on an interactive element that was recently introduced in the DOM.

#### wait-for-url

`(wait-for-url #"/some-url")`

Waits for the browser to navigate to the given URL. Accepts a regular expression
pattern.

#### wait-for-visible

`(wait-for-visible :menu :log-out)`

Waits for an element to become visible.

#### wait-for-removed

`(wait-for-removed :menu :log-in)`

Waits for an element to be removed from the DOM.

### Actions

#### a-click

`(a-click :menu :log-out)`

Works like `taxi/click` for elements such as `<a>` or `<button>`, and like
`click-not-clickable` for other elements.

#### a-text

`(a-text ($ :menu :log-out))`

Works like `taxi/text` for elements such as `<div>` or `<p>`, and like
`taxi/value` for form elements like `<input>`.

#### classes

`(classes ($ :menu :log-out))`

#### click-not-clickable

`(click-non-clickable ($ :menu :log-out))`

Clicks elements that are not anchors or buttons. Works by moving the cursor
on top of that element, then pressing and releasing a mouse button.

Returns list of CSS classes given element has applied directly (via an attribute).

#### fill-form

Similar to `taxi/quick-fill`, but elements are referenced using label from UI map.

Example:

```clojure
(set-ui-map! {:username (by-ng-model "user.username")
              :password (by-ng-model "user.password")})

(fill-form {:username "jorge.luis.borges"
            :password "tiger"})
```

#### clear-with-backspace

Given an input element, presses backspace key as many times as it takes to clear it.

```clojure
(clear-with-backspace :some-el)
```
