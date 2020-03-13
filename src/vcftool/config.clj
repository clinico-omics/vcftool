(ns vcftool.config
  (:require
   [cprop.core :refer [load-config]]
   [cprop.source :as source]))

(def env
  (atom {:vcftool-plugin-path "/etc/vcftool/plugins"}))

(defn load-env
  []
  (reset! env
          (load-config
           :merge
           [(source/from-system-props)     ; Priority Lowest
            (source/from-env)])))          ; Priority Highest
