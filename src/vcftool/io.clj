(ns vcftool.io
  (:require [clojure.pprint :as pprint]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(defn save2csv [sep lines file-path]
  (with-open [file (io/writer file-path)]
    (csv/write-csv file lines :separator sep :quote? #(some #{\"} %))))

(defn find-out-func
  [ftype]
  (case ftype
    "csv" (partial save2csv \,)
    "tsv" (partial save2csv \tab)
    "txt" (partial save2csv \space)
    println))