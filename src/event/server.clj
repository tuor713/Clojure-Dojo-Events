(ns event.server
  (:require [noir.server :as server]))

(server/load-views "src/event/views/")

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port 8080]
    (server/start port {:mode mode
                        :ns 'event})))

