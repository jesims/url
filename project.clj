(def VERSION (.trim (slurp "VERSION")))

(defproject io.jesi/url VERSION
  :description "Makes working with URLs in Clojure a little more pleasant."
  :url "http://github.com/jesims/url"
  :min-lein-version "2.9.0"

  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [pathetic "0.5.1"]]

  :source-paths ["src"]
  :test-paths ["test"]

  :clean-targets ^{:protect false} ["target"]

  :plugins [[lein-auto "0.1.3"]]

  :profiles {:dev {:dependencies [[thheller/shadow-cljs "2.8.36"]]}})
