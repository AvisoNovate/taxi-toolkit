(ns io.aviso.taxi-toolkit.fixtures
  "General clojure.test purpose fixtures for running selenium/webdriver tests.
  Please note that this namespace relies on webdriver-remote library."
  (:require [clj-webdriver.taxi :as taxi]
            [clj-webdriver.remote.server :as webdriver-remote])
  (:import (java.util.concurrent TimeUnit)))

(defn webdriver-remote-fixture
  "The first argument should be a timeout in milliseconds that will be applied to scriptTimeout and pageLoadTimeout of the driver.

All other arguments are browser specs, and for each of them:

First, set up webdriver remote session for a port and a host specified as:
- system properties - selenium.port/selenium.host or
- environment variables - SELENIUM_PORT/SELENIUM_HOST
If SELENIUM_HOST is used, and SAUCE_USER_NAME environment variable is set, set the host
to include the username:password@host authentication string.

This follows the convention from: https://github.com/saucelabs/bamboo_sauce/wiki#referencing-environment-variables to some extent and
the default settings are localhost/4444.

Then, set the default browser size to 1680x1050 for each iteration.

Next, invoke the function provided (assuming - the tests).

Finally, quit the webdriver session."
  [timeout-ms & browser-specs]
  (fn [f]
    (doseq [browser-spec browser-specs]
      (let [[_ a-driver] (webdriver-remote/new-remote-session
                           {:port     (Integer/parseInt (or
                                                          (System/getProperty "selenium.port")
                                                          (System/getenv "SELENIUM_PORT")
                                                          "4444"))
                            :existing true
                            :host     (or
                                        (System/getProperty "selenium.host")
                                        (when-let [host (System/getenv "SELENIUM_HOST")]
                                          (if (System/getenv "SAUCE_USER_NAME")
                                            (str (System/getenv "SAUCE_USER_NAME") ":"
                                                 (System/getenv "SAUCE_API_KEY") "@"
                                                 host)
                                            host))
                                        "localhost")}
                           browser-spec)]
        (taxi/set-driver! a-driver)
        (.setScriptTimeout (.. (:webdriver a-driver) manage timeouts) timeout-ms TimeUnit/MILLISECONDS) ;meh meh meh
        (.pageLoadTimeout (.. (:webdriver a-driver) manage timeouts) timeout-ms TimeUnit/MILLISECONDS) ;meh meh meh
        (taxi/implicit-wait 200)                            ;meh
        (try
          (taxi/window-resize {:width 1680 :height 1050})
          (f)
          (finally (taxi/quit)))))))

(defn jvm-timeout-fixture
"General purpose fixture, in case Selenium tests would hang (it happens).

Takes one argument:
- timeout-seconds - timeout in seconds after which the JVM should be stopped if the
test is still running."
  [timeout-seconds]
  (fn [f]
    (let [finished? (atom false)]
      (doto (Thread. (fn []
                       (Thread/sleep (* timeout-seconds 1000))
                       (when-not @finished?
                         (println "Timeout after " timeout-seconds "seconds.")
                         (System/exit -100))))
        (.setDaemon true)
        (.start))
      (try
        (f)
        (finally
          (reset! finished? true))))))