(ns io.aviso.taxi-toolkit.utils
  (:require [clojure.string :as s]))

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
        (if (> (System/currentTimeMillis) (+ (* 1000 1000) timestamp))
          (throw e)
          (do
            (Thread/sleep 100)
            (retry f timestamp)))))))
