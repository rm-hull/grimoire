user=> (partition-by #(= 3 %) [1 2 3 4 5])
((1 2) (3) (4 5))