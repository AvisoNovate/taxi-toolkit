(ns io.aviso.taxi-toolkit.composite-assertions
  "Complex assertion helpers for UI elements."
  (:require [clj-webdriver.taxi :as t]
            [clojure.string :as s]
            [clojure.test :refer [is] :as test]
            [io.aviso.taxi-toolkit
             [ui :refer :all]
             [utils :refer :all]
             [waiters :refer :all]])
  (:import (clojure.lang ExceptionInfo)))

(defn- retry-find+assert
  "Finding elements in the DOM and running assertions on them sometimes throws StaleElementExceptions
  due to rerendering in-between the two steps. Retrying the two steps a couple of times is a
  reasonable workaround."
  {:test (fn [] (let [find-fn (constantly nil)
                      i (atom 3)
                      assert-fn (fn [_] (if (= 0 @i)
                                          (is true)
                                          (do (swap! i dec)
                                              (is false))))
                      j (atom 3)
                      throw-fn (fn [_] (if (= 0 @j)
                                         (is true)
                                         (do (swap! j dec)
                                             (throw (ex-info "assertion has thrown an exception" {})))))]
                  (is (retry-find+assert find-fn assert-fn))
                  (is (= 0 @i))
                  (is (retry-find+assert find-fn throw-fn))
                  (is (= 0 @j))))}
  [find-fn assert-fn]
  (let [result (atom false)
        test-report test/report
        spy-report (fn [data]
                     (case (:type data)
                       :fail nil
                       :error nil
                       :pass (do (reset! result true)
                                 (test-report data))
                       (test-report data)))
        retriable #(let [return (assert-fn (find-fn))]
                    (when-not @result (throw (ex-info "forcing a retry" {})))
                    return)
        return (with-redefs [test/report spy-report]
                 (try (retry-times retriable)
                      (catch ExceptionInfo e nil)))]
    (if @result
      return
      (assert-fn (find-fn)))))

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
          finder (if collection? $$ $)
          el-spec-vector (as-vector el-spec)
          find-fn #(apply finder el-spec-vector)]
      (retry-find+assert find-fn assert-fn))))

(defn assert-nav
  "Helper for asserting whether clicking element (or sequence of elements)
  causes browser window to navigate to a certain URL."

  [& args]
  (let [ui-actions (butlast args)
        url (last args)]
    (apply a-ui ui-actions)
    (wait-for-url url)))
