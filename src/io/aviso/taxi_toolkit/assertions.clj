(ns io.aviso.taxi-toolkit.assertions
  "Assertion helpers for UI elements."
  (:require [clj-webdriver.taxi :as t]
            [clojure.string :as s]
            [clojure.test :refer [is]]
            [io.aviso.taxi-toolkit
             [ui :refer :all]
             [utils :refer :all]]))

(defn has-text?
  "UI assertion for text content. Use either text or regular expression."
  [txt]
  (fn [el]
    (let [actual-txt (s/trim (a-text el))]
      (is ((str-eq txt) actual-txt) (format "Expected <<%s>> Actual <<%s>>." txt actual-txt)))))

(defn has-attr?
  "UI assertion for an arbitrary attribute value."
  [attr-name attr-value]
  (fn [el]
    (let [actual-value (t/attribute el attr-name)]
      (is (= actual-value attr-value) (format "Expected attribute <<%s> Actual <<%s>>, on <<%s>>."
                                              attr-value actual-value el)))))

(def has-attribute? has-attr?)

(defn has-value?
  "UI assertion for an element's value to be as expected."
  [expected-value]
  (fn [el]
    (is (= (t/value el) expected-value) (str "Expected element's value to be '" expected-value "', but was '" (t/value el) "'."))))

(defn is-focused?
  "Asserts that an element is in focus."
  [el]
  (let [focused-el (t/execute-script "return document.activeElement;")
        topic-el   (:webelement el)]
    (is (= topic-el focused-el) (str "Element appears not to be in focus."))))

(defn is-not-focused?
  "Asserts that an element is NOT in focus."
  [el]
  (let [focused-el (t/execute-script "return document.activeElement;")
        topic-el   (:webelement el)]
    (is (not (= topic-el focused-el)) (str "Element appears to be in focus, but it wasn't expected to be."))))

(defn is-present?
  "UI assertion for an element to exist."
  [el]
  (is (t/present? el) (str "Expected element to be present (existing and visible).")))

(defn is-existing?
  "UI assertion for an element to be in the DOM."
  [el]
  (is (t/exists?) (str "Expected element to exist in the DOM, but it didn't.")))

(defn is-missing?
  "UI assertion for an element which should not be in a DOM."
  [el]
  (is (nil? el) (str "Expected element to be missing (nil), but it exists (wasn't nil).")))

(defn has-class?
  "UI assertion for a CSS class to exist on a certain element."
  [css-class]
  (fn [el]
    (is (not (nil? (some #{css-class} (classes el)))) (str "Expected element to have the class '" css-class "' but it didn't.  It had: " (classes el) "."))))

(defn has-no-class?
  "UI assertion for a CSS class to NOT exist on a certain element."
  [css-class]
  (fn [el]
    (is (nil? (some #{css-class} (classes el))) (str "Expected element to NOT have the class '" css-class "' but it did. It had: " (classes el) "."))))

(defn is-selected?
  "UI assertion for an <option> element to be selected."
  [el]
  (is (t/selected? el) (str "Expected element (option) to be selected, but it wasn't.")))

(defn is-not-selected?
  "UI assertion for an <option> element to NOT be selected."
  [el]
  (is (not (t/selected? el)) (str "Expected element (option) to NOT be selected, but it was.")))

(def is-deselected? is-not-selected?)

(defn is-visible?
  "UI assertion for an element to be visible."
  [el]
  (is (t/visible? el) (str "Expected element to be visible/displayed, but it wasn't.")))

(def is-displayed? is-visible?)

(defn is-hidden?
  "UI assertion for an element to be considered hidden (not visible)."
  [el]
  (is (not (t/visible? el)) (str "Expected element to be hidden, but it was visible.")))

(defn is-enabled?
  "UI assertion for an element to be enabled."
  [el]
  (is (t/enabled? el) (str "Expected element to be enabled, but it was not.")))

(defn is-disabled?
  "UI assertion for an element to be disabled (not enabled)."
  [el]
  (is (not (t/enabled? el)) (str "Expected element to be disabled, but it was enabled.")))

(defn allows-multiple?
  "UI assertion for a <select> to allow for multiple selections."
  [el]
  (is (t/multiple? el) (str "Expected select element to allow multiple selections, but it didn't.")))

(defn is-not-multiple?
  "UI assertion for a <select> to NOT allow for multiple selections."
  [el]
  (is (not (t/multiple? el)) (str "Expected select element to NOT allow multiple selections, but it did.")))

; This assertion is not made upon an element (ie. it will not run with `assert-ui`)
(defn has-page-title?
  "Assert for the page title to be as expected."
  [page-title]
  (is (= (t/title) page-title) (str "Expected page title to be '" page-title "', but it was '" (t/title)"'.")))

; Faster (express) version of `is-missing?`
; Will not run with `assert-ui`
(defn x-is-missing?
  "Faster assertion for missing element."
  [el-spec]
  (let [selector (query-with-params {} el-spec)
        js (if (:css selector)
             (str "return document.querySelectorAll(\"" (:css selector) "\").length;")
             (str "return document.evaluate(\"count(" (or (:xpath selector) selector) ")\", document, null, XPathResult.NUMBER_TYPE, null).numberValue;"))
        cnt (t/execute-script js)]
    (is (= 0 cnt) (str "Element " el-spec " is not missing - found " cnt " of those."))))

(defn is-count?
  "UI assertion for number of elements."
  [n]
  (fn [els]
    (is (= n (count els)) (str "Expected to find " n " elements, but found " (count els) "."))))

(def found-exact-nr? is-count?)
(def is-exactly-nr? is-count?)

(defn each
  "Run given assertion for every item in the collection."
  ([f]
    (fn [els]
      (let [match-result (doall (map (fn [element]
                                       (f element)) els))]
        (every? true? match-result))))
  ([f expected]
    (fn [els]
      (let [match-result (doall (map (fn [[element expectation]]
                                       ((f expectation) element)) (zipmap els expected)))]
        (every? true? match-result)))))
