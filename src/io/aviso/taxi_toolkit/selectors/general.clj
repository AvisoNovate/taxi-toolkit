(ns io.aviso.taxi-toolkit.selectors.general)


(defn by-xpath
  "Find element(s) by X-Path expression.
  Use either (by-xpath \"...\") to find one element,
  or (by-xpath :all \"...\") to find all."
  [& xs]
  (apply str xs))

(defn by-css
  "Find element by CSS selector"
  [selector]
  {:css selector})

(defn by-attribute
  "Find element by attribute value"
  [attr-name attr-val]
  (by-xpath (str "//*[@" attr-name "='" attr-val "']")))

(defn by-class-name
  "Find element by CSS class name"
  [class-name]
  (by-xpath "//*[contains(@class, '" class-name "')]"))

(defn by-role
  "Find element by role"
  [role]
  (by-attribute "role" role))

(defn by-exact-text
  "Finds element containing the exact text given."
  [txt]
  (by-xpath "//*[text()='" txt "']"))
