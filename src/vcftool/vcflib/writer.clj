(ns vcftool.vcflib.writer
  (:require [clojure.string :as str]))

(defn get-name
  [col-name]
  (if (= col-name :chr)
    "#CHROM"
    (name col-name)))

(defn sample-ids
  [gtype]
  (keys gtype))

(defn sample-vals
  [gtype]
  (mapv gtype (sample-ids gtype)))

(defn join-coll
  [coll sep]
  (apply str (interpose sep coll)))

(defn join-val
  ([val] (join-val val ","))
  ([val sep]
   (cond
     (vector? val) (join-coll
                    (map #(join-val %) val)
                    sep)
     (seq? val) (join-coll
                 (map #(join-val %) val)
                 sep)
     (map? val) (join-coll
                 (map (fn [[k v]]
                        (if (true? v)
                          k
                          (str k "=" (join-val v)))) val)
                 sep)
     (string? val) val
     (number? val) val
     (nil? val) ".")))

(defn join-gtype-record
  "{GT 1/1, GQ 43, DP 5, HQ [nil nil]}"
  [gtype-record]
  (join-val (vals gtype-record) ":"))

(defn tranform-record
  [record]
  (concat [(:chr record)
           (join-val (:pos record))
           (join-val (:id record) ";")
           (join-val (:ref record))
           (join-val (:alt record) ",")
           (join-val (:qual record))
           (join-val (:filter record) ",")
           (join-val (:info record) ";")
           (join-val (:format record) ":")]
          (map join-gtype-record (sample-vals (:gtype record)))))

(defn variant-dump
  ([variant-map] (variant-dump variant-map []))
  ([variant-map metadata-headers]
   (let [metadata ()
         columns [:chr :pos :id :ref :alt :qual :filter :info :format]
         headers (concat (map #(str/upper-case (get-name %)) columns)
                         (sample-ids (:gtype (first variant-map))))
         rows (map tranform-record variant-map)]
     (cons (vector metadata-headers) (concat [headers] rows)))))
