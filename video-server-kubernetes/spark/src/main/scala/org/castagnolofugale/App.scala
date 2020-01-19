package org.castagnolofugale

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

    //val lines = stream.map(_.value)
    //val count = lines.flatMap(line => line.split(" ")).map(word => (word, 1))
    //val reduced = count.reduceByKey(_ + _)
    //reduced.saveAsTextFiles("/opt/spark/data")
    //println(lines)

    // Start the computation
    ssc.start()
    ssc.awaitTermination()

  }

}
