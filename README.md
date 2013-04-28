# convert

A Clojure library allowing to specify conversions between types
and then find some conversions that aren't directly specified.

## Usage

This library is an experiment using [core.logic](https://github.com/clojure/core.logic)
to specify conversions between types.

For instance you can specify:

```clojure
(ns ...
  (use converso.core))
  
(add-conversion :mm :cm #(/ % 10) #(* % 10))

```

and then do something like:

```clojure
((search-conversion :mm :cm) 10)
;=> 1

((search-conversion :cm :mm) 10)
;=> 100
``

## The goodies
Using [core.logic](https://github.com/clojure/core.logic) to do only
simples conversions like above is a waste, multi-methods are fine 
to do this. Converso brings more to the table. 

Since we are working with conversions we can use their 
matehmatical properties to find conversions that are not specified.
This is were [core.logic](https://github.com/clojure/core.logic) is used.

This library use two mathematical properties:

 - inverse functions
 - transitivity

### Inverse functions
Suppose we want to specify a piece of the metric system. 
We will use the units:
 - millimeters :mm
 - centimeters :cm 
 - decimeters  :dm
 
We could specify the conversions like that:

```clojure
(add-conversion :mm :cm #(/ % 10) #(* % 10))
(add-conversion :cm :dm #(/ % 10) #(* % 10))
```
You can then look up for the conversions:
 - :mm -> :cm
 ```clojure
 (search-conversion :mm :cm)
 ```
 
 - :cm -> :mm
 ```clojure
 (search-conversion :cm :mm)
 ```
 
 - :cm -> :dm
 ```clojure
 (search-conversion :cm :dm)
 ```
 
 - :dm -> :cm
 ```clojure
 (search-conversion :dm :cm)
 ```

We it gets interesting is that Converso lets you do that:
```clojure
(defn *10    [n] (* n 10))
(defn div-10 [n] (/ n 10))

(add-conversion :mm :cm div-10 *10)
(add-conversion :cm :dm div-10)

(search-conversion :dm :cm)
;=> *10
```

Here we didn't specify the conversion from :dm to :cm.
However Converso is capable of finding it because 
```clojure
(add-conversion :mm :cm div-10 *10)
```
specifies implicitely that `div-10` and `*10` are inverse functions.
The conversion `:dm -> :cm` isn't specified but its inverse `:cm -> :dm` is, it
is the `div-10` function. Since we know that the inverse of 
`div-10` is `*10` we can say that 

```
:dm -> :cm 
<=> inverse(inverse(:dm -> :cm))
<=> inverse(div-10)
<=> *10
```

### Transitivity
Converso is also capable to use transitivity to find conversions.

If we take the previous example:
```clojure
(defn *10    [n] (* n 10))
(defn div-10 [n] (/ n 10))

(add-conversion :mm :cm div-10 *10)
(add-conversion :cm :dm div-10)
```

The conversion :mm :dm isn't specified but converso can do this:
```clojure
(search-conversion :mm :cm) ;=> div-10
(search-conversion :cm :dm) ;=> div-10

;thus
(search-conversion :mm :dm) ;=> (comp div-10 div-10)
```

`:mm :dm` isn't specified but Converso can still find a way to do it.

### Transitivity + inverse functions
Converso can use a combination of the two strategies to find conversions.

Once again with the example:
```clojure
(defn *10    [n] (* n 10))
(defn div-10 [n] (/ n 10))

(add-conversion :mm :cm div-10 *10)
(add-conversion :cm :dm div-10)
```
The conversion `:dm -> :mm` isn't spécified but using both strategies
Converso does this:
```clojure
(search-conversion :dm =mm) ;=> (comp *10 *10) ; using the composition of the following properties
  (search-conversion :dm :cm) ;=> div-10 (using the inverse :dm -> :cm)
  (search-conversion :dm :cm) :=> div-10 (user specified)
```

A more complicated is used in the tests :
```clojure
(defn *10 [n]
  (* n 10))

(defn div-10 [n]
  (/ n 10))

(defn *1000 [n]
  (* n 1000))

(defn div-1000 [n]
  (/ n 1000))

(add-conversion :mm :cm    div-10)
(add-conversion :cm :dm    div-10)
(add-conversion :cm :decam div-1000 *1000)
(add-conversion :hm :dm    *1000)
(add-conversion :hm :km    div-10)

(search-conversion :mm :km) ;=> (comp div-10 div-1000 div-10 div-10)
  (search-conversion :mm :cm) ;=> div-10 spec
  (search-conversion :cm :dm) ;=> div-10 spec
  (search-conversion :dm :hm) ;=> div-1000 inverse
  (search-conversion :hm :km) ;=> div-10 spec

(search-conversion :dm :hm)
<=> (inverse (search-conversion :hm :dm))
<=> (inverse *1000)
<=> (inverse (search-conversion :decam :cm))
<=> (search-conversion :cm :decam)
<=> div-1000
```

## Note
If we supress the use of the inverse functions Converso is 
graph traversing tool with types as nodes and conversions as arcs.

## License

Copyright © 2013 Jérémy Schoffen.

Distributed under the Eclipse Public License, the same as Clojure.
