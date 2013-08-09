package model

import play.libs.Akka
import akka.util.Timeout
import akka.actor._
import scala.concurrent.duration._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.json.Json.toJson
import play.api.libs.json.Json.obj
import akka.pattern.ask
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.json.JsArray
import play.api.libs.json.JsString
import play.api.libs.json.JsObject
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.MongoController
import controllers.DB
import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime


case class CreateRoom(id: String)

class Rooms extends Actor {
  def receive = {
    case room: CreateRoom => {
      create(room.id)
    }
  }

  def create(id: String) = {
    try {
      sender ! context.children.find(_.path.name == id).getOrElse(context.actorOf(Props(new Room(id)), id))
    } catch {
      case ex: InvalidActorNameException => sender ! context.actorFor(id)
    }
  }
}

object Room {
  implicit val timeout = Timeout(1 second)
  implicit val executionContext = Akka.system().dispatcher
  val rooms = Akka.system().actorOf(Props[Rooms])

  def join(roomId: String): scala.concurrent.Future[(Iteratee[JsValue, _], Enumerator[JsValue])] = {
    // one actor per room
    (rooms ? CreateRoom(roomId)).flatMap {
      case room: ActorRef => {
        (room ? Connect()).map {

          case Connected(enumerator, iteratee) => {
            (iteratee, enumerator)
          }

          case CannotConnect(error) =>

            val iteratee = Done[JsValue, Unit]((), Input.EOF)

            // Send an error and close the socket
            val enumerator = Enumerator[JsValue](JsObject(Seq("error" -> JsString(error)))).andThen(Enumerator.enumInput(Input.EOF))

            (iteratee, enumerator)
        }
      }
    }
  }


}

/**
 * undo is not supported, I dont like the server-side state
 * of the undo protocol
 */
class Room(roomId: String) extends Actor with ActorLogging {

  var participants = Map.empty[String, Channel[JsValue]]
  var roomPosition = obj("status" -> JsNull)

  def receive = {

    case Connect() => {
      val (participantEnumerator, participantChannel): (Enumerator[JsValue], Channel[JsValue]) = Concurrent.broadcast[JsValue]
      sender ! Connected(participantEnumerator, createParticiant(participantEnumerator, participantChannel))
    }

    case AddParticipant(participantName, channel) => {
      participants = participants + (participantName -> channel)

      val tracks = JsArray(DB.getTracks(roomId))

      channel.push(obj(
        "channelJoined" -> obj(
          "user" -> obj("name" -> participantName, "id" -> participantName),
          "participants" -> JsArray(participants.keys.filter({
            p => !p.equals(participantName)
          }).map {
            key =>
              obj("name" -> toJson(key),
                "id" -> toJson(key))
          }.toSeq),
          "tracks" -> tracks,
          "room" -> obj("name" -> roomId, "id" -> roomId, "position" -> roomPosition,
            "time" -> DateTime.now().getMillis)
        )
      ))

      notifyAll(channel, "participantJoined", obj("user" -> obj("name" -> participantName, "id" -> participantName)))
    }

    case RemoveParticipant(channelToRemove) => {
      for ((participant, channel) <- participants if channel == channelToRemove) {
        participants -= participant
        notifyAll(channel, "participantLeft", obj("userId" -> participant))
      }
    }

    case ChatMessage(author, message) => {
      notifyAll(participants(author), "text", obj("message" -> message, "author" -> author))
    }

    case AddTrack(track: JsObject) => {
      val roomTrack = track.deepMerge(obj("roomName" -> roomId, "trackId" -> BSONObjectID.generate.stringify))

      DB.insertTrack(roomTrack)
      notifyAll(null, "trackAdded", obj("track" -> roomTrack, "position" -> JsNull, "user" -> JsNull))
    }

    case TrackStarted(trackId, user, position) => {
      roomPosition ++= obj("trackId" -> trackId, "position" -> position, "status" -> "PLAYING", "date" -> DateTime.now().getMillis)
      notifyAll(participants(user), "trackStarted", obj("trackId" -> trackId, "user" -> user, "position" -> position, "undo" -> JsNull))
    }

    case TrackStopped(trackId, user) => {
      notifyAll(participants(user), "trackStopped", obj("trackId" -> trackId, "user" -> user, "undo" -> JsNull))
    }

    case removed: TrackRemoved => {
      DB.removeTrack(roomId, removed.trackId)
      notifyAll(null, "trackRemoved", obj("trackId" -> removed.trackId, "user" -> removed.user, "undo" -> JsNull))
    }

    case move: MoveTrack => {
      DB.moveTrack(roomId, move.trackId, move.from, move.to)
    }

    case _: Any => {
      println("unrecognized event")
    }

  }

  def notifyAll(exclude: Channel[JsValue], kind: String, payload: JsObject) {
    val msg = JsObject(Seq(kind -> payload))
    for (participant <- participants.values if participant != exclude) {
      participant.push(msg)
    }
  }

  def extractMessage(event: JsValue) = {
    event.as[JsObject].fields(0) match {
      case ("channelJoin", payload) => ChannelJoin((payload \ "participantName").as[String])
      case ("text", payload) => Text((payload \ "message").as[String])
      case ("addTrack", payload) => AddTrack((payload \ "track").as[JsObject])
      case ("removeTrack", payload) => RemoveTrack((payload \ "trackId").as[String])
      case ("startTrack", payload) => StartTrack((payload \ "trackId").as[String], (payload \ "position").as[Int])
      case ("stopTrack", payload) => StopTrack((payload \ "trackId").as[String])
      case ("moveTrack", payload) => MoveTrack((payload \ "trackId").as[String], ((payload \ "from") \ "position").as[Int], ((payload \ "to") \ "position").as[Int])
    }
  }

  def createParticiant(participantEnumerator: Enumerator[JsValue], participantChannel: Concurrent.Channel[JsValue]): Iteratee[JsValue, _] = {
    val iteratee: Iteratee[JsValue, _] = Iteratee.foreach {
      event =>

        val participant = participants.find(_._2 == participantChannel).getOrElse(("", null))._1

        extractMessage(event) match {
          case join: ChannelJoin => {
            self ! AddParticipant(join.participantName, participantChannel)
          }
          case text: Text => self ! ChatMessage(participant, text.text)
          case addTrack: AddTrack => self ! addTrack
          case remove: RemoveTrack => self ! TrackRemoved(remove.trackId, participant)
          case startTrack: StartTrack => self ! TrackStarted(startTrack.trackId, participant, startTrack.position)
          case stopTrack: StopTrack => self ! TrackStopped(stopTrack.trackId, participant)
          case move: MoveTrack => self ! move
        }
    }

    iteratee.mapDone {
      case _ => self ! RemoveParticipant(participantChannel)
    }
  }

}

case class Connect()

case class ChannelJoin(participantName: String)

case class AddParticipant(participantName: String, channel: Concurrent.Channel[JsValue])

case class RemoveParticipant(channel: Concurrent.Channel[JsValue])

case class Text(text: String)

case class AddTrack(track: JsObject)

case class StartTrack(trackId: String, position: Int)

case class StopTrack(trackId: String)

case class RemoveTrack(trackId: String)

case class MoveTrack(trackId: String, from: Int, to: Int)

case class ChatMessage(author: String, message: String)

case class TrackStarted(trackId: String, user: String, position: Int)

case class TrackRemoved(trackId: String, user: String)

case class TrackStopped(trackId: String, user: String)

case class Connected(enumerator: Enumerator[JsValue], iteratee: Iteratee[JsValue, _])

case class CannotConnect(msg: String)