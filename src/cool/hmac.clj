(ns cool.hmac)

;by Craig Andera https://clojurians.slack.com/team/candera
(defmacro formulet
  "Emits a form that will produce a cell using the formula over the
  specified input cells. Avoids some of the code-walking problems of
  the Hoplon macros. Cells can be either a vector, in which case the
  cells will be re-bound to their values under the same names within
  `body`, or a map whose keys are binding forms and whose values are
  the cells to bind.

  E.g.
  (formulet [x y z] (+ x y z))

  (formulet
    {x-val x-cell
     {:keys [a b]} y-cell}
    (+ x-val a b))"
  [cells & body]
  (if (map? cells)
    `((javelin.core/formula
        (fn ~(-> cells keys vec)
          ~@body))
       ~@(vals cells))
    `((javelin.core/formula
        (fn ~cells
          ~@body))
       ~@cells)))
