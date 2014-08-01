(defn subset? 
  "Is set1 a subset of set2?"
  {:added "1.2",
   :tag Boolean}
  [set1 set2]
  (and (<= (count set1) (count set2))
       (every? #(contains? set2 %) set1)))