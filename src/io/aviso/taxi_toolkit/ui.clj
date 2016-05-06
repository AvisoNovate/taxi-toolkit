(ns io.aviso.taxi-toolkit.ui
  "Set of helper functions for interacting with DOM elements."
  (:require [clj-webdriver.taxi :refer :all :as taxi]
            [clojure.string :as s]
            [clojure.test :refer [is]]
            [io.aviso.taxi-toolkit
             [utils :refer :all]
             [ui-map :refer :all]])
  (:import org.openqa.selenium.Keys))

(def webdriver-timeout (* 15 1000))
(def ^:private ui-maps (atom {}))

(defmacro retrying
  "Just a convenience macro."
  [& body]
  `(retry-till-timeout webdriver-timeout (fn [] ~@body)))

(defn set-ui-spec!
  [& xs]
  (reset! ui-maps (apply merge xs)))

(defn $
  "Find one element"
  [& el-path]
  (apply find-one @ui-maps el-path))

(defn $$
  "Find all elements"
  [& el-path]
  (apply find-all @ui-maps el-path))

(defn query-with-params
  "Retrieves element spec and replaces all params needles
  with the proper values."
  [params & el-path]
  (let [nested? (not= 1 (count el-path))
        resolve-params (fn [id]
                         (let [spec (resolve-element-path @ui-maps id)]
                           (into {} (map (fn [[k v]] [k (replace-all v params)]) spec))))
        el-spec (resolve-params el-path)]
    (if nested?
      [(resolve-params (first el-path)) el-spec]
      el-spec)))

(defn click-non-clickable
  "Similar to (taxi/click), but works with non-clickable elements such as <div>
   or <li>."
  [& el-spec]
  (retrying
   (let [el (apply $ el-spec)]
     (el-click-non-clickable el))))

(defn a-click
  "Element-agnostic. Runs either (taxi/click) or (click-anything)."
  [& el-spec]
  (retrying
   (let [el (apply $ el-spec)]
     (case (.getTagName (:webelement el))
       ("a" "button") (click el)
       (el-click-non-clickable el)))))

(defn a-text
  "For non-form elements such as <div> works like (taxi/text).
  For <input> works like (taxi/value)."
  [& el-spec]
  (retrying
   (let [el (apply $ el-spec)]
     (el-text el))))

(defn js-click
  "Invokes click event on an element using DOM API."
  [& el-spec]
  (retrying
   (let [el (apply $ el-spec)]
     (taxi/execute-script *driver* "arguments[0].click();" (:webelement el)))))

(defn classes
  "Return list of CSS classes element has applied directly (via attribute)."
  [& el-spec]
  (retrying
   (let [el (apply $ el-spec)]
     (el-classes el))))

(defn fill-form
  "Fill a form. Accepts a map of 'element - value' pairs.

  Also, waits until the element is enabled using clj-webdriver.taxi/wait-until.

  Will invoke an appropriate function depending on the element charactericts:
   - select - select-option,
   - input[type=checkbox] or input[type=radio]- select/deselect, otherwise
   - input/textarea - clear and input-text

  Takes either a vector [el value] pairs (or a map which would behave as such collection when applied to doseq)
  or an even number of key-value pairs if we want to preserve the order."

  [& el-val-or-entries]
  (let [el-val (if (= (count el-val-or-entries) 1)
                 (first el-val-or-entries)
                 (partition 2 el-val-or-entries))
        start (System/currentTimeMillis)]
    (doseq [[el-spec value] el-val]
      (retry-till-timeout webdriver-timeout
       (fn []
         (let [q-getter #(apply $ (as-vector el-spec))]
           (wait-until #(let [q (q-getter)]
                          (and q
                               (enabled? q)
                               (visible? q)))
                       webdriver-timeout)
           (let [q (q-getter)
                 tag-name (s/lower-case (tag q))
                 type-attr (s/lower-case (or (attribute q "type") ""))]
             (case tag-name
               "select" (select-option q value)
               ("textarea" "input") (case type-attr
                                      ("radio" "checkbox") (if value (select q) (deselect q))
                                      (do
                                        (clear q)
                                        (input-text q value)))))))
       :start start)))
  el-val-or-entries)

(defn clear-with-backspace
  "Clears the input by pressing the backspace key until it's empty."
  [& el-spec]
  (retrying
   (let [el (apply $ el-spec)
         n-of-strokes (count (el-text el))]
     (doall (repeatedly n-of-strokes #(send-keys el org.openqa.selenium.Keys/BACK_SPACE))))))
