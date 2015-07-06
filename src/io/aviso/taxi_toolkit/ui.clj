(ns io.aviso.taxi-toolkit.ui
  "Set of helper functions for interacting with DOM elements."
  (:require [clj-webdriver.taxi :refer :all]
            [clj-webdriver.core :refer [->actions move-to-element click-and-hold release]]
            [clojure.string :as s]
            [clojure.test :refer [is]]
            [io.aviso.taxi-toolkit
             [utils :refer :all]
             [ui-map :refer :all]]))

(def webdriver-timeout (* 15 1000))
(def ^:private ui-maps (atom {}))


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
  (let [el-spec (resolve-element-path @ui-maps el-path)]
    (replace-all el-spec params)))

(defn click-non-clickable
  "Similar to (taxi/click), but works with non-clickable elements such as <div>
   or <li>."
  [el]
  (->actions *driver*
           (move-to-element el)
           (click-and-hold el)
           (release el)))

(defn a-click
  "Element-agnostic. Runs either (taxi/click) or (click-anything)."
  [& el-spec]
  (let [el (apply $ el-spec)]
    (case (.getTagName (:webelement el))
      ("a" "button") (retry #(click el))
      (retry #(click-non-clickable el)))))

(defn a-text
  "For non-form elements such as <div> works like (taxi/text).
  For <input> works like (taxi/value)."
  [el]
  (case (.getTagName (:webelement el))
    ("input") (retry #(value el))
    (retry #(text el))))

(defn classes
  "Return list of CSS classes element has applied directly (via attribute)."
  [el]
  (s/split (attribute el :class) #"\s+"))

(defn fill-form
  "Fill a form. Accepts a map of 'element - text' pairs."
  [el-val]
  (mapv (fn [[el txt]] (input-text (apply $ (as-vector el)) txt)) el-val))
