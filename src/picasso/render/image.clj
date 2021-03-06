(ns picasso.render.image
  "Render BufferedImage objects"
  (:import
   [java.awt Image]
   [java.awt.image BufferedImage]
   [java.io ByteArrayOutputStream]
   [javax.imageio ImageIO])
  (:require
   [clojure.data.codec.base64 :as b64]
   [clojure.string :as string]
   [picasso.protocols :refer [make Renderable render]]))

(defn image-to-bytes [^Image image ^String type width height]
  (let [bi (BufferedImage. width height (if (#{"png" "gif"} type)
                                          BufferedImage/TYPE_INT_ARGB
                                          BufferedImage/TYPE_INT_RGB))
        baos (ByteArrayOutputStream.)]
    (doto (.getGraphics bi) (.drawImage image 0 0 width height nil))
    (ImageIO/write bi type baos)
    (.toByteArray baos)))

(defrecord ImageView [image alt type width height]
  Renderable
  (render [{:keys [image alt type width height]}]
    (make
     :html
     (format "<img src=\"data:image/%1$s;base64,%2$s\" width=\"%3$s\" height=\"%4$s\" alt=\"%5$s\" />"
             type (String. (b64/encode (image-to-bytes image type width height))) width height alt))))

(defn image-view [^BufferedImage image & {:keys [alt type width height]}]
  (let [alt (or alt "")
        type (string/lower-case (or type "png"))
        iw (.getWidth image)
        ih (.getHeight image)
        [w h] (cond
                (and width height) [(int width) (int height)]
                width [(int width) (int (* (/ width iw) ih))]
                height [(int (* (/ height ih) iw)) (int height)]
                :else [iw ih])]
    (ImageView. image alt type w h)))
