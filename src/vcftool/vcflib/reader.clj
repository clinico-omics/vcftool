(ns vcftool.vcflib.reader
  (:require [clojure.java.io :as io]
            [vcftool.vcflib.header-parser :as hp]
            [vcftool.vcflib.variant-parser :as vp])
  (:import [net.sf.samtools.util BlockCompressedInputStream]
           [java.io IOException]))

(def ^:private header? (partial re-find #"^#"))

(defn- open-input-stream
  "Open `s` as a BlockCompressedInputStream if it is in BGZF format, otherwise
   as a BufferedInputStream."
  [s]
  (let [raw-stream (io/input-stream s)]
    (if (BlockCompressedInputStream/isValidFile raw-stream)
      (BlockCompressedInputStream. raw-stream)
      raw-stream)))

;; The VcfReader record stores a java.io.BufferedReader and the parsed
;; VCF headers. We implement the java.io.Closeable interface so that a
;; VcfReader object can be used in with-open.
(defrecord VcfReader [reader headers]
  java.io.Closeable
  (close [_]
    (.close reader)))

(defn vcf-reader
  "Open a VCF for reading and parse the header. `vcf` can be anything
  understood by clojure.java.io/input-stream. Gzip-compressed data is
  detected and automatically decompressed."
  [vcf]
  (let [reader (io/reader (open-input-stream vcf))
        headers (hp/parse-headers (take-while header? (line-seq reader)))]
    (VcfReader. reader headers)))

(defn sample-ids
  "Return a list of sample ids from the VCF."
  [vcf]
  (drop 9 (get-in vcf [:headers :columns])))

(defn header
  "Return the specified header from the VCF. When no keys are
  specified, return a map with all headers from the VCF."
  [vcf & ks]
  (get-in vcf (into [:headers] ks)))

(defn variant-seq*
  [vcf variant-parser]
  (map variant-parser (line-seq (:reader vcf))))

(defn basic-variant-seq
  "Return a lazy sequence of parsed variants from the VCF using a basic parser.
  Faster than `variant-seq`, but does not parse the INFO and FORMAT field values."
  [vcf]
  (variant-seq* vcf (vp/basic-variant-parser (:headers vcf))))

(defn variant-seq
  "Return a lazy sequence of parsed variants from the VCF using a full parser.
  Parses the INFO and FORMAT field values according to the header specification."
  [vcf]
  (variant-seq* vcf (vp/variant-parser (:headers vcf))))

(defn pass?
  "Returns true if any of the variant filter values are 'PASS',
  otherwise false."
  [variant]
  (some #(= % "PASS") (:filter variant)))
