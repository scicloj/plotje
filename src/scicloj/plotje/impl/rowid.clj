(ns scicloj.plotje.impl.rowid
  "Where a row sat in the dataset a pose was given.

   A facet splits a leaf's data with `tc/select-rows`, and the subset's
   rows are numbered from zero again -- so a row index taken after the
   split says which row of *that panel* it is, and two panels both have
   a row 0. Anything relating a drawn mark back to the data has to
   survive that, and the way to survive a selection is to be a column:
   `tc/select-rows` carries every column along without being told.

   So the identity is attached once, to the leaf's own data, before
   anything splits it. Every stage downstream inherits it for free."
  (:require [tablecloth.api :as tc]))

(def row-key
  "Column name for a row's place in the dataset it came from."
  :scicloj.plotje/row)

(defn tagged?
  "True of a dataset already carrying the identity."
  [ds]
  (boolean (and ds (contains? (set (tc/column-names ds)) row-key))))

(defn tag
  "Attach the row identity to `ds`, unless it is already there.

   Idempotent, because a dataset reaches this more than once: a pose
   may be drafted twice, and a layer may name the same dataset its leaf
   does."
  [ds]
  (if (or (nil? ds) (tagged? ds))
    ds
    (tc/add-column ds row-key (range (tc/row-count ds)))))

(defn untag
  "The dataset without the identity column, for anything that shows a
   user what columns they have."
  [ds]
  (tc/drop-columns ds (filterv (set (tc/column-names ds)) [row-key])))

(defn visible-names
  "The column names a user gave, without the identity Plotje attached.

   Everything that asks a dataset what columns it has -- to resolve a
   mapping, or to tell a reader what they could have written -- asks
   through here, so that the identity takes no part in either."
  [ds]
  (into #{} (remove #{row-key}) (tc/column-names ds)))

(defn rows
  "The source rows of `ds`, in its own order. Answers the row numbers
   themselves where the identity is absent, which is what an untagged
   dataset's own numbering means."
  [ds]
  (if (tagged? ds)
    (get ds row-key)
    (vec (range (tc/row-count ds)))))
