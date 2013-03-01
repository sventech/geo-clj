(ns geo.core
  (:require [clojure.string :refer [join]]))

(defprotocol ICoordinate
  (coordinates [obj] "Returns the coordinates of the `obj`."))

(defprotocol IPoint
  (point-x [point] "Returns the x coordinate of `point`.")
  (point-y [point] "Returns the y coordinate of `point`.")
  (point-z [point] "Returns the z coordinate of `point`."))

(defprotocol IWellKnownText
  (wkt [obj] "Returns `obj` as a WKT formatted string."))

(defn- format-position [p]
  (let [[x y z] p]
    (str x " " y (if z (str " " z)))))

(defrecord LineString [coordinates]
  ICoordinate
  (coordinates [geo]
    coordinates)
  IWellKnownText
  (wkt [geo]
    (str "LINESTRING(" (join "," (map format-position coordinates)) ")")))

(defrecord MultiLineString [coordinates]
  ICoordinate
  (coordinates [geo]
    coordinates)
  IWellKnownText
  (wkt [geo]
    (let [coordinates (map #(str "(" (join "," (map format-position %1)) ")") coordinates)]
      (str "MULTILINESTRING(" (join "," coordinates) ")"))))

(defrecord MultiPolygon [coordinates]
  ICoordinate
  (coordinates [geo]
    coordinates)
  IWellKnownText
  (wkt [geo]
    (let [coordinates
          (map (fn [polygon]
                 (str "(" (join "," (map #(str "(" (join "," (map format-position %1)) ")") polygon)) ")"))
               coordinates)]
      (str "MULTIPOLYGON(" (join "," coordinates) ")"))))

(defrecord MultiPoint [coordinates]
  ICoordinate
  (coordinates [geo]
    coordinates)
  IWellKnownText
  (wkt [geo]
    (str "MULTIPOINT(" (join "," (map format-position coordinates)) ")")))

(defrecord Point [coordinates]
  ICoordinate
  (coordinates [geo]
    coordinates)
  IPoint
  (point-x [geo]
    (nth coordinates 0))
  (point-y [geo]
    (nth coordinates 1))
  (point-z [geo]
    (nth coordinates 2 nil))
  IWellKnownText
  (wkt [geo]
    (str "POINT" (seq coordinates))))

(defrecord Polygon [coordinates]
  ICoordinate
  (coordinates [geo]
    coordinates)
  IWellKnownText
  (wkt [geo]
    (let [coordinates (map #(str "(" (join "," (map format-position %1)) ")") coordinates)]
      (str "POLYGON(" (join "," coordinates) ")"))))

(defn point
  "Make a new Point."
  [x y & [z]]
  (->Point
   (if z
     [(float x) (float y) (float z)]
     [(float x) (float y)])))

(defn line-string
  "Make a new LineString."
  [& coordinates]
  (->LineString
   (vec (map #(vec (map float %1)) coordinates))))

(defn multi-point
  "Make a new MultiPoint."
  [& coordinates]
  (->MultiPoint (vec (map #(vec (map float %1)) coordinates))))

(defn multi-line-string
  "Make a new MultiLineString."
  [& coordinates]
  (->MultiLineString
   (vec (map (fn [line] (vec (map #(vec (map float %1)) line)))
             coordinates))))

(defn multi-polygon
  "Make a new MultiPolygon."
  [& coordinates]
  (->MultiPolygon
   (vec (map (fn [polygon]
               (vec (map (fn [ring] (vec (map #(vec (map float %1)) ring))) polygon)))
             coordinates))))

(defn polygon
  "Make a new Polygon."
  [& coordinates]
  (->Polygon
   (vec (map (fn [ring] (vec (map #(vec (map float %1)) ring)))
             coordinates))))

(defn print-wkt
  "Print the geometric `obj` as `type` to `writer`."
  [type obj writer]
  (.write writer (str "#geo/" (name type)))
  (.write writer (pr-str (coordinates obj))))

;; PRINT-DUP

(defmethod print-dup LineString
  [geo writer]
  (print-wkt :line-string geo writer))

(defmethod print-dup MultiLineString
  [geo writer]
  (print-wkt :multi-line-string geo writer ))

(defmethod print-dup MultiPoint
  [geo writer]
  (print-wkt :multi-point geo writer))

(defmethod print-dup MultiPolygon
  [geo writer]
  (print-wkt :multi-polygon geo writer))

(defmethod print-dup Point
  [geo writer]
  (print-wkt :point geo writer))

(defmethod print-dup Polygon
  [geo writer]
  (print-wkt :polygon geo writer))

;; PRINT-METHOD

(defmethod print-method LineString
  [geo writer]
  (print-wkt :line-string geo writer))

(defmethod print-method MultiLineString
  [geo writer]
  (print-wkt :multi-line-string geo writer))

(defmethod print-method MultiPoint
  [geo writer]
  (print-wkt :multi-point geo writer))

(defmethod print-method MultiPolygon
  [geo writer]
  (print-wkt :multi-polygon geo writer))

(defmethod print-method Point
  [geo writer]
  (print-wkt :point geo writer))

(defmethod print-method Polygon
  [geo writer]
  (print-wkt :polygon geo writer))

;; READER

(defn read-line-string
  "Read a LineString from `coordinates`."
  [coordinates] (->LineString coordinates))

(defn read-multi-line-string
  "Read a MultiLineString from `coordinates`."
  [coordinates] (->MultiLineString coordinates))

(defn read-multi-point
  "Read a MultiPoint from `coordinates`."
  [coordinates] (->MultiPoint coordinates))

(defn read-multi-polygon
  "Read a MultiPolygon from `coordinates`."
  [coordinates] (->MultiPolygon coordinates))

(defn read-point
  "Read a Point from `coordinates`."
  [coordinates] (->Point coordinates))

(defn read-polygon
  "Read a Point from `coordinates`."
  [coordinates] (->Polygon coordinates))

(def ^:dynamic *readers*
  {'geo/line-string read-line-string
   'geo/multi-line-string read-multi-line-string
   'geo/multi-point read-multi-point
   'geo/multi-polygon read-multi-polygon
   'geo/point read-point
   'geo/polygon read-polygon})
