(ns r2.xmpp
  (:use [clojure.contrib.trace :only [trace]]
        [clojure.contrib.strint :only [<<]])
  (:import (org.jivesoftware.smack XMPPConnection ConnectionConfiguration
                                   PacketListener MessageListener)
           (org.jivesoftware.smack.packet Message)
           (org.jivesoftware.smackx.muc MultiUserChat)))

(defn connect [jid pass]
  (doto (XMPPConnection. (last (.split jid "@")))
    .connect
    (.login (first (.split jid "@")) pass)))

(defn muc [connection room]
  (doto (MultiUserChat. connection room)
    (.join "r2")))

(defn parse-login [who]
  (let [parsed (.split who "/")]
    (if (= (alength parsed) 1)
      who
      (aget parsed 1))))

(defn packet-listener [room-object]
  (proxy [PacketListener] []
    (processPacket
      [p]
      (try
        (let [body (.getBody p)
              who (parse-login (.getFrom p))
              [_ cmd args] (re-find #"^:(\w+)\s+(.*)$" body)
              send-msg (fn [text]
                         (let [msg (.createMessage room-object)]
                           (.setBody msg (str who ": " text))
                           (.sendMessage room-object msg)))]
          (cond
           (= cmd "doc") (send-msg
                          (with-out-str
                            (eval (read-string (<< "(doc ~{args})")))))))
        (catch Exception e (prn e))))))

(defn listen-rooms [connection rooms]
  (doseq [room rooms]
    (let [room-object (muc connection room)
          lst (packet-listener room-object)]
      (.addMessageListener room-object lst))))
