(ns ttt
  (:require
   [com.fulcrologic.statecharts :as sc]
   [com.fulcrologic.statecharts.elements :refer [state transition log parallel on-entry on-exit]]
   [com.fulcrologic.statecharts.events :refer [new-event]]
   [com.fulcrologic.statecharts.protocols :as sp]
   [com.fulcrologic.statecharts.simple :as simple]
   [com.fulcrologic.statecharts.chart :refer [statechart]]
   [com.fulcrologic.statecharts.util :refer [extend-key]]
   [com.fulcrologic.statecharts.algorithms.v20150901-validation :as v]
   [com.fulcrologic.statecharts.convenience :refer [on]]
   [clojure.pprint :refer [pprint]]
   [taoensso.timbre :as log]))

(def nk extend-key)

(def justone
  (statechart {}
              (state {:id :justone})))

(def toggle
  (statechart {}
              (state {:id      :toggle
                      :initial :off}
                     (state {:id :on}
                            (transition {:event  :toggle
                                         :target :off}))
                     (state {:id :off}
                            (transition {:event  :toggle
                                         :target :on})))))

(def threestep-sequential-circular
  (statechart {}
              (state {:id :threestep
                      :initial :one}
                     (state {:id :one}
                            (transition {:event :next
                                         :target :two}))
                     (state {:id :two}
                            (transition {:event :next
                                         :target :three}))
                     (state {:id :three}
                            (transition {:event :next
                                         :target :one})))))

(defn show-states [wmem] (println (sort (::sc/configuration wmem))))

(def env (simple/simple-env))

(simple/register! env ::toggle toggle)
(simple/register! env ::threestep threestep-sequential-circular)

(def processor (::sc/processor env))

(def three0 (sp/start! processor env ::threestep {::sc/session-id 1}))
(def three1 (sp/process-event! processor env t0 (new-event :next)))
(def three2 (sp/process-event! processor env t1 (new-event :next)))
(def three3 (sp/process-event! processor env t2 (new-event :next)))
(def three4 (sp/process-event! processor env t3 (new-event :next)))

(def toggle0 (sp/start! processor env ::threestep {::sc/session-id 1}))
(def toggle1 (sp/process-event! processor env toggle0 (new-event :toggle)))
(def toggle2 (sp/process-event! processor env toggle1 (new-event :toggle)))
(def toggle3 (sp/process-event! processor env toggle2 (new-event :toggle)))

(log/set-min-level! :debug)
(comment
  (show-states three0)
  (show-states three1)
  (show-states three2)
  (show-states three3)
  (show-states three4)
  (show-states toggle0)
  (show-states toggle1)
  (show-states toggle2)
  (pprint env))
