(defmulti ^:dynamic tap-report (fn [data] (:type data)))