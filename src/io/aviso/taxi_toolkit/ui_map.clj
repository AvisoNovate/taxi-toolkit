(ns io.aviso.taxi-toolkit.ui-map
  "Generating UI maps for easy DOM access."
  (:require [clj-webdriver.taxi :refer [find-element find-elements]]
            [io.aviso.taxi-toolkit.utils :refer :all]))


(defn- resolve-parent-reference
  "If child element refers to the parent element using :_ keyword,
  it needs to be resolved so the resulting XPath expression is valid.

  For instance [:child [:_ \"/..\"]] with parent \"//div[@class='abc']\"
  would resolve to: [:child [\"//div[@class='abc']/..\"]]"
  [parent [child-name child-spec]]
  (let [child-spec-resolved (map #(if (= :_ %) parent %) child-spec)]
    [child-name (apply str child-spec-resolved)]))

(defn- gen-ui-element-spec
  "Accepts a vector where first item describes the parent element,
  and rest consist of tuples: label and spec of child elements.

  This function rewrites this into a map, and resolves all parent
  references ':_'.

  For instance:

      [\"//div\"
       :child1 [:_ \"/h1\"]
       :child2 [:_ \"/h2\"]]

  Would be transformed into:

      {:self \"//div\"
       :child1 \"//div/h1\"
       :child2 \"//div/h2\"}"
  [[el-itself & children]]
  (let [unresolved (apply hash-map children)
        resolved (map (partial resolve-parent-reference el-itself) unresolved)]
    (assoc (into {} resolved) :self el-itself)))

(defn- gen-ui-element
  "Transforms tuples from the UI map into the format used
  by functions such as (find-one) and (find-all)."
  [[label spec]]
  (if (vector? spec)
    [label (gen-ui-element-spec spec)]
    [label {:self spec}]))

(defn gen-ui-map
  "Builds UI map out of user supplied map."
  [m]
  (into {} (map gen-ui-element m)))

(defn- xpath-or-css
  "Given a query, returns it in a form accepted by
  (clj.webdriver.taxi/find-element) - as a map with
  key either :xpath (default) or :css. If a string
  is supplied, assumes it's a xpath selector."
  [query]
  (cond
    (:css query) query
    (:xpath query) query
    :else {:xpath query}))

(defn resolve-element-path
  "Finds element in a UI map."
  [m path-spec]
  (if-let [el (get-in m path-spec)]
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
  ([f query]
   (f (xpath-or-css query)))
  ([f ui & el-path]
   (if (and (= 1 (count el-path))
            (string? (first el-path)))
     (find-with-f f (first el-path))
     (f (xpath-or-css (resolve-element-path ui el-path))))))

(defn find-one
  "Find one element matching selector. See (find-with-f)."
  [& args]
  (apply (partial find-with-f find-element) args))

(defn find-all
  "Find all element matching selector. See (find-with-f)."
  [& args]
  (apply (partial find-with-f find-elements) args))

