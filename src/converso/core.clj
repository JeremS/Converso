(ns converso.core
  (:refer-clojure :exclude (== !=))
  (:use clojure.core.logic))

(defrel conversions ^:index from ^:index to ^:index f)


(defn add-conversion 
  "Function that adds a conversion to the 
  conversion table and if provided the inverse 
  of that conversion."
  ([t1 t2 t1->2]
   (fact conversions t1 t2 t1->2))
  ([t1 t2 t1->2 t2->1]
   (do
     (add-conversion t1 t2 t1->2)
     (add-conversion t2 t1 t2->1))))

(defn conv [from to]
  "Function that looks for a conversion. 
  It can only find specified coversions."
  (first
   (run 1 [c]
     (conversions from to c))))

(defn remove-conversion 
  "Function that removes a conversion."
  [t1 t2]
  (let [c (conv t1 t2)]
    (retraction conversions t1 t2 c)))

(defn remove-all-conversions
  "Function that removes a conversion for 
  the pair (type-from, type-to) and the pair
  (type-to, type-from) if it exists."
  [from to]
  (do
    (remove-conversion from to)
    (remove-conversion to   from)))

(defn clear-all-conversions 
  "Clear the all conversion table."
  []
  (let [convs (run* [from to]
                (fresh [?c]
                  (conversions from to ?c)))]
    (doseq [[from to] convs]
      (remove-conversion from to))))


(defn not-membero 
  "A goal that succeeds when a value x 
  is not inside a list l."
  [x l]
  (conde 
    [(== l '())]
    [(fresh [head tail]
       (conso head tail l)
       (!= x head)
       (not-membero x tail))]))


(defn inverso
  "A goal that find the inverse of a function
  if that inverse exists.
  
  We can look for an inverse passing a function 
  directly (arity 2) or we can pass the type to convert
  from and the type to convert to. (arity 3)"
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
  "Function that looks for the inverse of a conversion.
  
  - [c] looks for the inverse of c
  - [from to] looks for what would be (conv to from).
  
  In certain cases the [from to] can find a function
  even if (conv to from) isn't specified."
  ([c]
   (first
    (run 1 [inverse]
      (inverso c inverse))))
  ([from to]
   (first
    (run 1 [inverse]
      (inverso from to inverse)))))

(defn converso
  "A goal that looks for a conversion. Note that it construct 
  a list of lists of conversions because, if possible, it can
  find a compositions of conversions to convert from `from`
  to `to` if (conv from to) isn't specified. 
  
  More precisely it finds every possible combinations of 
  conversions that can do the job.
  
  It does so using differents strategies :
  
  - if (conv from to) exists -> it returns it
  - if (conv from to) doesn't exists but 
    (search-inverse from to) does, it looks for
    (-> (search-inverse from to)
         (search-inverse))
    because
    (-> (search-inverse from to)
        (search-inverse))
    <=> (conv from to)
  
  - if none of the above it can search for transitive relations like
   (conv a c) <=> (conv a b ) then (conv b c).
  
  Looking for a path of conversions it is able to use each strategy 
  at each step."
  ([from to fns]
   (converso '() from to fns))
  
  ([visited from to fns]
   
   (not-membero from visited)
   (fresh [?fn] 
     (conda
       [(conda
          [(conversions from to ?fn)]
          [(inverso to from ?fn)])
        (conso ?fn '() fns )]
       
       [(fresh [?visited ?to ?fns]
          (conda
            [(conversions from ?to ?fn)]
            [(inverso ?to from ?fn)])
          (not-membero ?to visited)
          (conso from visited ?visited)
          (converso ?visited ?to to ?fns)  
          (conso ?fn ?fns fns))]))))

(defn search-conversions
  "A function that looks for a conversions from `from`
  to `to` using converso."
  [from to]
  (run* [c]
    (converso from to c)))

(defn search-conversion 
  "Search a conversion from the type `from`
  to the type `to`. Returns identity if the type 
  are the same."
  [from to]
  (if (= from to) 
    identity
    (when-let [cs (search-conversions from to)]
      (->> cs
           first
           reverse
           (apply comp)))))

(defn convert 
  "Convert a value `value` to the type `to`"
  [value to]
  (let [from (type value)
        c (search-conversion from to)]
    (if c 
      (c to)
      (throw (ex-info "No conversion." {:from from
                                        :to to})))))
