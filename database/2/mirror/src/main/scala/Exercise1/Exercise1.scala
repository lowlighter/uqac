package Exercise1

import com.exercise1.{BatchLayer, WebServices}
import org.apache.spark.sql.SparkSession

//-------------------------------------------------------------------------------------------
//Main
object Exercise1 extends App {

  //Apache Spark
  val spark = SparkSession.builder
    .master("local")
    .appName("Exercise 1")
    .config("spark.testing.memory", "2147480000")
    .getOrCreate()
  spark.sparkContext.setLogLevel("ERROR")

  //Application
  BatchLayer.compute(spark)
  BatchLayer.load(spark)
  WebServices.start()

}
