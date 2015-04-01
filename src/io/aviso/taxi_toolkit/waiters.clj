(ns io.aviso.taxi-toolkit.waiters
  "Assertion helpers for UI elements."
  (:require [clj-webdriver.taxi :refer :all]
            [clojure.string :as s]
            [clojure.test :refer [is]]
            [io.aviso.taxi-toolkit
             [ui :refer :all]
             [utils :refer :all]]))

(defn wait-for
  "Waits for element to appear in the DOM."
  [& el-spec]
  (wait-until #(not (nil? (apply $ el-spec))) webdriver-timeout))

(defn wait-for-visible
  "Waits for element to be visible."
  [& el-spec]
  (apply wait-for el-spec)
  (wait-until (fn [] (retry #(visible? (apply $ el-spec)))) webdriver-timeout))

(defn wait-for-text
  "Waits for element to contain given text. Typical use case is when
  element's text content is changed shortly before asserting, for instance
  when value in an element will be replaced after completing AJAX request,
  and we want to assert on a new value."
  [txt & el-spec]
  (apply wait-for el-spec)
  (wait-until #((str-eq txt) (text (apply $ el-spec))) webdriver-timeout))

(defn wait-for-enabled
  "Waits for element to be enabled."
  [& el-spec]
  (apply wait-for el-spec)
  (wait-until #(enabled? (apply $ el-spec)) webdriver-timeout))

(defn wait-for-url
  "Waits for the browser to load an URL which would match (partially)
  the given pattern."
  [re-url]
  (wait-until #(re-find re-url (current-url))))

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
