(ns vcftool.parser
  (:require [vcftool.plugin :as plugin]
            [clojure.tools.logging :as log]
            [vcftool.io :as io]
            [vcftool.config :refer [env]]))

(defn command
  "Dispatch to different function.

   Load a parser plugin and then call the parse function. An plugin must
   have parse function, it accept an input file and return the parsed text
   which are stored in a hash-map.
  "
  [{:keys [input output rtype ftype]}]
  (plugin/local-repo (:vcftool-plugin-path @env))         ; Reset plugin-repo-path for load-plugin function
  (plugin/load-plugin rtype)
  (log/info "Load plugin" rtype)
  (-> ((find-var (symbol (str rtype "/" "parse"))) input)  ; The namespace of plugin is `plugin-name`
      ((io/find-out-func ftype) output)))