(ns convert.core
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




(comment
  )

(add-conversion :mm :cm :div-10 :*10)
(add-conversion :cm :dm :div-10)

(search-inverse? :cm :dm)

(add-conversion :a :b :a-to-b :b-to-a)
(add-conversion :b :c :b-to-c :c-to-b)


(defn not-membero [x l]
  (conde 
    [(== l '())]
    [(fresh [head tail]
       (conso head tail l)
       (!= x head)
       (not-membero x tail))]))

;; inverse the way we discard froms
;; instead of 
;; we could  also search for the inverse of the inverse 
;; before constructing a path
(defn conversionso 
  ([from to fns]
   (conversionso '() from to fns))
  
  ([excluded from to fns]
   (not-membero from excluded)
   (fresh [?fn] 
     (conde
       [(conversions from to ?fn)
        (== fns [?fn])]
       
       [(fresh [?excluded ?from ?fns]
          (conversions from ?from ?fn)
          (conso from excluded ?excluded)
          (conversionso ?excluded ?from to ?fns)  
          (conso ?fn ?fns fns))]))))





(defn conv [from to]
  (first
   (run 1 [c]
     (conversions from to c))))


(defn search-inverse [t1 t2]
  (first
   (run 1 [c]
     (conde
       [(conversions t2 t1 c)]
       [(fresh [?c ?tx ?ty]
          (conversions t1 t2   ?c)
          (conversions ?tx ?ty ?c)
          (conversions ?ty ?tx  c))]))))


(defn search-conversion [from to]
  (run* [q]
    (conversionso from to q)))




(-> (get-conversion :cm :km) first reverse)
(get-conversion :b :a)
(get-conversion :b :c)

(comment
(defn convert [val to-type]
  (let [c (first (conversion (type val) to-type))]
    (c val)))
)