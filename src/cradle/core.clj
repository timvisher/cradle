(ns cradle.core
  (:require fs [clojure.java.io :as io] [clojure.contrib.str-utils :only str-join :as str-utils])
  (:use [clojure.contrib.command-line]))

(defn- globs->pattern [globs]
  (re-pattern
   (str ".+("
        (str-utils/str-join "|"
                            (map #(.replaceAll % "\\." "\\\\.") globs))
        ")")))

(defn as-file [path]
  (io/as-file path))

(defn hidden? [file]
  "Tests a File for it's hidden status"
  (.isHidden file))

(defn tree-paths
  "Get paths to all files under a directory. If included, include-globs resricts what is returned to files matching the include globs. Hidden files are excluded."
  ([]
     (tree-paths "."))
  ([dir]
     (tree-paths dir nil))
  ([dir include-globs]
     (map fs/normpath (filter (complement hidden?) (filter fs/file? (filter #(re-matches (globs->pattern include-globs) (.getName %)) (file-seq (io/as-file dir))))))))

(defn do-copy-tree [from to include-globs]
  (let [globs-pattern (globs->pattern include-globs)
        src-paths (tree-paths from include-globs)
        from (fs/normpath from)
        to (fs/normpath to)
        dest-paths (map #(.replaceFirst % from to) src-paths)]
    (dorun (map fs/copy+ src-paths dest-paths))))
