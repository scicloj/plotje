(ns scicloj.plotje.impl.temporal
  "What a temporal axis reads, decided in one place.

   A date reaches Plotje as any of several `java.time` types, and four
   separate lists of them had grown -- the predicate, the conversion to
   epoch-milliseconds, the conversion to `LocalDateTime`, and the pose
   schema. A type present in some and missing from others is how a
   `ZonedDateTime` came to be typed temporal by the dataset and then
   refused by the tick generator, with a cast exception naming neither
   the column nor the axis.

   This namespace requires nothing of the library, so both
   `impl.resolve` and `impl.pose-schema` can read it without a cycle."
  (:require [java-time.api :as jt]))

(def readings
  "Every value a temporal axis reads, and how each is read as a
   `LocalDateTime`.

   A value carrying an offset is read at UTC, which is the offset
   `resolve/epoch-ms->local-date-time` reads back at, so a value
   round-trips.

   A value naming a period rather than a moment -- `YearMonth`, `Year`
   -- is deliberately absent. Which instant such a value stands for is
   a decision, not a conversion, and `resolve/warn-unread-temporal!`
   says so rather than making it."
  [[jt/local-date-time?                     identity]
   [jt/local-date?                          #(jt/local-date-time % (jt/local-time 0))]
   [jt/instant?                             #(jt/local-date-time % "UTC")]
   [#(instance? java.util.Date %)           #(jt/local-date-time (jt/instant %) "UTC")]
   [#(instance? java.time.ZonedDateTime %)  #(jt/local-date-time
                                              (.toInstant ^java.time.ZonedDateTime %) "UTC")]
   [#(instance? java.time.OffsetDateTime %) #(jt/local-date-time
                                              (.toInstant ^java.time.OffsetDateTime %) "UTC")]])

(def accepted-names
  "The types `readings` covers, for a message that has to list them."
  "LocalDate, LocalDateTime, Instant, ZonedDateTime, OffsetDateTime or java.util.Date")

(defn temporal-value?
  "True of a value a temporal axis reads as a date.

   Asked before converting, which is what an axis setting written as
   `(jt/local-date 2019 1 1)` needs -- on `:domain`, on `:breaks` and
   on `:include` alike."
  [v]
  (boolean (some (fn [[pred _]] (pred v)) readings)))

(defn ->local-date-time
  "A temporal value as the `LocalDateTime` the tick generators read, or
   the value unchanged where it is not one."
  [v]
  (if-let [[_ ->ldt] (first (filter (fn [[pred _]] (pred v)) readings))]
    (->ldt v)
    v))

(defn names-a-period?
  "True of a `java.time` value that names a stretch of time rather than
   a moment -- a `YearMonth`, a `Year`, a `MonthDay` -- and so has no
   instant of its own for an axis to place it at.

   Asked of anything temporal that `readings` does not cover, so a new
   type of this kind is reported rather than read as a category in
   silence."
  [v]
  (and (instance? java.time.temporal.Temporal v)
       (not (temporal-value? v))))
