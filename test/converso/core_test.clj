(ns converso.core-test
  (:use convert.core
        midje.sweet))

(add-conversion ::a ::b ::a-b)
(add-conversion ::c ::d ::c-d ::d-c)




(facts "The simple ones..."
  (fact "We can specify conversions and find them"
    (conv ::a ::b) => ::a-b
    )
  
  (fact "we can look for the inverse of a conversions")
  
  (fact "we can remove conversions"))


(facts "More funky ones"
  (fact "We can look for the inverse of a conversion"
    (comment "use conv"))
  
  (fact "If it doesnt exists we can try to look for it"
    (comment "use search-inverse"))
  
  (fact (str "If a conversion doesn't exists but its inverse "
             "does maybe its inverse inverse is used and already exists")
    (comment "use inverse inverse")))

(facts "Going crazy"
  (fact (str "If a conversion doesnt exists we can try to find an equivalent compositions "
             "of conversions which is transiently equivalent"))
  
  (facts (str "Since we can compose conversions to find new ones "
              "we should be able to find inverses that we couldn't before")))