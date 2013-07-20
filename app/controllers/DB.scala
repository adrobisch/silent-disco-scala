package controllers

import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection._
import play.api.mvc.Controller
import play.api.libs.json._
import play.api.libs.json.Json.obj
import scala.concurrent.{Await}
import scala.concurrent.duration._


object DB extends Controller with MongoController {
  def tracks: JSONCollection = db.collection[JSONCollection]("tracks")

  def insertTrack(track:JsObject) {
    tracks.insert(track)
  }

  def removeTrack(roomId:String, trackId:String) {
    getTracks(roomId).filter(t => (t \ "trackId").as[String] equals trackId).map {
      tracks.remove(_)
    }
  }

  def moveTrack(roomId: String, trackId:String, from : Int, to:Int) = {
    val roomTracks = getTracks(roomId)
    val moved = roomTracks(from)

    // remove all
    roomTracks.map(roomTrack => tracks.remove(roomTrack))

    // re-add with new order
    (roomTracks.filterNot(o => o equals moved).splitAt(to) match {
      case (pre, post) => pre ::: moved :: post
      case _ => roomTracks
    }).foreach(insertTrack)
  }

  def getTracks(roomId:String) : List[JsObject] = {
    Await.result(tracks.find(obj("roomName" -> roomId)).cursor[JsObject].toList(), 10 seconds)
  }

}