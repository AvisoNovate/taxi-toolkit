(ns io.aviso.taxi-toolkit.url
  "General tools for managing app's URL.")

(defn app-url
  "Get the actual application url. This method is handy when the application is ran on varying and/or virtualized
  environments - for example:
  - regression tests are developed on local machine/VirtualBox
  - bamboo rans them on remote agents
  - deployment is done with AWS.

 For each environment, the application url can be set as system property, for example
 -Dapp.url=http://10.0.2.2:8080
 or as an environment variable:
 APP_URL=http://10.0.2.2:8080
  "
  [& path]
  (apply str
         (or (System/getProperty "app.url")
             (System/getenv "APP_URL"))
         path))
