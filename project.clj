(defproject jeremys/converso "0.1.0"
  :description "Library to specify conversions"
  :url "https://github.com/JeremS/Converso"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.logic "0.8.3"]]

  :profiles {:dev
             {:dependencies [[org.clojure/tools.trace "0.7.5"]
                             [midje "1.5.1"]]}})
