
# Plotje
Composable plotting in Clojure

![](readme_files/image0.svg)

---------------------

Plotje is a Clojure library for composable plotting, inspired by
the Grammar of Graphics.


## General information

|||
|-|-|
|Website | [https://scicloj.github.io/plotje/](https://scicloj.github.io/plotje/)
|Source |[![(GitHub repo)](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)](https://github.com/scicloj/plotje)|
|Deps |[![Clojars Project](https://img.shields.io/clojars/v/org.scicloj/plotje.svg)](https://clojars.org/org.scicloj/plotje)|
|License |[MIT](https://github.com/scicloj/plotje/blob/main/LICENSE)|
|Status |🛠alpha🛠|


## Usage

Plotje is intended to be used with data-visualization tools
that support the [Kindly](https://scicloj.github.io/kindly) convention
such as [Clay](https://scicloj.github.io/clay/).


## Quick example

Line chart with point markers from plain Clojure data:
```clj
(-> [{:month "Jan" :sales 120}
     {:month "Feb" :sales 95}
     {:month "Mar" :sales 140}
     {:month "Apr" :sales 175}
     {:month "May" :sales 160}
     {:month "Jun" :sales 210}]
    (pj/lay-line :month :sales)
    pj/lay-point
    (pj/options {:title "Monthly Sales"}))
```
![](readme_files/image1.svg)

Scatter plot matrix (SPLOM) -- all pairwise combinations with color grouping:
```clj
(-> (rdatasets/datasets-iris)
    (pj/pose (pj/cross [:sepal-length :sepal-width
                        :petal-length :petal-width]
                       [:sepal-length :sepal-width
                        :petal-length :petal-width])
             {:color :species})
    (pj/options {:title "Iris SPLOM"}))
```
![](readme_files/image2.svg)


## License

Copyright (c) 2025-2026 Scicloj

Distributed under the MIT License.