package org.castagnolofugale

import java.io.{File, PrintWriter}
import java.net.URLEncoder

import com.pengrad.telegrambot.request.SendMessage
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

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
    //val count = lines.flatMap(line => line.split(" ")).filter(word => word.contains("#")).map(word => (word, 1))
    //val reduced = count.reduceByKey(_ + _)
    //reduced.saveAsTextFiles("hdfs://resources/spark_data")

    input.foreachRDD(rdd => { //per ogni stream
      if(rdd.isEmpty()) println("Empty stream.")
      else {
        //Filtro le metriche che mi servono
        if(!new File((streamingPath)).exists()){
          new File(streamingPath).mkdirs()
        }
        val printWriter = new PrintWriter(streamingFile)
        val lines = rdd.flatMap(line => line.split(" ")).map(w => (w,1))
        val numSamples = lines.count()
        val sumValues = lines.reduceByKey((accumulatedValue: Int, currentValue: Int) => accumulatedValue + currentValue)
        val avgValue = sumValues.map(x => (x._2/numSamples)).reduce((y,z)=>y+z)
        println("Il valore medio di non so che cosa e': "+ avgValue.toString)
        printWriter.write(avgValue.toString)
        println("Sending alarm message on telegram...\n")
        bot.execute(new SendMessage(183503957, "Alarm message!"))
      }
    })
    input.print();

    // Start the computation
    ssc.start()
    ssc.awaitTermination()
  }
}
