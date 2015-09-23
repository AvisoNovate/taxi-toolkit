(ns io.aviso.taxi-toolkit.composite-assertions
  "Complex assertion helpers for UI elements."
  (:require [clj-webdriver.taxi :as t]
            [clojure.string :as s]
            [clojure.test :refer [is]]
            [io.aviso.taxi-toolkit
             [ui :refer :all]
             [utils :refer :all]
             [waiters :refer :all]]))

(defn assert-ui
  "Accepts a map in a following form:
   {el-spec1 assertions1
    el-spec2 assertions2}
  Each el-spec is an element path from n UI map (or a vector,
  if element is nested in this map).
  Each assertion is either one function, or a sequence of thereof,
  which will be run over an element.
  Example:
    (def ui {:some-table {:some-header #(by-xpath \"...\")}
             :some-div #(...)})
    (def assert-ui (assert-ui-factory ui))

    (deftest example
      (assert-ui {[:some-table :some-header] [exists? (text= \"Header\")]
                   :some-div                 visible?}))

  If the assertion expects a collection of elements, use :all hint:
    (assert-ui {^:all [:some-element-collection] (count= 3)})"
  [m]
  (doseq [[el-spec asserts] m
            assert-fn (as-vector asserts)]
    (let [collection? (-> el-spec meta :all)
          find-fn (if collection? $$ $)
          el (apply find-fn (as-vector el-spec))]
      (assert-fn el))))

(defn assert-nav
  "Helper for asserting whether clicking element (or sequence of elements)
  causes browser window to navigate to a certain URL."

  [& args]
  (let [ui-actions (butlast args)
        url (last args)]
    (apply a-ui ui-actions)
    (wait-for-url url)))
