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
                  (transition {:event  :toggle-x
                               :target x}))
           (state {:id x}
                  (transition {:event  :toggle-empty
                               :target empty}))
           (state {:id empty}
                  (transition {:event :toggle-o
                               :target o})))))

(def tic-tac-toe
  (statechart {}
              (parallel {}
                        (ttt-cell :c0 :empty))))

(def onoff
  (statechart {}
              (parallel {}
                        (state {:id :toggle
                                :initial :off}
                               (state {:id :on}
                                      (transition {:event  :turn-off
                                                   :target :off}))
                               (state {:id :off}
                                      (transition {:event  :turn-on
                                                   :target :on}))))))

(defn show-states [wmem] (println (sort (::sc/configuration wmem))))

(def env (simple/simple-env))

(simple/register! env ::ttt tic-tac-toe)
(simple/register! env ::toggle onoff)

(def processor (::sc/processor env))

(def t0 (sp/start! processor env ::ttt {::sc/session-id 1}))
(def t1 (sp/process-event! processor env t0 (new-event :toggle-o)))
(def t2 (sp/process-event! processor env t1 (new-event :toggle-x)))
(def t3 (sp/process-event! processor env t2 (new-event :toggle-empty)))
(def t4 (sp/process-event! processor env t3 (new-event :toggle-o)))
(def t5 (sp/process-event! processor env t4 (new-event :toggle-empty)))

(def s0 (sp/start! processor env ::toggle {::sc/session-id 2}))
(def s1 (sp/process-event! processor env s0 (new-event :turn-off)))
(def s2 (sp/process-event! processor env s1 (new-event :turn-on)))
(def s3 (sp/process-event! processor env s2 (new-event :turn-on)))
(def s4 (sp/process-event! processor env s3 (new-event :turn-off)))

(comment
  (show-states s0)
  (show-states s1)
  (show-states s2)
  (show-states s3)
  (show-states s4)
  (show-states t0)
  (show-states t1)
  (show-states t2)
  (show-states t3)
  (show-states t4)
  (show-states t5)
  (pprint env)
  (pprint s0)
  (pprint s1))
