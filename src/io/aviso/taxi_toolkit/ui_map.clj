(ns io.aviso.taxi-toolkit.ui-map
  "Generating UI maps for easy DOM access."
  (:require [clj-webdriver.taxi :refer [find-element find-elements find-element-under find-elements-under]]
            [io.aviso.taxi-toolkit.utils :refer :all]))


(defn resolve-element-path
  "Finds element in a UI map."
  [m path-spec]
  (if-let [el (get-in m (if (sequential? path-spec) path-spec (list path-spec)))]
    (if (:self el)
      (:self el)
      el)
    (throw (Exception. (str "Element " path-spec " not found in UI map")))))

(defn- find-with-f
  "Calls function f (should be either taxi/find-element or taxi/find-elements)
  with a correct parameters.

  Usage:
  - (find-with-f \"//div\") - maps to (taxi/find-element(s) {:xpath \"//div\"})
  - (find-with-f ui-map \"//div\") - same as above
  - (find-with-f ui-map :el1 :el2) - will look up selector for [:el1 :el2] in UI map
  and use it as a parameter for taxi/find-element(s)."
  [f-one f-nested ui & el-spec]
  (if (map? (first el-spec))
    ;; Allows user to call function without looking the element
    ;; reference up in the UI map:
    ;;   (find-with-f ... ... ... {:xpath "..."})
    ;;   (find-widt-f ... ... ... {:css "..."})
    (if (= 1 (count el-spec))
      (f-one (first el-spec))
      (f-nested (find-element (first el-spec))
                (second el-spec)))

    ;; Allows user to call function by looking the element
    ;; reference up in the UI map:
    ;;   (find-with-f ... ... ui-map :el-name)
    ;;   (find-with-f ... ... ui-map :el-name :child)
    (let [parent-spec (first el-spec)
          child-spec (second el-spec)]
      (if (or (= nil child-spec)
              (= :self child-spec))
        (f-one (resolve-element-path ui el-spec))
        (f-nested (find-element (resolve-element-path ui parent-spec))
                  (resolve-element-path ui el-spec))))))

(defn find-one
  "Find one element matching selector. See (find-with-f)."
  [& args]
  (apply (partial find-with-f find-element find-element-under) args))

(defn find-all
  "Find all element matching selector. See (find-with-f)."
  [& args]
  (apply (partial find-with-f find-elements find-elements-under) args))

