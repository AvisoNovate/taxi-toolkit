(ns io.aviso.taxi-toolkit.selectors.angular
  (:require [io.aviso.taxi-toolkit.selectors.general :refer :all]))


(defn by-ng-model
  "Find element by AngularJS ng-model attribute"
  [ng-model]
  (by-attribute "ng-model" ng-model))

(defn by-ng-bind
  "Find element by AngularJS ng-bind attribute"
  [ng-bind]
  (by-attribute "ng-bind" ng-bind))

(defn by-ng-click
  "Find element by AngularJS ng-click attribute"
  [ng-click]
  (by-attribute "ng-click" ng-click))

(defn by-ng-grid-header
  "Selects ng-grid table cell based on a header title. If no row number (indexed from 1) is given,
  selects cells in all rows (a column)."
  ([header-label]
   (by-ng-grid-header header-label nil))
  ([header-label row-index]
   (let [row-selector (if row-index (str "[position()=" row-index "]") "")]
     (by-xpath "//*[@ng-viewport]/div/div" row-selector                 ; position() selects desired row in a table.
               "/div[count(//*[@ng-header-row]/div[*//text()='"         ; Use count() as an index of a cell in a row. Count
               header-label                                             ; the header cells preceding the header cell with
               "']/preceding-sibling::div) + 1]"))))                    ; the given label.

(defn by-ng-show
  "Find element by AngularJS ng-show attribute."
  [ng-show]
  (by-attribute "ng-show" ng-show))

(defn by-ng-hide
  "Find element by AngularJS ng-hide attribute."
  [ng-hide]
  (by-attribute "ng-hide" ng-hide))