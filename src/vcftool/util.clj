(ns vcftool.util
  (:require [clojure.java.io :as io]))

(defn is-file? [path]
  (and path (.exists (io/as-file path))))

(defn is-dir? [path]
  (and path (.isDirectory (io/file path))))

(defn basename [path]
  (and path (.getName (io/file path))))

(defn expand-home [s]
  (if (.startsWith s "~")
    (clojure.string/replace-first s "~" (System/getProperty "user.home"))
    s))