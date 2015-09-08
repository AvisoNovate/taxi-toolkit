(ns io.aviso.taxi-toolkit.assertions
  "Assertion helpers for UI elements."
  (:require [clj-webdriver.taxi :as t]
            [clojure.string :as s]
            [clojure.test :refer [is]]
            [io.aviso.taxi-toolkit
             [ui :refer :all]
             [utils :refer :all]]))

(defn text=
  "UI assertion for text content. Use either text or regular expression."
  [txt]
  (fn [el]
    (let [actual-txt (s/trim (a-text el))]
      (is ((str-eq txt) actual-txt) (format "Expected <<%s>> Actual <<%s>>" txt actual-txt)))))

(defn attr=
  "UI assertion for an arbitrary attribute value."
  [attr-name attr-value]
  (fn [el]
    (let [actual-value (t/attribute el attr-name)]
      (is (= actual-value attr-value) (format "Expected attribute <<%s> Actual <<%s>>"
                                              attr-value actual-value)))))

(defn focused?
  "Asserts whether given element is in focus."
  [el]
  (let [focused-el (t/execute-script "return document.activeElement;")]
    (is (= (:webelement el) focused-el) "Element appears not to be in focus")))

(defn is-missing?
  "Faster assertion for missing element."
  [el-id]
  (let [selector (query-with-params {} el-id)
        js (if (:css selector)
             (str "return document.querySelectorAll(\"" (:css selector) "\").length;")
             (str "return document.evaluate(\"count(" (or (:xpath selector) selector) ")\", document, null, XPathResult.NUMBER_TYPE, null).numberValue;"))
        cnt (t/execute-script js)]
    (is (= 0 cnt) (str "Element " el-id " is not missing - found " cnt " of those."))))

(defn missing?
  "UI assertion for a element which should not be in a DOM"
  [el]
  (is (nil? el)))

(defn has-class?
  "Indicates whether element has a given class applied"
  [css-class]
  (fn [el]
    (not (nil? (some #{css-class} (classes el))))))

(defn selected?
  [expect-selected?]
  (fn [el]
    (let [actual (t/selected? el)]
      (is (= actual expect-selected?) (format "Expected to be <<%s>> but is not"
                                              (if expect-selected? "selected" "unselected"))))))

(def hidden? (complement t/visible?))
(def disabled? (complement t/enabled?))
(def has-no-class? #(complement (has-class? %)))
(def deselected? #(complement (selected? %)))

(defn count= [n]
  "UI assertion for number of elements"
  (fn [els]
    (is (= n (count els)))))

(defn each
  "Run given assertion for every item in the collection."
  [f expected]
  (fn [els]
    (let [match-result (doall (map (fn [[element expectation]]
                                     ((f expectation) element)) (zipmap els expected)))]
      (every? true? match-result))))
