# CHANGELOG

## 0.1.7
- New waiter function `wait-for-element-count`.

## 0.1.6

- Added: clear-with-backspace.
- Added: selected? and deselected? assertions for checkboxes.
- Fixed a bug in each= so it respects the assert function passed as an argument (previously it was ignored and text= was always used).

## 0.1.5

- New waiter function `wait-for-present`.

## 0.1.4

- New selectors: `by-id` and `by-name`.
- `$` and `$$` now support nested elements with no UI map lookup, i.e.:

```clojure
($ {:xpath "//div"} {:xpath "p"})
```

- Fixed `is-missing?` so it handles XPath selectors properly.
- Fixed `query-with-params` so it handles nested elements properly. Note that parameters will be replaced only in the
child selector, not in a parent selector.

## 0.1.3

- Syntax has changed for nesting elements in the UI map. The new syntax:
```clojure
{:some-el {:self ...
           :child1 ...
           :child2 ...}}
```

replaces the old one:

```clojure
{:some-el [...
           :child1 ...
           :child2 ...]}

```

- When nesting elements, both CSS and XPath selectors are now supported. It is even legal now to use XPath selector for
a parent element, and CSS one for the child (or vice versa).
- The new `is-missing` can now be used (along the old `missing?`) - it is faster, but cannot be currently used as an
assertion in `assert-ui`.
- `(by-xpath)` no longer responds with a string. It returns a proper `{:xpath ...}` map instead.

## <= 0.1.2

Initial releases.
