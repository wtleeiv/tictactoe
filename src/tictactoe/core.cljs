(ns tictactoe.core
    (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(def dim 3)
(def init-move-char "X")

(defn new-board [n]
  (vec (repeat n (vec (repeat n " ")))))

;; define app data so that it doesn't get over-written on reload
(defonce app-state (atom {:title "Tic Tac Toe"
                          :board (new-board dim)
                          :move-char init-move-char
                          :won false}))

(defn move [m]
  (if (= m "O")
    "X"
    "O"))

(defn check-win [b c d]
  (defn rows []
    (some identity
          (mapv (fn [x]
                  (every? identity
                          (mapv (fn [y]
                                  (= c (get (get b x) y)))
                                (range d))))
                (range d))))
  (defn cols []
    (some identity
          (mapv (fn [y]
                  (every? identity
                          (mapv (fn [x]
                                  (= c (get (get b x) y)))
                                (range d))))
                (range d))))
  (defn diag-down []
    (every? identity
            (mapv (fn [xy]
                    (= c (get (get b xy) xy)))
                  (range d))))
  (defn diag-up []
    (every? identity
            (mapv (fn [xy]
                    (= c (get (get b (- d 1 xy)) xy)))
                  (range d))))
  (some true? [(rows) (cols) (diag-down) (diag-up)]))

(defn blank [x y]
  [:rect {:x (+ x 0.05)
          :y (+ y 0.05)
          :width 0.9
          :height 0.9
          :fill "gray"
          :on-click
          (fn rect-click [e]
            (when (not (get @app-state :won))
              (let [m (move (get @app-state :move-char))]
                (swap! app-state assoc-in [:board y x] m)
                (swap! app-state assoc :move-char m)))
            (swap! app-state assoc :won
                   (check-win (get @app-state :board)
                              (get @app-state :move-char)
                              dim)))}])

(defn circle [x y]
  [:circle {:cx (+ x 0.5)
            :cy (+ y 0.5)
            :r 0.4
            :stroke "green"
            :stroke-width 0.08
            :fill "none"}])

(defn cross [x y]
  [:g {:stroke "darkred"
       :stroke-width "0.2"
       :stroke-linecap "round"
       :transform
       (str "translate(" (+ 0.5 x) "," (+ 0.5 y) ") "
            "scale(0.35)")}
   [:line {:x1 -1 :y1 -1 :x2 1 :y2 1}]
   [:line {:x1 -1 :y1 1 :x2 1 :y2 -1}]])

(defn tictactoe []
  [:center
   [:h1 (:title @app-state)]
   (when (get @app-state :won)
     [:h3 (str (get @app-state :move-char) " wins!")])
   [:p
    [:button
     {:on-click
      (fn new-game-click [e]
        (swap! app-state assoc :board (new-board dim))
        (swap! app-state assoc :move-char init-move-char)
        (swap! app-state assoc :won false))}
     "New game"]]
   ;; put rects into svg => remove :key warnings
   (into
    [:svg
     { ;; specify coordinate frame
      :view-box (str "0 0 " dim " " dim)
      ;; with these dimensions
      :width 500
      :height 500}]
    (for [x (range dim)
          y (range dim)]
      (case (get-in @app-state [:board y x])
        " " [blank x y]
        "O" [circle x y]
        "X" [cross x y])))])

(reagent/render-component [tictactoe]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state assoc-in [:board 0 0] "X")
  )
