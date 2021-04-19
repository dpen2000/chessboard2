(ns com.oakmac.chessboard2.core
  (:require
    [clojure.string :as str]
    [goog.dom :as gdom]
    [goog.object :as gobj]
    [com.oakmac.chessboard2.html :as html]
    [com.oakmac.chessboard2.util.fen :refer [fen->position position->fen]]
    [com.oakmac.chessboard2.util.base58 :refer [random-base58]]))

(defn random-square-id []
  (str "square-" (random-base58)))

(defn random-row-id []
  (str "row-" (random-base58)))


(def ruy-lopez-fen "r1bqkbnr/pppp1ppp/2n5/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R")


(def default-num-cols 8)
(def default-num-rows 8)
(def start-position-fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR")

(def ellipsis "…")

; (defn create-square-ids
;   "returns a map of coord --> square ids
;   used for the DOM elements on the page"
;   [num-rows num-cols])

;; TODO: move to dom-util namespace?
(defn grab-element
  "does it's best to grab a native DOM element from it's argument
  arg can be either:
  1) already a DOM element
  2) id of an element (getElementById)
  3) querySelector

  return nil if not able to grab the element"
  [arg]
  (let [el1 (gdom/getElement arg)]
    (if el1
      el1
      (let [el2 (.querySelector js/document arg)]
        (if el2 el2 nil)))))

(def default-options
  (js-obj "position" "start"))

(def initial-state
  {:arrows {}
   :num-rows 8
   :num-cols 8
   :orientation "white"
   :position {}})

(defn click-root-el [js-evt]
  (.log js/console "clicked root element:" js-evt))

(defn- add-events!
  "Attach DOM events."
  [root-el]
  (.addEventListener root-el "click" click-root-el))

(defn toggle-orientation [o]
  (if (= o "white") "black" "white"))

(defn- draw-board!
  "FIXME: write this"
  [{:keys [arrows orientation position root-el]}]
  (gobj/set root-el "innerHTML" (str "<h1>" orientation "</h1>")))

;; -----------------------------------------------------------------------------
;; API Methods

(defn orientation
  [board arg]
  (cond
    (= arg "white") (do (swap! board assoc :orientation "white")
                        (draw-board! @board)
                        "white")
    (= arg "black") (do (swap! board assoc :orientation "black")
                        (draw-board! @board)
                        "black")
    (= arg "flip") (do (swap! board update :orientation toggle-orientation)
                       (draw-board! @board)
                       (:orientation @board))
    :else (:orientation @board)))

(defn position
  "returns or sets the current board position"
  [board new-pos animate?]
  (cond
    (not new-pos) (:position @board)
    (= (str/lower-case new-pos) "fen") (position->fen (:position @board))
    :else nil))

;; -----------------------------------------------------------------------------
;; Constructor

(defn init-dom!
  [{:keys [root-el orientation position] :as board}]
  (gobj/set root-el "innerHTML" (html/BoardContainer board)))

(defn constructor
  "Called to create a new Chessboard2 object."
  ([el-id]
   (constructor el-id default-options))
  ([el js-opts]
   (let [root-el (grab-element el)
         initial-state2 (assoc initial-state :root-el root-el)
         board-state (atom initial-state2)]
     (init-dom! @board-state)
     (add-events! root-el)
     ;; return JS object that implements the API
     (js-obj
       "clear" #(position board-state {} %1)
       "destroy" "FIXME"
       "fen" "FIXME"
       "flip" #(orientation board-state "flip")
       "move" #()
       "orientation" #(orientation board-state %1)
       "position" #()
       "resize" #()
       "start" #(position board-state start-position %1)))))

(when js/window
  (gobj/set js/window "Chessboard2" constructor))
