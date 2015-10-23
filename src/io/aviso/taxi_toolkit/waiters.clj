(ns io.aviso.taxi-toolkit.waiters
  "Assertion helpers for UI elements."
  (:require [clj-webdriver.taxi :refer :all]
            [clj-webdriver.core :refer [->actions move-to-element]]
            [clojure.string :as s]
            [clojure.test :refer [is]]
            [io.aviso.taxi-toolkit
             [ui :refer :all]
             [utils :refer :all]
             [assertions :as at]]))

(defn smart-wait [checker el-spec]
  "Waits for condition to be met for el-spec. If timeout is exceeded throws an exception with checker and el-spec information.
  Useful when tests use common functions in different scenarios, adhering to DRY principle."
  (try
    (wait-until #(retry (fn [] (checker (apply $ el-spec)))) webdriver-timeout)
    (catch org.openqa.selenium.TimeoutException e
      (throw (org.openqa.selenium.TimeoutException. (str "Timeout exceeded for: " checker " with el spec: " el-spec) e)))))

(defn wait-for
  "Waits for element to appear in the DOM."
  [& el-spec]
  (smart-wait (complement nil?) el-spec))

(defn wait-for-visible
  "Waits for element to be visible."
  [& el-spec]
  (apply wait-for el-spec)
  (smart-wait visible? el-spec))

(defn wait-for-hidden
  "Waits for element to be hidden."
  [& el-spec]
  (apply wait-for el-spec)
  (smart-wait (complement visible?) el-spec))

(defn wait-for-text
  "Waits for element to contain given text. Typical use case is when
  element's text content is changed shortly before asserting, for instance
  when value in an element will be replaced after completing AJAX request,
  and we want to assert on a new value."
  [txt & el-spec]
  (apply wait-for el-spec)
  (smart-wait (comp (str-eq txt) text) el-spec))

(defn wait-for-enabled
  "Waits for element to be enabled."
  [& el-spec]
  (apply wait-for el-spec)
  (try
    (wait-until #(enabled? (apply $ el-spec)) webdriver-timeout)
    (catch org.openqa.selenium.TimeoutException err
      (is false (str "Waited for element " el-spec " to be enabled.")))))

(defn wait-for-url
  "Waits for the browser to load an URL which would match (partially)
  the given pattern or string."
  [url]
  (try
    (wait-until #(re-find (re-pattern url) (current-url)))
    (catch org.openqa.selenium.TimeoutException err
      (is false (str "Expected URL to match: '" url "' but got: '" (current-url)"'")))))

(defn wait-for-present
  "Waits for an element to be considered present. Existing and visible."
  [& el-spec]
  (try
    (wait-until #(present? (apply $ el-spec)) webdriver-timeout)
    (catch org.openqa.selenium.TimeoutException err
      (is false (str "Waited for element " el-spec " to be present.")))))

(defn wait-for-ng-animations
  "Waits for Angular animations to complete."
  []
  ; It would be better to rely on more deterministic criteria here, i.e.
  ; whether there is an element in DOM with ng-animate CSS class applied.
  ; Unfortunately, for some reason, Angular would leave this class applied
  ; to some elements which are not being animated.
  (Thread/sleep 500))

(defn wait-and-click
  "Waits for an element to appear, and then clicks it."
  [& el-spec]
  (apply wait-for el-spec)
  (apply a-click el-spec))

(defn wait-for-removed
  "Waits for an element to be removed from the DOM"
  [& el-spec]
  (smart-wait nil? el-spec))

(defn wait-for-class
  "Waits for an element to have a certain class"
  [cls & el-spec]
  (try
    (wait-until #(some #{cls} (classes (apply $ el-spec))) webdriver-timeout)
    (catch org.openqa.selenium.TimeoutException err
      (is false (str "Waited for class '" cls "' to appear on " el-spec " but it never did.")))))

(defn wait-for-class-removed
  "Waits for an element to NOT have a certain class"
  [cls & el-spec]
  (try
    (wait-until #(nil? (some #{cls} (classes (apply $ el-spec)))) webdriver-timeout)
    (catch org.openqa.selenium.TimeoutException err
      (is false (str "Waited for element " el-spec " to NOT have class '" cls "', but that never happened.")))))

(defn wait-for-element-count
  "Waits for the number of elements to be found by a selector to match expected number."
  [no & el-spec]
  (try
    (wait-until #(= (count (apply $$ el-spec)) no) webdriver-timeout)
    (catch org.openqa.selenium.TimeoutException err
      (is false (str "Waited for exactly " no " elements to be found by selector " el-spec " but found " (count (apply $$ el-spec)))))))

(defn a-hover
  "Hover an item."
  [& el-spec]
  (->actions *driver*
             (move-to-element (apply $ el-spec))))

(defn a-ui
  "Applies a sequence of actions to a sequence of elements.
  Arg. 1 (action) is applied to arg. 2 (el-spec),
  arg. 3 (action) is applied to arg. 4 (el-spec) etc.
  Waits for element visible, then interacting with el."
  [& els-spec]
  (let [[action el-spec & remainder] els-spec
        el-spec-v (as-vector el-spec)]
    (apply wait-for-visible el-spec-v)
    (apply action el-spec-v)
    (if remainder
      (apply a-ui remainder))))
