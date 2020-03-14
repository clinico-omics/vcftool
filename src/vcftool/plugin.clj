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

(defn all-plugins
  []
  (map
   #(first (str/split % #"\."))
   (mapv #(.getName %)
         (filter #(and (.isFile %)
                       (str/ends-with? % ".clj"))
                 (file-seq (clojure.java.io/file @repo))))))

(defn load-plugin
  [name]
  (load-plugins (partial match-name? name)))

(defn load-plugin-metadata
  [name]
  (let [metadata (find-var (symbol (str name "/" "metadata")))
        parser (find-var (symbol (str name "/" "parse")))]
    (if (and metadata parser)
      (assoc (deref metadata) :runs (deref parser))
      (throw (Exception. (format "Not valid plugin: %s" name))))))