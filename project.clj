(defproject io.aviso/taxi-toolkit "0.2.9"
  :description "A Clojure library designed to help with writing integration tests using clj-webdriver."
  :url "https://github.com/AvisoNovate/taxi-toolkit"
  :license {:name "Apache Software License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-webdriver "0.7.2"]
                 [potemkin "0.3.12"]]
  :profiles {:dev {:dependencies [[org.seleniumhq.selenium/selenium-java "2.50.0"]]}})
