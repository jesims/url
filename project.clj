(def VERSION (.trim (slurp "VERSION")))

(defproject io.jesi/url VERSION
  :description "Makes working with URLs in Clojure a little more pleasant."
  :url "http://github.com/jesims/url"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-parent "0.3.8"]]
  :clean-targets ^{:protect false} ["target"]
  :parent-project {:coords  [io.jesi/parent "3.10.0"]
                   :inherit [:plugins :managed-dependencies :deploy-repositories :dependencies :profiles :aliases :min-lein-version]}
  :dependencies [[org.clojure/clojure :scope "provided"]
                 [org.clojure/clojurescript :scope "provided"]
                 [pathetic "0.5.1"]]
  :profiles {:dev [:parent/dev {:dependencies [[io.jesi/customs]
                                               [thheller/shadow-cljs]]}]})
