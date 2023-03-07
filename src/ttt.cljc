(ns ttt
  (:require
   [com.fulcrologic.statecharts :as sc]
   [com.fulcrologic.statecharts.elements :refer [state transition assign log parallel on-entry on-exit data-model script-fn]]
   [com.fulcrologic.statecharts.events :refer [new-event]]
   [com.fulcrologic.statecharts.protocols :as sp]
   [com.fulcrologic.statecharts.simple :as simple]
   [com.fulcrologic.statecharts.chart :refer [statechart]]
   [com.fulcrologic.statecharts.util :refer [extend-key]]
   [com.fulcrologic.statecharts.algorithms.v20150901-validation :as v]
   [com.fulcrologic.statecharts.convenience :refer [on]]
   [portal.api :as p]
   [clojure.pprint :refer [pprint]]
   [taoensso.timbre :as log]))

(defonce portal (p/open))
(add-tap #'p/submit)
(def nk extend-key)

(def empty-board [nil nil nil nil nil nil nil nil nil])

(defn tile [id initial idx]
  "create tiles with namespaced ids and events"
  (let [o       (nk id "o")
        x       (nk id "x")
        empty   (nk id "empty")
        initial (nk id (name initial))
        toggle-o (nk id "toggle-o")
        toggle-x (nk id "toggle-x")]
    (state {:id      id
            :initial initial}
           (state {:id o}
                  (on-entry {}
                            (assign {:location [:board idx] :expr :o})
                            (log {:label (str "entering " o) :expr (fn [env data] (tap> data))}))
                  (on toggle-o empty)
                  (on toggle-x x))
           (state {:id x}
                  (on-entry {}
                            (assign {:location [:board idx] :expr :x})
                            (log {:label (str "entering " x) :expr (fn [env data] (tap> data))}))
                  (on toggle-x empty)
                  (on toggle-o o))
           (state {:id empty}
                  (on-entry {}
                            (assign {:location [:board idx] :expr :empty})
                            (log {:label (str "entering " empty) :expr (fn [env data] (tap> data))}))
                  (on toggle-o o)
                  (on toggle-x x)))))

(defn player [id initial]
  (let [idle (nk id "idle")
        waiting (nk id "waiting")
        playing (nk id "playing")
        initial (nk id (name initial))]
    (state {:id id
            :initial initial}
           (state {:id idle}
                  (on :waiting waiting)
                  (on :playing playing))
           (state {:id waiting}
                  (on :idle idle)
                  (on :playing playing))
           (state {:id playing}
                  (on :idle idle)
                  (on :waiting waiting)))))

(defn turn [id initial idx]
  (let [initial  (nk id (name initial))
        active   (nk id "active")
        inactive (nk id "inactive")
        advance  (nk id "advance")]
    (state {:id      id
            :initial initial}
           (state {:id active}
                  (on advance inactive))
           (state {:id inactive}))))

(def tic-tac-toe
  (statechart {}
              (parallel {}
                        (state {:id :players}
                               (parallel {}
                                         (player :player-x :idle)
                                         (player :player-o :idle)))
                        (state {:id :board}
                               (on-entry {}
                                         (assign {:location [:board] :expr empty-board})
                                         (assign {:location [:history] :expr []}))
                               (parallel {}
                                         (for [t (range 9)]
                                           (tile (keyword (str "tile" t)) :empty t))))
                        (state {:id :turns}
                               (parallel {}
                                         (for [t (range 9)]
                                           (turn (keyword (str "turn" t)) :active t)))))))

(defn show-states [wmem] (println (sort (::sc/configuration wmem))))

(def env (simple/simple-env))

(simple/register! env ::ttt tic-tac-toe)

(def processor (::sc/processor env))

(comment
  (do
    (def t0 (sp/start! processor env ::ttt {::sc/session-id 1}))
    (def t1 (sp/process-event! processor env t0 (new-event :tile0/toggle-o)))
    (def t2 (sp/process-event! processor env t1 (new-event :tile0/toggle-x)))
    (def t3 (sp/process-event! processor env t2 (new-event :tile1/toggle-o)))
    (def t4 (sp/process-event! processor env t3 (new-event :tile1/toggle-empty)))
    (log/set-min-level! :debug)))

(comment
  (show-states t0)
  (pprint t0)
  (show-states t1)
  (pprint t1)
  (show-states t2)
  (pprint t2)
  (show-states t3)
  (pprint t3)
  (show-states t4)
  (pprint t4)
  (pprint s1))
