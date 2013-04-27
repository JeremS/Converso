(ns converso.core
  (:refer-clojure :exclude (== !=))
  (:use clojure.core.logic))

(defrel conversions from to f)


(defn add-conversion 
  ([t1 t2 t1->2]
   (fact conversions t1 t2 t1->2))
  ([t1 t2 t1->2 t2->1]
   (do
     (add-conversion t1 t2 t1->2)
     (add-conversion t2 t1 t2->1))))

(defn conv [from to]
  (first
   (run 1 [c]
     (conversions from to c))))

(defn remove-conversion [t1 t2]
  (let [c (conv t1 t2)]
    (retraction conversions t1 t2 c)))

(defn remove-all-conversions [from to]
  (do
    (remove-conversion from to)
    (remove-conversion to   from)))

(defn clear-all-conversions []
  (let [convs (run* [from to]
                (fresh [?c]
                  (conversions from to ?c)))]
    (doseq [[from to] convs]
      (remove-conversion from to))))

(comment
(defn search-inverse 
  ([c]
   (first
    (run 1 [inverse]
      (fresh [?from ?to]
        (conversions ?from ?to c)
        (conversions ?to ?from inverse)))))
  ([t1 t2]
  (first
   (run 1 [c]
     (conde
       [(conversions t2 t1 c)]
       [(fresh [?c ?tx ?ty]
          (conversions t1 t2   ?c)
          (conversions ?tx ?ty ?c)
          (conversions ?ty ?tx  c))])))))

)

(defn not-membero [x l]
  (conde 
    [(== l '())]
    [(fresh [head tail]
       (conso head tail l)
       (!= x head)
       (not-membero x tail))]))


(defn inverso 
  ([c inverse]
   (fresh [?from ?to ?c]
     (conversions ?from ?to c)
     (conversions ?to   ?from inverse)))
  ([from to inverse]
   (conde
     [(conversions to from inverse)]
     [(fresh [?from ?to ?c]
        (conversions from to ?c)
        (conversions ?from ?to ?c)
        (conversions ?to ?from inverse))])))

(defn search-inverse
  ([c]
   (first
    (run 1 [inverse]
      (inverso c inverse))))
  ([from to]
   (first
    (run 1 [inverse]
      (inverso from to inverse)))))

;; we could  also search for the inverse of the inverse 
;; before constructing a path
(defn converso 
  ([from to fns]
   (converso '() from to fns))
  
  ([visited from to fns]
   
   (not-membero from visited)
   (fresh [?fn] 
     (conda
       [(conversions from to ?fn)
        (== fns [?fn])]
       
       [(fresh []
          (inverso to from ?fn)
          (== fns [?fn]))]
       
       [(fresh [?visited ?to ?fns]
          (conversions from ?to ?fn)
          (not-membero ?to visited)
          (conso from visited ?visited)
          (converso ?visited ?to to ?fns)  
          (conso ?fn ?fns fns))]))))

(defn search-conversions [from to]
  (run* [c]
    (converso from to c)))

(defn setup4 []
  (do
    (add-conversion ::mm ::cm    ::div-by-10)
    (add-conversion ::cm ::dm    ::div-by-10)
    (add-conversion ::cm ::decam ::div-by-1000 :*1000)
    (add-conversion ::hm ::dm    ::*1000)
    (add-conversion ::hm ::km    ::div-by-10)))

(setup4)
(search-conversions ::mm ::km)

(comment
(defn convert [val to-type]
  (let [c (first (conversion (type val) to-type))]
    (c val)))
)