(ns ttt
  (:require
   [com.fulcrologic.statecharts :as sc]
   [com.fulcrologic.statecharts.elements :refer [state transition parallel]]
   [com.fulcrologic.statecharts.events :refer [new-event]]
   [com.fulcrologic.statecharts.protocols :as sp]
   [com.fulcrologic.statecharts.simple :as simple]
   [com.fulcrologic.statecharts.chart :refer [statechart]]
   [com.fulcrologic.statecharts.util :refer [extend-key]]
   [clojure.pprint :refer [pprint]]))

(def empty-board [[nil nil nil]
                  [nil nil nil]
                  [nil nil nil]])

(def flat-empty-board (vec (flatten empty-board)))

(defn ttt-cell [id initial]
  (let [o       (extend-key id "o")
        x       (extend-key id "x")
        empty   (extend-key id "empty")
        initial (extend-key id (name initial))]
    (state {:id      id
            :initial initial}
           (state {:id o}
                  (transition {:event  :swap-cell
                               :target empty}))
           (state {:id x}
                  (transition {:event  :swap-cell
                               :target empty}))
           (state {:id empty}
                  (transition {:event  :reset
                               :target id})))))

(def ttt
  (statechart {}
              (parallel {}
                        (ttt-cell :c0 :empty))))

(defn show-states [wmem] (println (sort (::sc/configuration wmem))))

(def env (simple/simple-env))

(simple/register! env ::ttt ttt)

(def processor (::sc/processor env))

(def s0 (sp/start! processor env ::ttt {::sc/session-id 1}))
(def s1 (sp/process-event! processor env s0 (new-event :o)))
(def s2 (sp/process-event! processor env s1 (new-event :x)))
(def s3 (sp/process-event! processor env s2 (new-event :empty)))

(comment
  (show-states s0)
  (show-states s1)
  (show-states s2)
  (show-states s3)
  (pprint env)
  (pprint s0))
