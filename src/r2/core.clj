(ns r2.core
  (:use [clojure.contrib.command-line :only [with-command-line]]
        [clojure.contrib.strint :only [<<]]
        [clojure.string :only [split]]
        [r2.xmpp :only [connect listen-rooms]])
  (:import [java.io ByteArrayOutputStream ByteArrayInputStream]))

;; TODO: Smack always throws an NPE during authentication, but it does not
;; seem to affect the functionality.

(defn -main [& args]
  (with-command-line args
    "Bot buddy for daily chores."
    [[user "Jabber user name"]
     [password "Jabber password"]
     [server "Jabber server"]
     [rooms "Comma-separated chatroom names"]]
    (let [c (r2.xmpp/connect (<< "~{user}@~{server}") password)
          rooms (split rooms #",")]
      (do
        (println "Connected to " c " with rooms " rooms)
        (r2.xmpp/listen-rooms c rooms)
        (while true (Thread/sleep 3000))))))
