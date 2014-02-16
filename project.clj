(defproject tailrecursion/boot.ring "0.1.0-SNAPSHOT"
  :description  "FIXME: write description"
  :url          "http://example.com/FIXME"
  :license      {:name "Eclipse Public License"
                 :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure     "1.5.1"]
                 [tailrecursion/boot.core "2.2.0"]
                 [ring                    "1.2.1"]]
  :target-path  "target/%s"
  :profiles     {:uberjar {:aot :all}})
