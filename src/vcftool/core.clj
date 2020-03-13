(ns vcftool.core
  (:require [clojure.string :as string]
            [vcftool.config :as config]
            [clojure.tools.logging :as log]
            [vcftool.parser :as parser]
            [cli-matic.core :refer [run-cmd]])
  (:gen-class))

(def CONFIGURATION
  {:app         {:command     "vcftool"
                 :description "A command-line parser for vcf file."
                 :version     "0.0.1"}

   :global-opts [{:option  "verbose"
                  :as      "Verbosity level; may be specified multiple times to increase value"
                  :type    :int
                  :default 0}]

   :commands    [{:command     "parser"
                  :description "Parse TEXT from vcf file."
                  :opts        [{:option "input" :short "i" :as "Input file/directory" :type :string :default :present}
                                {:option "output" :short "o" :as "Output file/directory" :type :string :default :present}
                                {:option "rtype" :short "r" :as "Plugin type supported by parser command." :type :string :default :present}
                                {:option "ftype" :short "f" :as "File type supported by parser command." :type #{"txt"} :default "txt"}]
                  :runs        parser/command}]})

(defn -main [& args]
  (log/info "Starting vcftool in STANDALONE mode")
  ; Load configuration from system-props & env
  (config/load-env)
  (run-cmd args CONFIGURATION))
