package com.supra.scala
import org.apache.log4j._
import org.apache.spark.sql.SparkSession

/**
  * Created by Supra on 19.04.17.
  * With out using SQL syntax and working with structured data
  */

object SparkSQL2 extends java.io.Serializable {

  // Create a custom object type
  case class People(ID: Int, name: String, age: Int, numFriends: Int)

  def mapper(line: String): People = {
    val parts = line.split(',')
    People(parts(0).toInt, parts(1), parts(2).toInt, parts(3).toInt)
  }

  def main(args: Array[String]): Unit = {

    // Set Logger level
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Creating spark session object
    val spark = SparkSession.builder().appName("withoutSQL").master("local[*]").getOrCreate()

    // Here we are reading the csv file line by line but there are other ways to read and load a database for spark sql
    // spark.read.json("json file path")
    // sqlContext.table("hive table name")
    val lines = spark.sparkContext.textFile("../SparkScalaWorkshop/data/fakefriends.csv")
    val Persons = lines.map(mapper)
    import spark.implicits._

    // Why cache: Most RDD operations are lazy, they will not do anything until you call .count or something but if you
    // write like .cache then second time you call count the cached content will be used than reading it again.
    val PerSchema = Persons.toDS().cache()

    PerSchema.printSchema()

    PerSchema.select("name").show()
    PerSchema.filter(PerSchema("age") < 21).show()
    PerSchema.groupBy("age").count().show()
    PerSchema.select(PerSchema("name"), PerSchema("age") + 10).show()
  }


}
