(ns converso.core-test
  (:use converso.core
        midje.sweet)
  (:import clojure.lang.ExceptionInfo))

(defn teardown [] (clear-all-conversions))

(defn setup1 []
  (do
    (add-conversion ::a ::b ::a-b)
    (add-conversion ::c ::d ::c-d ::d-c)))

(facts "The simple ones..."
  (with-state-changes [(before :facts (setup1))
                       (after :facts (teardown))]
    (fact "We can specify conversions and find them"
      (conv ::a ::b) => ::a-b
      (conv ::c ::d) => ::c-d
      (conv ::d ::c) => ::d-c)

    (fact "we can remove conversions"
      (add-conversion ::y ::z ::y-z)
      (conv ::y ::z) => ::y-z

      (remove-conversion ::y ::z)
      (conv ::y ::z) => nil )

    (fact "We can remove more and its inverse for a pair of types"
      (add-conversion ::y ::z ::y-z ::z-y)
      (conv ::y ::z) => ::y-z
      (conv ::z ::y) => ::z-y

      (remove-all-conversions ::y ::z)
      (conv ::y ::z) => nil
      (conv ::z ::y) => nil)))

(fact "We can clear all conversions at once"
  (setup1)
  (conv ::a ::b) => ::a-b
  (conv ::c ::d) => ::c-d
  (conv ::d ::c) => ::d-c
  (clear-all-conversions)

  (conv ::a ::b) => nil
  (conv ::c ::d) => nil
  (conv ::d ::c) => nil)


(defn setup2 []
  (do
    (add-conversion ::mm ::cm ::div-by-10)
    (add-conversion ::cm ::dm ::div-by-10 ::*10)))



(facts "More funky ones"
  (with-state-changes [(before :facts (setup2))
                       (after :facts (teardown))]

    (facts "We can look for the inverse of a conversion"

      (fact "We naively don't directly find one for mm -> cm"
        (conv ::mm ::cm) => ::div-by-10
        (conv ::cm ::mm) => nil)

      (fact "but we can go look for it !"
        (search-inverse ::mm ::cm) => ::*10 )

      (fact "If we already know a function we can also search for its inverse"
        (search-inverse ::div-by-10) => ::*10
        (search-inverse ::*10)       => ::div-by-10))


    (fact (str "If a conversion doesn't exists but its inverse does "
               "maybe its inverse inverse is used and already exists."
               "This way we can use a conversion that not directly specified.")
      (conv ::cm :mm) => nil
      (search-inverse ::cm ::mm) => ::div-by-10
      (-> (search-inverse ::cm ::mm) search-inverse) => ::*10)))


(defn setup3 []
  (do
    (add-conversion ::mm ::cm ::div-by-10)
    (add-conversion ::cm ::dm ::div-by-10)
    (add-conversion ::dm ::m  ::div-by-10 ::*10)

    (add-conversion ::a ::b  ::a-b)
    (add-conversion ::b ::c  ::b-c)
    (add-conversion ::a ::d  ::a-d)
    (add-conversion ::d ::e  ::d-e)
    (add-conversion ::e ::c  ::e-c)
    (add-conversion ::c ::b  ::c-b)))



(facts "Now let's go crazy baby"
  (with-state-changes [(before :facts (setup3))
                       (after :facts (teardown))]
    (fact "We have a general search that can find by inverse"
      (conv ::cm :mm) => nil
      (search-conversions ::cm ::mm) => '((::*10)))


    (fact (str "If a conversion doesnt exists we can try to find an equivalent compositions "
               "of conversions which is transiently equivalent"
               (conv ::mm ::m) => nil

               (search-conversions ::mm ::m)
               => '((::div-by-10 ::div-by-10 ::div-by-10))

               (search-conversions ::a ::c)
               => '((::a-b ::b-c)
                    (::a-d ::d-e ::e-c))))

    (facts (str "Since we can compose conversions to find new ones "
                "we should be able to find inverses that we couldn't before"))))


(defn setup4 []
  (do
    (add-conversion ::mm ::cm    ::div-by-10)
    (add-conversion ::cm ::dm    ::div-by-10)
    (add-conversion ::cm ::decam ::div-by-1000 ::*1000)
    (add-conversion ::hm ::dm    ::*1000)
    (add-conversion ::hm ::km    ::div-by-10)))


(fact "The big final of the search we can use all technique together to find conversions"
  (setup4)

  (search-conversions ::mm ::km)
  => '((::div-by-10 ::div-by-10 ::div-by-1000 ::div-by-10))

  (teardown))

(defn *10 [n]
  (* n 10))

(defn div-10 [n]
  (/ n 10))

(defn *1000 [n]
  (* n 1000))

(defn div-1000 [n]
  (/ n 1000))

(defn setup5 []
  (do
    (add-conversion ::mm ::cm    div-10)
    (add-conversion ::cm ::dm    div-10)
    (add-conversion ::cm ::decam div-1000 *1000)
    (add-conversion ::hm ::dm    *1000)
    (add-conversion ::hm ::km    div-10)))

(fact "Now xe can convert a lot of things"
  (setup5)

  (search-conversions ::mm ::km)
  => [[div-10 div-10 div-1000 div-10]]

  ((search-conversion ::mm ::km) 1)
  => (/ 1 10 10 1000 10)

  (teardown))


(defrecord Num [n])

(fact "We have a convert utility"
  (add-conversion Long Num ->Num)

  (convert 1 Num) => (->Num 1)
  (convert(->Num 1) String) => (throws ExceptionInfo)

  (teardown))

