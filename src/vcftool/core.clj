(ns vcftool.core
  (:require [clojure.string :as string]
            [vcftool.config :as config]
            [clojure.tools.logging :as log]
            [vcftool.plugin :as plugin]
            [vcftool.config :refer [env]]
            [cli-matic.core :refer [run-cmd]])
  (:gen-class))

(defonce CONFIGURATION
  (atom
   {:app         {:command     "vcftool"
                  :description "A command-line parser for vcf file."
                  :version     "0.0.1"}

    :global-opts [{:option  "verbose"
                   :as      "Verbosity level; may be specified multiple times to increase value"
                   :type    :int
                   :default 0}]}))

(defn update-commands [value]
  (swap! CONFIGURATION #(assoc % :commands value)))

(defn preparse
  "prepare the configuration for all plugins.

   Note: An plugin must have parse and metadata function.
  "
  []
  (plugin/local-repo (:vcftool-plugin-path @env))         ; Reset plugin-repo-path for load-plugin function
  (plugin/load-plugins)
  (update-commands (map #(plugin/load-plugin-metadata %) (plugin/all-plugins))))

(defn -main [& args]
  (log/info "Starting vcftool in STANDALONE mode")
  ; Load configuration from system-props & env
  (config/load-env)
  (preparse)
  (run-cmd args @CONFIGURATION))
