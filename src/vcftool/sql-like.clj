(ns vcftool.sql-like
  (:refer-clojure :exclude [or and]))

(def db (atom {}))

(defn setup-db
  [data]
  reset! db data)

(defn insert-into
  "adds a new record"
  [table-name record]
  (swap! db update-in [table-name] conj record))

(defmacro def-filter
  [name args-vec pred-body]
  `(defn ~name
     ~args-vec
     (fn [~'%]
       ~pred-body)))

(def-filter eq
  [attr val]
  (= (attr %) val))

(def-filter gt
  [attr val]
  (pos? (compare (attr %) val)))

(def-filter and
  [& preds]
  ((apply every-pred preds) %))

(def-filter or
  [& preds]
  ((apply some-fn preds) %))

(defn where
  [pred]
  pred)

(defn select
  [table filter-pred]
  (filter filter-pred
          (table @db)))