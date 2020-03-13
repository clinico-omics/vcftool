(ns vcftool.core
  (:require [clojure.data.csv :as csv]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [clojure.java.io :as io])
  (:gen-class))

(defn find-max-len-item
  [drop-nums lines]
  (pmap #(concat (take drop-nums %) [(apply max-key count (drop drop-nums %))]) lines))

(defn transform-data
  [from to trans-fn drop-nums]
  (with-open [reader (io/reader from)
              writer (io/writer to)]
    (->> (csv/read-csv reader :separator \tab)
         (drop 1)
         (find-max-len-item drop-nums)
         (csv/write-csv writer))))

(def cli-options
  [["-s" "--skip SKIP" "Which column you want to handle"
    :default 4
    :parse-fn #(Integer/parseInt %)]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["This is my program. There are many like it, but this one is mine."
        ""
        "Usage: program-name [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Arguments:"
        "  input-file    Input file for transforming."
        "  output-file   Output file."
        ""
        "Please refer to the manual page for more information."]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (= 2 (count arguments))
      {:arguments arguments :options options}
      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [arguments options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (transform-data (first arguments) (nth arguments 1) find-max-len-item (:skip options)))))