package au.csiro.data61.magda.indexer.external.registry

import java.io.IOException

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import au.csiro.data61.magda.indexer.external.{ExternalInterface, HttpFetcher, InterfaceConfig}
import au.csiro.data61.magda.model.misc.DataSet
import com.typesafe.config.Config
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import au.csiro.data61.magda.util.Collections.mapCatching
import au.csiro.data61.magda.model.Registry.Record
import au.csiro.data61.magda.indexer.external.registry.{RegistryIndexerProtocols, RegistryRecordsResponse}

import scala.concurrent.{ExecutionContext, Future}

class RegistryExternalInterface(interfaceConfig: InterfaceConfig, implicit val config: Config, implicit val system: ActorSystem, implicit val executor: ExecutionContext, implicit val materializer: Materializer) extends RegistryIndexerProtocols with ExternalInterface with RegistryConverters {
  implicit val fetcher = new HttpFetcher(interfaceConfig, system, materializer, executor)
  implicit val logger = Logging(system, getClass)

  override def getInterfaceConfig = interfaceConfig

  val baseUrl = s"v0/records?aspect=dcat-dataset-strings&aspect=source"

  override def getDataSets(start: Long, number: Int): scala.concurrent.Future[List[DataSet]] =
    fetcher.request(s"$baseUrl&optionalAspect=dataset-distributions&optionalAspect=temporal-coverage&dereference=true&start=$start&limit=$number").flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[RegistryRecordsResponse].map { registryResponse =>
          mapCatching[Record, DataSet](registryResponse.records,
            { hit => registryDataSetConv(interfaceConfig)(hit) },
            { (e, item) => logger.error(e, "Could not parse item for {}: {}", interfaceConfig.name, item.toString) }
          )
        }
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"Registry request failed with status code ${response.status} and entity $entity"
          Future.failed(new IOException(error))
        }
      }
    }

  override def getTotalDataSetCount(): Future[Long] =
    fetcher.request(s"$baseUrl&limit=0").flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[RegistryRecordsResponse].map(_.totalCount)
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"Registry request failed with status code ${response.status} and entity $entity"
          logger.error(error)
          Future.failed(new IOException(error))
        }
      }
    }
}
