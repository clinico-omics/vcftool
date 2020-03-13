(ns vcftool.plugin
  (:require [clojure.java.classpath :as cp]
            [clojure.string :as str]
            [clojure.tools.logging :refer [info warn error]]
            [vcftool.util :as util]
            [vcftool.config :refer [env]]))

(def repo
  (atom "/etc/vcftool/plugins"))

(defn local-repo
  "Sets the location of the local clojure repository used
   by `load-plugins` or `load-plugin`"
  [path]
  (reset! repo (util/expand-home path)))

(defn clj-file?
  [file-path]
  (str/ends-with? (.getName file-path) ".clj"))

(defn match-name?
  [name file-path]
  (and
   (clj-file? file-path)
   (= (format "%s.clj" name) (.getName file-path))))

(defn load-plugins
  ([] (load-plugins clj-file?))
  ([filter-fn]
   (if (or (util/is-dir? @repo) (util/is-file? @repo))
     (run! #(load-file (.getAbsolutePath %))
           (filter
            filter-fn
            (rest (file-seq (java.io.File. @repo)))))
     (throw (Exception. (format "No such path: %s" @repo))))))

(defn load-plugin
  [name]
  (load-plugins (partial match-name? name)))