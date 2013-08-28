import java.io.IOException
import org.slf4j.LoggerFactory
import org.specs2.execute.Result
import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class ApplicationSpec extends Specification {
  val logger = LoggerFactory.getLogger(classOf[ApplicationSpec])

  object Karma {
    def isPresent = {
      try {
        new ProcessBuilder("karma", "--version")
      } catch {
        case io:IOException => false
      }
      true
    }

    def run (configFile:String) = {
      new KarmaRun(configFile).run
    }
  }

  class KarmaRun(configFile: String) {
    val confPath = getClass().getClassLoader.getResource(configFile).getFile

    def run : Result = {
      val builder = new ProcessBuilder("karma", "start", confPath)
      try {
        val karmaProcess = builder.start()
        println("karma runner starting")
        println(scala.io.Source.fromInputStream(karmaProcess.getInputStream()).getLines().mkString("\n"))

        if (karmaProcess.waitFor() == 0) success else failure
      } catch {
        case io:IOException => skipped("karma runner is not available on this system")
      }
    }

    override def toString = {
      configFile
    }
  }

  "Application" should {
    
    "redirect to the client page" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/")).get
        redirectLocation(home) must equalTo(Some("/sd/app/index.html"))
      }
    }

    "implement the silent disco client protocol" in {
      running(TestServer(3333, FakeApplication())) {
        Karma.run("karma.conf.js")
      }
    }
  }

}