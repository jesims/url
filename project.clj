(def VERSION (.trim (slurp "VERSION")))

(defproject io.jesi/url VERSION
  :description "Makes working with URLs in Clojure a little more pleasant."
  :url "http://github.com/jesims/url"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.9.0"
  :plugins [[lein-parent "0.3.6"]
            [lein-auto "0.1.3"]]
  :source-paths ["src"]
  :test-paths ["test"]
  :clean-targets ^{:protect false} ["target"]

  :parent-project {:coords  [io.jesi/parent "0.0.22"]
                   :inherit [:plugins :managed-dependencies :deploy-repositories :dependencies [:profiles :dev] :test-refresh]}

  :dependencies [[org.clojure/clojure]
                 [pathetic "0.5.1"]]

  :profiles {:test [:dev {:dependencies [[thheller/shadow-cljs]]}]})
