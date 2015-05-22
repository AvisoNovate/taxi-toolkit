(ns io.aviso.taxi-toolkit.actions
  "Set of helper functions for composite interactions with the UI."
  (:require [clj-webdriver.taxi :refer :all]
  	        [clj-webdriver.core :refer [->actions move-to-element click-and-hold release]]
            [clojure.string :as s]
            [clojure.test :refer [is]]
            [io.aviso.taxi-toolkit
             [ui :refer :all]
             [utils :refer :all]
             [waiters :refer :all]]))

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
