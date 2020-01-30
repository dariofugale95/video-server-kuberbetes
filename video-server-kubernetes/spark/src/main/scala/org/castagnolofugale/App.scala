package org.castagnolofugale

import java.io.{File, FileOutputStream, PrintWriter}

import com.pengrad.telegrambot.request.SendMessage
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import scala.io.Source
/**
 * @author ${user.name}
 */

object App {


  def main(args : Array[String]): Unit = {

    import com.pengrad.telegrambot.TelegramBot
    val bot = new TelegramBot("1084535042:AAH3Ob34ofaBwA1xEgE8is7Pbsvi6YqKWFA")

    // Create context with 30 second batch interval
    val conf = new SparkConf().setAppName("spark-kafka")
    val ssc = new StreamingContext(conf, Seconds(30))

    /* Configure Kafka */
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "kafkaa:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "test_group",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("metrics-topic")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent, //Location strategy
      Subscribe[String, String](topics, kafkaParams)
    )

    val streamingPath = "/streaming/"
    val streamingFile = streamingPath+"data.txt"

    val input = stream.map(_.value)

    input.foreachRDD(rdd => { //per ogni stream
      if(rdd.isEmpty()) println("Empty stream.")
      else {
        //Filtro le metriche che mi servono
        if(!new File((streamingPath)).exists()){
          new File(streamingPath).mkdirs()
        }


         val avgTime = rdd.map(_.split("\\|"))
          .filter(word => word.contains("RESPONSE_TIME_IN_MS"))
          .map(x => (x(0),x(1)))
          .map(_._2.toInt).mean()

        val avgRequests = rdd.map(_.split("\\|"))
          .filter(word => word.contains("COUNT_OF_REQ_1S"))
          .map(x => (x(0),x(1)))
          .map(_._2.toInt).mean()


        new PrintWriter(new FileOutputStream(new File(streamingFile),true)) { append(avgRequests.toString+" "+avgTime.toString+"\n"); close }


        val dataFile = Source.fromFile(streamingFile).getLines.toList
        var sumRequest = 0.0
        var sumTime  = 0.0
        var T = 10

        if(dataFile.size < T){
          T = dataFile.size
          dataFile.foreach(row => {
            val pair = row.split(" ")
            var valNumRequest = pair(0).toFloat
            var valTime = pair(1).toFloat

            sumTime += valTime
            sumRequest += valNumRequest
          })
        }

        val lastPair = dataFile.size - T
        if(lastPair > 0){
          dataFile.slice(lastPair,dataFile.size).foreach(row => {
            val pair = row.split(" ")
            var valNumRequest = pair(0).toFloat
            var valTime = pair(1).toFloat

            sumTime += valTime
            sumRequest += valNumRequest
          })
        }

        if(T == 0){
          T = 1
        }

        val avgRequestPast = sumRequest/T
        val avgTimePast = sumTime/T

        println("_____________________________________________________________")
        println("Numero di Richieste Media - ATTUALE: "+avgRequests+ ", PRECEDENTI: "+avgRequestPast)
        println("Tempo di Risposta Media - ATTUALE: "+avgTime+", PRECEDENTI: "+avgTimePast+"ms")
        println("_____________________________________________________________")

        println("Sending alarm message on telegram...\n")

        if(avgTime > avgTimePast*1.2 && avgRequests > avgRequestPast*1.2){
          bot.execute(new SendMessage(183503957, "Numero di Richieste Media - ATTUALE: "+avgRequests+", PRECEDENTI: "+avgRequestPast+".\nTempo di Risposta Media - ATTUALE: "+avgTime+", PRECEDENTI: "+avgTimePast+"ms\n\nIncremento del tempo medio di risposta pari a "+((avgTime/avgTimePast)-1)*100+"%. Registrato anche un\nincremento del numero medio di richieste pari a "+((avgRequests/avgRequestPast)-1)*100+"%"))
          bot.execute(new SendMessage(186181976, "Numero di Richieste Media - ATTUALE: "+avgRequests+", PRECEDENTI: "+avgRequestPast+".\nTempo di Risposta Media - ATTUALE: "+avgTime+", PRECEDENTI: "+avgTimePast+"ms\n\nIncremento del tempo medio di risposta pari a "+((avgTime/avgTimePast)-1)*100+"%. Registrato anche un\nincremento del numero medio di richieste pari a "+((avgRequests/avgRequestPast)-1)*100+"%"))
        }
        if(avgTime > avgTimePast*1.2){
          bot.execute(new SendMessage(183503957,"Numero di Richieste Media - ATTUALE: "+avgRequests+", PRECEDENTI: "+avgRequestPast+".\nTempo di Risposta Media - ATTUALE: "+avgTime+", PRECEDENTI: "+avgTimePast+"ms\n\nIncremento del tempo medio di risposta pari a "+((avgTime/avgTimePast)-1)*100+"%. Non è stato registrato un\nincremento considerevole del numero medio di richieste."))
          bot.execute(new SendMessage(186181976,"Numero di Richieste Media - ATTUALE: "+avgRequests+", PRECEDENTI: "+avgRequestPast+".\nTempo di Risposta Media - ATTUALE: "+avgTime+", PRECEDENTI: "+avgTimePast+"ms\n\nIncremento del tempo medio di risposta pari a "+((avgTime/avgTimePast)-1)*100+"%. Non è stato registrato un\nincremento considerevole del numero medio di richieste."))
        }
      }
    })
    input.print();

    // Start the computation
    ssc.start()
    ssc.awaitTermination()
  }
}
