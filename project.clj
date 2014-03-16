(defproject tailrecursion/boot.ring "0.1.0-SNAPSHOT"
  :description  "Boot tasks to create ring server components."
  :url          "https://github.com/tailrecursion/boot.ring"
  :license      {:name "Eclipse Public License"
                 :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure     "1.5.1"]
                 [ring                    "1.2.1"]
                 [tailrecursion/boot.core "2.2.2-SNAPSHOT"]]
  :target-path  "target/%s"
  :profiles     {:uberjar {:aot :all}})
