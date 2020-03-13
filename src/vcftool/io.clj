(ns vcftool.io
  (:require [clojure.pprint :as pprint]))

(defn save2txt [lines file-path]
  (with-open [wtr (clojure.java.io/writer file-path)]
    (binding [*out* wtr]
      (doseq [line lines] (pprint/pprint line wtr)))))

(defn find-out-func
  [ftype]
  (case ftype
    "txt" save2txt
    println))