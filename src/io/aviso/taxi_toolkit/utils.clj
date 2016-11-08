(ns io.aviso.taxi-toolkit.utils
  (:require [clojure.string :as s]
            [clj-webdriver.core :refer [->actions move-to-element click-and-hold release]]
            [clj-webdriver.taxi :as taxi])
  (:import [org.openqa.selenium TimeoutException]))

(defn str-eq
  "Returns an equality comparator for a string-ish needle,
  so the comparison can be done in a same way for both simple
  strings and for regular expressions.

  I.e.: ((str-eq #\"a.c\") \"abc\") => true
        ((str-eq \"a.c\")  \"abc\") => false"
  [needle]
  (if (instance? java.util.regex.Pattern needle)
                (partial re-matches needle)
                (partial = needle)))

(defn replace-all
  "Given a map {k1 v1 k2 v2 ... kn vn}, replaces all occurences of k1 with v1,
  k2 with v2 ... and kn with vn.
  For example:
    (replace-all \"Lorem ipsum\" {\"Lo\" \"AAA\" \"m\" \"BBB\"})
  returns:
    AAAreBBB ipsuBBB"
  [s m]
  (if (empty? m)
    s
    (let [[needle replacement] (first m)]
      (replace-all (s/replace s (name needle) (str replacement)) (dissoc m needle)))))

(defn as-vector [x]
  "Act like (identity) if x is already a sequence.
  Puts it into a vector otherwise."
  (if (sequential? x) x (vector x)))

(defn retry
  "Keeps retrying an action for one second before throwing an underlying
   exception. Useful i.e. when an element is not available for the moment
   (i.e. it's being animated)."
  ([f]
    (retry f (System/currentTimeMillis)))
  ([f timestamp]
    (try
      (f)
      (catch Exception e
        (if (> (System/currentTimeMillis) (+ 1000 timestamp))
          (throw e)
          (do
            (Thread/sleep 100)
            (retry f timestamp)))))))

(defn retry-till-timeout
  "Keeps retrying an action till the specified timeout described by value in milliseconds,
  with optionally overriden start time. Additionaly, a predicate can be passed which causes
  a retry if action is successful, but predicate is not."
  [timeout f & {:keys [pred start]
                :or {pred (constantly true)
                     start (System/currentTimeMillis)}}]
  (let [wrapped #(try [::success (f) (pred)] (catch Throwable e [::error e false]))]
    (loop [[tag ret check] (wrapped)]
      (cond
        (and (= tag ::success) check)
        ret
        (> (System/currentTimeMillis) (+ start timeout))
        (if (instance? Throwable ret)
          (throw ret)
          (throw (TimeoutException.)))
        :else (do
                (Thread/sleep 17)
                (recur (wrapped)))))))

(defn el-text
  "For non-form elements such as <div> works like (taxi/text).
  For <input> works like (taxi/value)."
  [el]
  (case (.getTagName (:webelement el))
    ("input") (taxi/value el)
    (taxi/text el)))

(defn el-classes
  "Splits the class attribute to obtain list of element classes."
  [el]
  (s/split (taxi/attribute el :class) #"\s+"))

(defn el-click-non-clickable
  "Similar to (taxi/click), but works with non-clickable elements such as <div>
   or <li>."
  [el]
  (->actions taxi/*driver*
             (move-to-element el)
             (click-and-hold el)
             (release el)))
