(ns io.aviso.taxi-toolkit.selectors.complex
  (:require [io.aviso.taxi-toolkit.selectors.general :refer :all]))


(defn by-label
  "Selects a div next to the div with text content specified.
  I.e. if there is one div with the label, and next one with value,
  selects value node based on the text of the label."
  [label]
  (by-xpath "//*[contains(@class, 'panel')]//*[text()[contains(., '" label "')]]/following-sibling::div"))
