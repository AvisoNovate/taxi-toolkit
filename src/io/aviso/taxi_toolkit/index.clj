(ns io.aviso.taxi-toolkit.index
  "So it's easier to require all this."
  (:require [potemkin :refer [import-vars]]
            [io.aviso.taxi-toolkit.assertions]
            [io.aviso.taxi-toolkit.composite-assertions]
            [io.aviso.taxi-toolkit.selectors.angular]
            [io.aviso.taxi-toolkit.selectors.general]
            [io.aviso.taxi-toolkit.selectors.complex]
            [io.aviso.taxi-toolkit.logs]
            [io.aviso.taxi-toolkit.ui]
            [io.aviso.taxi-toolkit.waiters]))

(defn ns-public-symbols
  "Returns a sequence of all public symbols from a namespace,
  along with the namespace symbol itself (as a first element)."
  [x]
  (into [x] (keys (ns-publics x))))

(defmacro import-ns-vars
  "Similar to (potemkin.namespaces/import-vars), but imports
  all symbols from the namespace."
  [x]
  (let [x (ns-public-symbols x)]
    `(import-vars ~x)))

(import-ns-vars io.aviso.taxi-toolkit.assertions)
(import-ns-vars io.aviso.taxi-toolkit.composite-assertions)
(import-ns-vars io.aviso.taxi-toolkit.ui)
(import-ns-vars io.aviso.taxi-toolkit.waiters)
(import-ns-vars io.aviso.taxi-toolkit.selectors.general)
(import-ns-vars io.aviso.taxi-toolkit.selectors.angular)
(import-ns-vars io.aviso.taxi-toolkit.selectors.complex)
(import-ns-vars io.aviso.taxi-toolkit.logs)
